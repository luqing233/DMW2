package fun.luqing.Plugin.Chat;

import fun.luqing.Config.Config;
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
    public String parseResponse(String response) {
        try {
            //System.out.println(response);
            JSONObject responseObject = new JSONObject(response);
            JSONArray choices = responseObject.getJSONArray("choices");

            // 获取第一个 choice 的 message
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            // 解析 audio 信息
            if (message.has("audio")) {
                JSONObject audio = message.getJSONObject("audio");
                String audioData = audio.getString("data");
                return audioData; // 返回音频数据
            }

            // 处理视频结果
            if (responseObject.has("video_result")) {
                JSONArray videoResults = responseObject.getJSONArray("video_result");
                String videoUrl = videoResults.getJSONObject(0).getString("url");
                return videoUrl; // 返回视频 URL
            }

            // 处理网页搜索结果
            if (responseObject.has("web_search")) {
                JSONArray webSearchResults = responseObject.getJSONArray("web_search");
                StringBuilder searchResults = new StringBuilder();
                for (int i = 0; i < webSearchResults.length(); i++) {
                    JSONObject searchResult = webSearchResults.getJSONObject(i);
                    searchResults.append(searchResult.getString("title"))
                            .append(": ")
                            .append(searchResult.getString("link"))
                            .append("\n");
                }
                return searchResults.toString(); // 返回搜索结果
            }

            return content; // 默认返回内容
        } catch (Exception e) {
            logger.error("BigModel", e);
            return "抱歉，处理回复时出错了";
        }
    }
}
