package fun.luqing.Plugin.Chat;

import fun.luqing.Config.Config;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class DeepSeekApi implements ApiInterface {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = "sk-75c38aaf00fb439c95b4d0796e1a1216"; // 替换为实际的 DeepSeek API 密钥

    @Override
    public String sendRequest(JSONObject jsonBody) {
        logger.info("正在由DeepSeek处理");
        jsonBody.put("model", Config.getInstance().getString("DEEPSEEK_MODEL"));
        return ClientRequest.sendPostRequest(API_URL, API_KEY, jsonBody.toString());
    }

    @Override
    public String parseResponse(String response) {
        try {
            JSONObject responseObject = new JSONObject(response);
            return responseObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            logger.error("DeepSeek", e);
            return "抱歉，处理回复时出错了";
        }
    }
}
