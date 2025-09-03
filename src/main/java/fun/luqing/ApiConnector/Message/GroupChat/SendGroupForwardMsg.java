package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import static fun.luqing.DMW2.logger;

public class SendGroupForwardMsg {

    /**
     * 构造合并转发消息并发送
     *
     * @param group_id        目标群号
     * @param messageElements 通过PostBuilder创建的原始消息元素数组
     * @return CompletableFuture，完成后可获取完整响应（含发送状态）
     */
    public static CompletableFuture<JSONObject> send(long group_id, JSONArray messageElements) {
        JSONArray nodes = buildMessagesArray(messageElements);

        JSONObject request = PostBuilder.buildBaseMessage("send_group_forward_msg", params -> {
            params.put("group_id", group_id);
            params.put("messages", nodes);
        });

        return PostSender.send(request)
                .thenApply(response -> {
                    logger.info("发送合并转发消息状态: {}", response.optString("status", "unknown"));

                    if (response.has("data") && !response.isNull("data")) {
                        try {
                            int messageId = response.getJSONObject("data").getInt("message_id");
                            logger.debug("合并转发消息ID: {}", messageId);
                        } catch (Exception e) {
                            logger.warn("消息ID解析失败", e);
                        }
                    } else {
                        logger.warn("返回数据中缺少data字段或为null");
                    }

                    return response;
                })
                .exceptionally(e -> {
                    logger.error("发送合并转发消息失败", e);
                    JSONObject error = new JSONObject();
                    error.put("status", "failed");
                    error.put("error", e.toString());
                    return error;
                });
    }

    private static JSONArray buildMessagesArray(JSONArray originalMessages) {
        JSONArray nodes = new JSONArray();
        for (int i = 0; i < originalMessages.length(); i++) {
            JSONObject message = originalMessages.getJSONObject(i);
            nodes.put(createForwardNode(message));
        }
        return nodes;
    }

    private static JSONObject createForwardNode(JSONObject messageElement) {
        return new JSONObject()
                .put("type", "node")
                .put("data", new JSONObject()
                        .put("content", new JSONArray().put(messageElement))
                );
    }
}
