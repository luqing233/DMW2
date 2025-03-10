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
    private static final long RECONNECT_DELAY_MS = 5000;     // 重连间隔
    private static final long CONNECT_TIMEOUT_MS = 5000;       // 连接超时时间
    private static final long RESPONSE_TIMEOUT_MS = 50000;      // 消息响应超时

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    // 请求ID与响应的关联
    private final ConcurrentMap<String, CompletableFuture<JSONObject>> pendingRequests = new ConcurrentHashMap<>();
    // 离线消息队列：在连接断开时保存待发送消息
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    // 防止多次重连任务同时执行
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);

    // 每次连接时使用新的 CountDownLatch 等待连接建立
    private CountDownLatch latch;
    private volatile WebSocketClient client;

    // 私有构造方法
    private WebSocket() {
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

    /**
     * 对外公开的连接入口
     */
    public void WSconnect() {
        connect();
    }

    /**
     * 建立 WebSocket 连接
     */
    private void connect() {
        latch = new CountDownLatch(1);
        try {
            // 如果存在旧的连接，则先关闭
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
                    //logger.info("接收到消息: {}", message);
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

            // 等待连接建立，超时后进行重连
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

    /**
     * 处理接收到的消息
     *
     * @param message 消息字符串
     */
    private void processMessage(String message) {
        try {
            JSONObject response = new JSONObject(message);
            // 根据 echo 字段匹配请求
            String responseId = response.optString("echo", null);
            if (responseId != null) {
                CompletableFuture<JSONObject> future = pendingRequests.remove(responseId);
                if (future != null) {
                    future.complete(response);
                }
            }
            // 调用插件处理消息
            new Plugin(message);
        } catch (Exception e) {
            logger.error("处理消息异常", e);
        }
    }

    /**
     * 发送离线消息队列中的所有消息
     */
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

    /**
     * 重连处理，防止多次重连任务同时执行
     */
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

    /**
     * 发送消息，并返回一个 CompletableFuture 用于异步获取响应（带超时控制）。
     * 利用消息 ID 关联请求与响应。
     *
     * @param message 消息内容（JSON 格式字符串）
     * @return CompletableFuture 对象，返回 JSONObject 格式的响应
     */
    public CompletableFuture<JSONObject> sendMessage(String message) {
        // 生成唯一消息 ID，并添加到消息体中
        String messageId = UUID.randomUUID().toString();
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.put("echo", messageId);
        String finalMessage = jsonMessage.toString();

        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);

        // 超时控制
        future.orTimeout(RESPONSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        pendingRequests.remove(messageId);
                    }
                });
        try {
            if (client != null && client.isOpen()) {
                client.send(finalMessage);
            } else {
                // 如果连接未就绪，则将消息入队等待后续发送
                messageQueue.offer(finalMessage);
                logger.warn("连接未就绪，消息已入队等待发送: {}", finalMessage);
            }
        } catch (Exception e) {
            pendingRequests.remove(messageId);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * 关闭 WebSocket 连接以及相关线程池
     */
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

    /**
     *关闭线程池
     *
     * @param executor 线程池实例
     * @param name     线程池名称
     */
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
}
