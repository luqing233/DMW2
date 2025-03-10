package fun.luqing.ApiConnector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

public class PostBuilder {
    // 常量定义
    private static final String ACTION = "action";
    private static final String PARAMS = "params";
    public static final String MESSAGE = "message";
    public static final String GROUP_ID = "group_id";
    private static final String ECHO = "echo";
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String TEXT = "text";
    private static final String AT = "at";
    private static final String REPLY = "reply";
    private static final String IMAGE = "image";
    private static final String RECORD = "record";
    private static final String FILE = "file";
    private static final String ID = "id";

    // 基础消息构建模板
    public static JSONObject buildBaseMessage(String action) {
        JSONObject message = new JSONObject();
        message.put(ACTION, action);
        return message;
    }

    public static JSONObject buildBaseMessage(String action, Consumer<JSONObject> paramsBuilder) {
        JSONObject message = new JSONObject();
        message.put(ACTION, action);

        JSONObject params = new JSONObject();
        if (paramsBuilder != null) {
            paramsBuilder.accept(params);
        }
        message.put(PARAMS, params);

        return message;
    }

    // 通用消息数组构建
    public static JSONArray buildMessageArray(Consumer<JSONArray> messageBuilder) {
        JSONArray array = new JSONArray();
        messageBuilder.accept(array);
        return array;
    }

    // 常用消息元素构建方法
    //文本
    public static JSONObject createText(String content) {
        return new JSONObject()
                .put(TYPE, TEXT)
                .put(DATA, new JSONObject().put(TEXT, content));
    }
    //at
    public static JSONObject createAt(long qqId) {
        return new JSONObject()
                .put(TYPE, AT)
                .put(DATA, new JSONObject().put("qq", qqId));
    }
    //回复
    public static JSONObject createReply(int messageId) {
        return new JSONObject()
                .put(TYPE, REPLY)
                .put(DATA, new JSONObject().put(ID, messageId));
    }
    //图片
    public static JSONObject createImage(String url) {
        return new JSONObject()
                .put(TYPE, IMAGE)
                .put(DATA, new JSONObject().put(FILE, url));
    }
    //语音
    public static JSONObject createRecord(String url) {
        return new JSONObject()
                .put(TYPE, RECORD)
                .put(DATA, new JSONObject().put(FILE, url));
    }
}


