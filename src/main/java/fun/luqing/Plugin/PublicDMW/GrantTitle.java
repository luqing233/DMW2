package fun.luqing.Plugin.PublicDMW;

import fun.luqing.ApiConnector.Group.SetGroupSpecialTitle;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessage;
import fun.luqing.Utils.Message.GroupMessage;

import java.util.HashSet;

public class GrantTitle {
    public GrantTitle(GroupMessage message) {
        //System.out.println(message.getText());
        if (message.getType().contains("at")&&message.getText().startsWith("授衔")){
            System.out.println("授衔命中");
            String grant=message.getText().substring(3);
            String status=new SetGroupSpecialTitle(message.getGroup_id(), at(message.getAt()), grant).set();
            if (status.equals("ok")){
                status="授衔成功";
            }
            new SendGroupMessage(message.getGroup_id(), status);

        }
    }
    private long at(HashSet<GroupMessage.At> ats) {
        long qq=0;
        for (GroupMessage.At at : ats) {
            qq+=at.getQq();
            break;
        }
        return qq;
    }
}
