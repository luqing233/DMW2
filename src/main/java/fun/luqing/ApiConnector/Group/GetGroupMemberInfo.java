package fun.luqing.ApiConnector.Group;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import org.json.JSONObject;

public class GetGroupMemberInfo {

    /**
     * 获取群成员信息
     * @param group_id 群号
     * @param user_id 用户QQ
     * @return 返回接口的 JSONObject 响应
     */
    public static JSONObject get(long group_id, long user_id) {
        JSONObject request = PostBuilder.buildBaseMessage("get_group_member_info", params -> {
            params.put("group_id", group_id);
            params.put("user_id", user_id);
        });
        return PostSender.send(request).join();
    }
}
