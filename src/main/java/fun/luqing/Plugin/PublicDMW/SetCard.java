package fun.luqing.Plugin.PublicDMW;

import fun.luqing.ApiConnector.Group.SetGroupCard;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessage;
import fun.luqing.Utils.Message.GroupMessage;

import java.util.HashSet;

public class SetCard {
    public SetCard(GroupMessage message) {

        if (message.getType().contains("at")&&message.getText().startsWith("赐名")){
            System.out.println("赐名命中");
            String card=message.getText().substring(3);
            String status=new SetGroupCard(message.getGroup_id(), at(message.getAt()), card).set();
            if (status.equals("ok")){
                status="赐名成功,由"+message.getNickname()+"操作";
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
