package fun.luqing.Plugin.Config;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Utils.Message.GroupMessage;

import java.util.Objects;

public class Config {
    public Config(GroupMessage message) {
        if (Objects.equals(message.getText(), "配置帮助")){

        }else if (message.getText().startsWith("修改模型 ")&&message.getUser_id()==3253912136L){
            String model=message.getText().substring("修改模型 ".length());
            fun.luqing.Config.Config.getInstance().setAI_MODEL(model);
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "修改模型为"+model);
        }else if (message.getText().startsWith("语音角色 ")&&message.getUser_id()==3253912136L){
            String character=message.getText().substring("语音角色 ".length());
            fun.luqing.Config.Config.getInstance().setTTS_MODEL(character);
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "修改语音合成角色为"+character);
        }
    }
}
