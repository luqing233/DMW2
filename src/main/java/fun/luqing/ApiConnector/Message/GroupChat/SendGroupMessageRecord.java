package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;


public class SendGroupMessageRecord {
    public void send(long groupId, String url) {
        JSONArray message = PostBuilder.buildMessageArray(array -> {
            array.put(PostBuilder.createRecord(url));
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