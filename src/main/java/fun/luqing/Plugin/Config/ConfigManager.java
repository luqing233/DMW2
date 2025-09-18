package fun.luqing.Plugin.Config;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Config.Config;
import fun.luqing.Utils.Message.GroupMessage;

import java.util.Objects;

public class ConfigManager {
    public ConfigManager(GroupMessage message) {

        Config cfg=Config.getInstance();

        if (Objects.equals(message.getText(), "配置帮助")){

        }

        if (message.getText().startsWith("修改模型 ")&&cfg.isMaster(message.getUser_id())){
            String model=message.getText().substring("修改模型 ".length());
            Config.getInstance().set("AI_MODEL",model);
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "修改模型为"+model);
        }

        if (message.getText().startsWith("语音角色 ")&&cfg.isMaster(message.getUser_id())){
            String character=message.getText().substring("语音角色 ".length());
            Config.getInstance().set("TTS_MODEL",character);
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "修改语音合成角色为"+character);
        }

        //授权功能
        if (message.getText().startsWith("+admin") && cfg.isMaster(message.getUser_id())) {
            if (message.getAt().isEmpty()) {
                new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "+admin 命令必须 @ 目标用户");
                return;
            }

            StringBuilder addedNames = new StringBuilder();
            StringBuilder existedNames = new StringBuilder();

            for (GroupMessage.At at : message.getAt()) {
                long qq = at.getQq();
                if (qq <= 0) {
                    continue; // 跳过无效 @
                }

                if (cfg.isMaster(qq)) {
                    existedNames.append(at.getName()).append(" ");
                } else {
                    cfg.addMaster(qq);
                    addedNames.append(at.getName()).append(" ");
                }
            }

            if (!addedNames.isEmpty()) {
                new SendGroupMessageReply(
                        message.getGroup_id(),
                        message.getMessage_id(),
                        "已添加 " + addedNames.toString().trim() + " 为 admin"
                );
            }

            if (!existedNames.isEmpty()) {
                new SendGroupMessageReply(
                        message.getGroup_id(),
                        message.getMessage_id(),
                        existedNames.toString().trim() + " 已经是 admin"
                );
            }
        }



    }
}
