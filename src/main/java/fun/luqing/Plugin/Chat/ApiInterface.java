package fun.luqing.Plugin.Chat;

import org.json.JSONObject;

public interface ApiInterface {
    String sendRequest(JSONObject jsonBody);   // 发送请求
    JSONObject parseResponse(String response); // 解析响应
}
