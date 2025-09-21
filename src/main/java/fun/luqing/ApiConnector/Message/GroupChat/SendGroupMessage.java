package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;

import static fun.luqing.DMW2.logger;

public class SendGroupMessage {

    public SendGroupMessage(long groupId, String text) {
        JSONArray message = PostBuilder.buildMessageArray(array ->
                array.put(PostBuilder.createText(text))
        );
        sendMessage(groupId, message);
    }

    public SendGroupMessage(long groupId, JSONArray message) {
        sendMessage(groupId, message);
    }

    private void sendMessage(long groupId, JSONArray message) {
        PostSender.sendGroupMessage(groupId, message)
                .thenAccept(record -> logger.info("群消息发送状态: {}", record.optString("status", "unknown")))
                .exceptionally(e -> {
                    logger.error("发送群消息出现异常", e);
                    return null;
                });
    }
}
