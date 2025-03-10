package fun.luqing.ApiConnector.Message;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class SendGroupForwardMsg {

    /**
     * 构造合并转发消息并发送
     * @param group_id  目标群号
     * @param messageElements 通过PostBuilder创建的原始消息元素数组
     */
    public SendGroupForwardMsg(long group_id, JSONArray messageElements) {
        // 构建符合API要求的消息节点数组
        JSONArray nodes = buildMessagesArray(messageElements);

        // 构造最终请求体
        JSONObject request = PostBuilder.buildBaseMessage("send_group_forward_msg", params -> {
            params.put("group_id", group_id);
            params.put("messages", nodes);
        });

        // 发送请求并处理结果
        PostSender.send(request)
                .thenAccept(response -> {
                    logger.info("消息发送状态: {}", response.getString("status"));
                    if (response.has("data")) {
                        logger.debug("消息ID: {}", response.getJSONObject("data").getInt("message_id"));
                    }
                })
                .exceptionally(e -> {
                    logger.error("发送合并转发消息失败", e);
                    return null;
                });
    }

    /**
     * 将原始消息元素转换为转发节点数组
     * @param originalMessages 原始消息数组（包含完整type/data结构的消息元素）
     * @return 符合forward消息格式的节点数组
     */
    private static JSONArray buildMessagesArray(JSONArray originalMessages) {
        JSONArray nodes = new JSONArray();
        for (int i = 0; i < originalMessages.length(); i++) {
            JSONObject message = originalMessages.getJSONObject(i);
            nodes.put(createForwardNode(message));
        }
        return nodes;
    }

    /**
     * 创建单个转发节点
     * @param messageElement 通过PostBuilder创建的完整消息元素
     * @return 组装好的节点结构
     */
    private static JSONObject createForwardNode(JSONObject messageElement) {
        return new JSONObject()
                .put("type", "node")
                .put("data", new JSONObject()
                        .put("content", new JSONArray().put(messageElement))
                );
    }
}