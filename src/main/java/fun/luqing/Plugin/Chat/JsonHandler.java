package fun.luqing.Plugin.Chat;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Config.Config;
import fun.luqing.Utils.FileManage.FileUtil;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class JsonHandler {
    private final String systemMessage;
    //private final String model = Config.getInstance().getDEEPSEEK_MODEL();
    private final boolean stream = false;
    private final String directoryPath = "./data/";
    private final int maxMessages;

    public JsonHandler(int maxMessages) {
        this.maxMessages = maxMessages;
        this.systemMessage = Config.getInstance().getString("AI_CHARACTER");
    }

    public JSONObject buildUserMessageJson(GroupMessage message) {
        String filename = message.getUser_id() + ".json";
        FileUtil fileUtil = new FileUtil(directoryPath);
        JSONObject jsonObject = fileUtil.readJsonFile(filename);

        if (jsonObject == null) {
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "接下来将由"+Config.getInstance().getString("AI_MODEL")+"回答，本提示仅在创建上下文记录时提示。如遇意外情况，可使用/clear重置上下文");
            jsonObject = buildInitialJson(message.getNickname());
            fileUtil.writeJsonFile(filename, jsonObject);
        }

        // 添加用户消息
        JSONArray messages = jsonObject.getJSONArray("messages");
        //System.out.println(message.getText());
        if (Objects.equals(message.getText(), "")) {
            addMessage(messages, "user", "["+message.getTime()+"] (此处用户发送了一个空白符号，意思是什么话都没说)", message.getNickname());
        }else {
            addMessage(messages, "user", "["+message.getTime()+"] "+message.getText(), message.getNickname());
        }
        trimMessages(messages);
        fileUtil.writeJsonFile(filename, jsonObject);
        return jsonObject;
    }

    private JSONObject buildInitialJson(String nickname) {
        JSONObject jsonObject = new JSONObject();
        //jsonObject.put("model", model);
        jsonObject.put("stream", stream);

        JSONArray messages = new JSONArray();
        addMessage(messages, "system", systemMessage.replace("%%", nickname), null);
        jsonObject.put("messages", messages);

        return jsonObject;
    }

    public void appendAssistantMessage(String message, long userId) {
        String filename = userId + ".json";
        FileUtil fileUtil = new FileUtil(directoryPath);
        JSONObject jsonObject = fileUtil.readJsonFile(filename);

        if (jsonObject == null) {
            jsonObject = new JSONObject();
            //jsonObject.put("model", model);
            jsonObject.put("messages", new JSONArray());
        }

        JSONArray messages = jsonObject.getJSONArray("messages");
        addMessage(messages, "assistant", message, null);
        trimMessages(messages);
        fileUtil.writeJsonFile(filename, jsonObject);
    }

    private void addMessage(JSONArray messages, String role, String content, String name) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        if (name != null) {
            msg.put("name", name);
        }
        messages.put(msg);
    }

    private void trimMessages(JSONArray messages) {
        while (messages.length() > maxMessages) {
            messages.remove(1); // 始终删除第二条消息（索引1）
        }
    }

    public String deleteJson(long userId) {
        if (new FileUtil(directoryPath).deleteFile(userId + ".json")) {
            return "删除上下文成功";
        }
        return "当前还没有属于你的上下文";
    }
}
