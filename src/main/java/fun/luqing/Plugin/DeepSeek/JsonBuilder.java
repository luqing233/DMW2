package fun.luqing.Plugin.DeepSeek;

import fun.luqing.Config.Config;
import fun.luqing.Utils.FileManage.FileUtil;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonBuilder {
    private final String systemMessage;
    private final String model = Config.getInstance().getDEEPSEEK_MODEL();
    private final boolean stream = false;
    private final String directoryPath = "./data/";
    private final int maxMessages; // 最大消息条数

    public JsonBuilder(int maxMessages) {
        this.maxMessages = maxMessages;
        this.systemMessage = Config.getInstance().getAI_CHARACTER();
    }

    public String addUserMessage(GroupMessage message) {
        String filename = message.getUser_id() + ".json";
        FileUtil fileUtil = new FileUtil(directoryPath);
        JSONObject obj = fileUtil.readJsonFile(filename);

        if (obj == null) {
            // 创建新文件并添加系统消息
            obj = buildBaseObject(message.getNickname());
            fileUtil.writeJsonFile(filename, obj);
        }

        // 添加用户消息
        JSONArray messages = obj.getJSONArray("messages");
        addMessage(messages, "user", message.getText(), message.getNickname());

        // 修剪消息历史
        trimMessages(messages);

        fileUtil.writeJsonFile(filename, obj);
        return obj.toString();
    }

    private JSONObject buildBaseObject(String nickname) {
        JSONObject obj = new JSONObject();
        obj.put("model", model);
        obj.put("stream", stream);

        JSONArray messages = new JSONArray();
        // 添加系统消息
        addMessage(messages, "system", systemMessage.replace("%%", nickname), null);
        obj.put("messages", messages);

        return obj;
    }

    public void appendAssistantMessage(String message, long id) {
        String filename = id + ".json";
        FileUtil fileUtil = new FileUtil(directoryPath);
        JSONObject obj = fileUtil.readJsonFile(filename);

        if (obj == null) {
            obj = new JSONObject();
            obj.put("model", model);
            obj.put("messages", new JSONArray());
        }

        JSONArray messages = obj.getJSONArray("messages");
        addMessage(messages, "assistant", message, null);

        // 修剪消息历史
        trimMessages(messages);

        fileUtil.writeJsonFile(filename, obj);
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
        // 保留系统消息（索引0），从第二条开始删除多余消息
        while (messages.length() > maxMessages) {
            messages.remove(1); // 始终删除第二条消息（索引1）
        }
    }

    public String deleteJson(long id) {
        if (new FileUtil(directoryPath).deleteFile(id + ".json")) {
            return "删除上下文成功";
        }
        return "当前还没有属于你的上下文";
    }
}