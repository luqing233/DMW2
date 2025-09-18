package fun.luqing.Plugin.Chat;

import fun.luqing.Config.Config;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

public class BigModelApi implements ApiInterface {

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String API_KEY = "18704c372131c74c7d46b1c1e01d41b2.fDtE1gtcD6tszWty"; // 替换为实际的新 API 密钥

    @Override
    public String sendRequest(JSONObject jsonBody) {
        logger.info("正在由智谱AI处理");
        jsonBody.put("model", Config.getInstance().getString("BIGMODEL_MODEL"));
        return ClientRequest.sendPostRequest(API_URL, API_KEY, jsonBody.toString());
    }

    @Override
    public JSONObject parseResponse(String response) {
        try {
            //System.out.println(response);

            return getJsonObject(response); // 默认返回内容
        } catch (Exception e) {
            logger.error("BigModel", e);
            JSONObject responseData=new JSONObject();
            responseData.put("status","error");
            responseData.put("data",e);
            return responseData;
        }
    }

    @NotNull
    private static JSONObject getJsonObject(String response) {
        JSONObject responseObject = new JSONObject(response);
        JSONArray choices = responseObject.getJSONArray("choices");

        // 获取第一个 choice 的 message
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String content = message.getString("content");
        JSONObject responseData=new JSONObject();
        responseData.put("status","ok");
        responseData.put("data",content);
        return responseData;
    }
}
