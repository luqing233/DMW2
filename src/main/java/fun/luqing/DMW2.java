package fun.luqing;

import fun.luqing.ApiConnector.Group.GetGroupMemberList;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageRecord;
import fun.luqing.ApiConnector.User.GetFriendList;
import fun.luqing.ApiConnector.Utils.Friend;
import fun.luqing.ApiConnector.Utils.GroupMember;
import fun.luqing.WebSocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class DMW2 {
    public static final Logger logger = LoggerFactory.getLogger(DMW2.class);

    public static void main(String[] args) {

        try {
            WebSocket wsClient = WebSocket.getInstance();
            wsClient.WSconnect();
            logger.info("WebSocket 客户端已启动");
            /*GetGroupMemberList getGroupMemberList = new GetGroupMemberList();
            for (GroupMember member:getGroupMemberList.get(688595279L)){
                System.out.println(member.getTitle());
            }*/


        } catch (Exception e) {
            logger.error("程序启动失败", e);
            System.exit(1);
        }

    }
}