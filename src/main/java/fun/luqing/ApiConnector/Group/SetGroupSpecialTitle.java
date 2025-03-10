package fun.luqing.ApiConnector.Group;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONException;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class SetGroupSpecialTitle {
    JSONObject request;
    public SetGroupSpecialTitle(long group_id, long user_id, String special_title) {
         request = PostBuilder.buildBaseMessage("set_group_special_title", params -> {
            params.put("group_id", group_id);
            params.put("user_id", user_id);
            params.put("special_title", special_title);
            // System.out.println(params);
        });
    }

    public String set() {
        try {
            JSONObject response = PostSender.send(request).join(); // 阻塞等待结果
            //System.out.println(response);
            return response.getString("status");
        } catch (JSONException e) {
            logger.error("授勋失败");
            throw new RuntimeException(e);
        }
    }
}
