package fun.luqing.WebSocket;

import fun.luqing.Config.Config;
import fun.luqing.Plugin.Plugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import static fun.luqing.Color.ANSIColors.*;
import static fun.luqing.DMW2.logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocket {
    private static volatile WebSocket instance;

    private static final long RECONNECT_DELAY_MS = 5000;       // 重连间隔
    private static final long CONNECT_TIMEOUT_MS = 5000;       // 连接超时时间
    private static final long RESPONSE_TIMEOUT_MS = 150_000;   // 消息响应超时
    private static final int MESSAGE_QUEUE_LIMIT = 500;        // 离线消息最大缓存
    private static final int PENDING_REQUEST_LIMIT = 5000;     // 请求缓存最大容量

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 插件任务线程池（核心 14，最大 28，队列 200）
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            14,
            28,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // 请求 ID 与响应的关联（带 TTL）
    private final ConcurrentMap<String, TimedFuture> pendingRequests = new ConcurrentHashMap<>();

    // 离线消息队列（有限大小）
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(MESSAGE_QUEUE_LIMIT);

    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private CountDownLatch latch;
    private volatile WebSocketClient client;

    // 内部类：包装 future 和创建时间，便于过期清理
    private static class TimedFuture {
        final CompletableFuture<JSONObject> future;
        final long createTime;

        TimedFuture(CompletableFuture<JSONObject> future) {
            this.future = future;
            this.createTime = System.currentTimeMillis();
        }
    }

    private WebSocket() {
        // 定期清理超时的 pendingRequests，避免 OOM
        scheduler.scheduleAtFixedRate(this::cleanupPendingRequests, 1, 30, TimeUnit.SECONDS);
    }

    public static WebSocket getInstance() {
        if (instance == null) {
            synchronized (WebSocket.class) {
                if (instance == null) {
                    instance = new WebSocket();
                }
            }
        }
        return instance;
    }

    public void WSconnect() {
        connect();
    }

    private void connect() {
        latch = new CountDownLatch(1);
        try {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    logger.warn("关闭旧的 WebSocket 连接异常", e);
                }
                client = null;
            }

            URI uri = new URI(Config.getInstance().getWS_URL());
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    logger.info(ANSI_GREEN + "连接成功" + ANSI_RESET);
                    latch.countDown();
                    flushMessageQueue();
                }

                @Override
                public void onMessage(String message) {
                    executorService.submit(() -> processMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.error(ANSI_RED + "连接中断: {}" + ANSI_RESET, reason);
                    handleReconnect();
                }

                @Override
                public void onError(Exception e) {
                    logger.error("WebSocket 错误", e);
                }
            };

            client.connect();

            if (!latch.await(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.warn("WebSocket 连接超时，尝试重连...");
                handleReconnect();
            }
        } catch (URISyntaxException e) {
            logger.error("无效的 WebSocket URL: {}", Config.getInstance().getWS_URL(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("WebSocket 连接等待中断", e);
            handleReconnect();
        }
    }

    private void processMessage(String message) {
        try {
            JSONObject response = new JSONObject(message);

            // 处理 echo 响应
            String responseId = response.optString("echo", null);
            if (responseId != null) {
                TimedFuture timed = pendingRequests.remove(responseId);
                if (timed != null) {
                    timed.future.complete(response);
                    return; // 响应消息不再进入插件
                }
            }

            // 并行执行插件任务
            for (Runnable plugin : Plugin.getPlugins(response)) {
                executorService.submit(plugin);
            }

        } catch (Exception e) {
            logger.error("处理消息异常", e);
        }
    }

    private void flushMessageQueue() {
        String msg;
        while ((msg = messageQueue.poll()) != null) {
            try {
                client.send(msg);
                logger.info("发送离线队列消息: {}", msg);
            } catch (Exception e) {
                logger.error("发送离线消息失败", e);
            }
        }
    }

    private void handleReconnect() {
        if (isReconnecting.compareAndSet(false, true)) {
            scheduler.schedule(() -> {
                try {
                    logger.info(ANSI_GREEN + "尝试重连..." + ANSI_RESET);
                    connect();
                } finally {
                    isReconnecting.set(false);
                }
            }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    public CompletableFuture<JSONObject> sendMessage(String message) {
        String messageId = UUID.randomUUID().toString();
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.put("echo", messageId);
        String finalMessage = jsonMessage.toString();

        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        pendingRequests.put(messageId, new TimedFuture(future));

        // 超时控制
        future.orTimeout(RESPONSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .whenComplete((result, throwable) -> pendingRequests.remove(messageId));

        try {
            if (client != null && client.isOpen()) {
                client.send(finalMessage);
            } else {
                if (!messageQueue.offer(finalMessage)) {
                    logger.warn("离线消息队列已满，丢弃消息: {}", finalMessage);
                } else {
                    logger.warn("连接未就绪，消息已入队等待发送: {}", finalMessage);
                }
            }
        } catch (Exception e) {
            pendingRequests.remove(messageId);
            future.completeExceptionally(e);
        }

        return future;
    }

    private void cleanupPendingRequests() {
        long now = System.currentTimeMillis();
        if (pendingRequests.size() > PENDING_REQUEST_LIMIT) {
            logger.warn("pendingRequests 超过限制，开始清理");
        }
        pendingRequests.forEach((id, timed) -> {
            if (timed.future.isDone() ||
                    now - timed.createTime > RESPONSE_TIMEOUT_MS) {
                pendingRequests.remove(id);
            }
        });
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.warn("关闭 WebSocket 时出现异常", e);
            }
        }
        shutdownExecutor(executorService, "executorService");
        shutdownExecutor(scheduler, "scheduler");
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("强制关闭 {}，可能丢弃部分任务", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    //获取运行状态（线程池 + 队列）
    public String getStatus() {
        return String.format(
                """
                        线程池状态:
                            活跃线程=%d
                            池中线程=%d
                            队列大小=%d
                            已完成任务=%d
                            总任务数=%d
                        请求缓存=%d, 离线消息队列=%d""",
                executorService.getActiveCount(),
                executorService.getPoolSize(),
                executorService.getQueue().size(),
                executorService.getCompletedTaskCount(),
                executorService.getTaskCount(),
                pendingRequests.size(),
                messageQueue.size()
        );
    }
}
