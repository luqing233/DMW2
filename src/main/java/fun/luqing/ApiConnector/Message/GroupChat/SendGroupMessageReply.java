package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class SendGroupMessageReply {
    public SendGroupMessageReply(long groupId, int messageId, String text) {
        logger.info("尝试发送回复消息⌈{}⌋", text);
        JSONArray message = PostBuilder.buildMessageArray(array -> {
            array.put(PostBuilder.createReply(messageId));
            array.put(PostBuilder.createText(" " + text));
        });

        // 异步处理返回的 JSONObject
        PostSender.sendGroupMessage(groupId, message)
                .thenAccept(record -> {
                    //System.out.println(record.toString());
                    if (record.getString("status").equals("ok")) {
                        logger.info("发送回复消息⌈{}⌋成功",text);
                    }
                })
                .exceptionally(e -> {
                    logger.error("发送群消息出现异常", e);
                    return null;
                });

    }
    public SendGroupMessageReply(long groupId, int messageId, String text,Long at) {
        JSONArray message = PostBuilder.buildMessageArray(array -> {
            array.put(PostBuilder.createReply(messageId));
            array.put(PostBuilder.createAt(at));
            array.put(PostBuilder.createText(" " + text));
        });
        PostSender.sendGroupMessage(groupId, message)
                .thenAccept(record -> {
                    System.out.println(record.toString());
                    logger.info(record.getString("status"));
                })
                .exceptionally(e -> {
                    logger.error("发送群消息出现异常", e);
                    return null;
                });
    }
}
