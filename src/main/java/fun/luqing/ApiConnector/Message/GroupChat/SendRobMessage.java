package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONArray;

import java.util.Map;

import static fun.luqing.DMW2.logger;

public class SendRobMessage {
    public SendRobMessage(Map<Long, Double> victims, long murderer, long groupId, double illicit) {
        JSONArray message = PostBuilder.buildMessageArray(array -> {
            // 案情描述
            array.put(PostBuilder.createText("据呆毛王报道，本群发生一起重大银行抢劫案，已经对多名群员造成金币损失\n"));

            // 受害者列表
            victims.forEach((qq, amount) -> {
                array.put(PostBuilder.createAt(qq));
                array.put(PostBuilder.createText(String.format("%d\n\t损失%.2f枚金币\n", qq, amount)));
            });

            // 罪犯信息
            array.put(PostBuilder.createText("目前，罪犯"));
            array.put(PostBuilder.createAt(murderer));
            array.put(PostBuilder.createText(String.format(" 仍携赃款%.2f枚金币逍遥法外", illicit)));
        });
        PostSender.sendGroupMessage(groupId, message)
                .thenAccept(record -> {
                    //System.out.println(record.toString());
                    logger.info(record.getString("status"));
                })
                .exceptionally(e -> {
                    logger.error("发送群消息出现异常", e);
                    return null;
                });
    }
}
