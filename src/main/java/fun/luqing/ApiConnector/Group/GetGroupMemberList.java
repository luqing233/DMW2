package fun.luqing.ApiConnector.Group;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import fun.luqing.ApiConnector.Utils.GroupMember;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static fun.luqing.DMW2.logger;

public class GetGroupMemberList {


    // 同步版本（会阻塞当前线程）
public ArrayList<GroupMember> get(Long gid) throws Exception {
    if (gid == null) {
        throw new IllegalArgumentException("群ID不能为空");
    }

    JSONObject request = PostBuilder.buildBaseMessage("get_group_member_list", params -> {
        params.put("group_id", gid);
        params.put("no_cache", false);
    });

    try {
        JSONObject response = PostSender.send(request).join(); // 阻塞等待结果
        if (response == null) {
            throw new Exception("获取成员列表失败：响应为空");
        }

        JSONArray groupMembersArray = response.getJSONArray("data");
        if (groupMembersArray == null) {
            throw new Exception("获取成员列表失败：数据为空");
        }

        ArrayList<GroupMember> groupMembers = new ArrayList<>();

        for (int i = 0; i < groupMembersArray.length(); i++) {
            groupMembers.add(new GroupMember(groupMembersArray.getJSONObject(i)));
        }

        logger.info("成功获取{}个群成员，状态: {}", groupMembers.size(), response.getString("status"));
        return groupMembers;
    } catch (Exception e) {
        logger.error("获取群成员列表时发生异常", e);
        throw new Exception("获取群成员列表失败", e);
    }
}

}
