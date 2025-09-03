package fun.luqing.Plugin.PublicDMW;

import fun.luqing.ApiConnector.Group.SetGroupSpecialTitle;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessage;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Utils.Message.GroupMessage;
import fun.luqing.WebSocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;

public class Pool {
    public Pool(GroupMessage message) {
        //System.out.println(message.getText());
        if (Objects.equals(message.getText(), "线程情况")){
            WebSocket webSocket = WebSocket.getInstance();

            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), webSocket.getStatus());




        }
    }

}
