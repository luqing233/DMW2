package fun.luqing.ApiConnector.Message.GroupChat;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class SetGroupAddRequest {
    public  SetGroupAddRequest(String flag,Boolean approve,String reason) {
        JSONObject request= PostBuilder.buildBaseMessage("set_group_add_request", params->{
            params.put("flag",flag);
            params.put("approve",approve);
            params.put("reason",reason);
        });
        PostSender.send(request)
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
