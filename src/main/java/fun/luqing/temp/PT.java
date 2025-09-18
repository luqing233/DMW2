package fun.luqing.temp;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupForwardMsg;
import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.User.GetFriendList;
import fun.luqing.ApiConnector.Utils.Friend;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;

public class PT {
    public PT(GroupMessage info){
        if (info.getUser_id()==3253912136L&& Objects.equals(info.getText(), "好友列表")){
            GetFriendList friendList = new GetFriendList();
            try {
                ArrayList<Friend> FL = friendList.get();
                JSONArray jsonArray = new JSONArray();
                int i=0;
                for (Friend friend : FL) {

                    String data= "QQ号:"+String.valueOf(friend.getUser_id())+"\n昵称:"+friend.getNickname()+"\n性别:"+friend.getSex()+"\n生日:"+friend.getBirthday();
                    jsonArray.put(PostBuilder.createText(data));
                    if(i++>10){
                        break;
                    }

                }
                SendGroupForwardMsg.send(info.getGroup_id(),jsonArray );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }






















    /*private static final long TARGET_GROUP_ID = 688595279L;
    private static final String AT_MESSAGE_PREFIX = "你艾特了\n";

    public PT(GroupMessage info) {
        if (isTargetGroup(info)) {
            sendAtNotification(info);
        }
    }

    private boolean isTargetGroup(GroupMessage info) {
        return info.getGroup_id() == TARGET_GROUP_ID;
    }

    private void sendAtNotification(GroupMessage info) {
        StringJoiner messageJoiner = new StringJoiner("\n", AT_MESSAGE_PREFIX, "");

        info.getAt().stream()
                .map(GroupMessage.At::getName)
                .forEach(messageJoiner::add);

        if (messageJoiner.length() > AT_MESSAGE_PREFIX.length()) {
            new SendGroupMessageReply(
                    info.getGroup_id(),
                    info.getMessage_id(),
                    messageJoiner.toString()
            );
        }
    }*/
}