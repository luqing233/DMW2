package fun.luqing.ApiConnector.Group;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONException;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class SetGroupCard {
    JSONObject request;
    public SetGroupCard(long group_id, long user_id, String card) {
        request = PostBuilder.buildBaseMessage("set_group_card", params -> {
            params.put("group_id", group_id);
            params.put("user_id", user_id);
            params.put("card", card);
        });
    }

    public String set() {
        try {
            JSONObject response = PostSender.send(request).join(); // 阻塞等待结果
            return response.getString("status");
        } catch (JSONException e) {
            logger.error("授勋失败");
            throw new RuntimeException(e);
        }

    }
}
