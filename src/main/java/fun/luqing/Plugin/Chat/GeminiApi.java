package fun.luqing.Plugin.Chat;

import org.json.JSONArray;
import org.json.JSONObject;

import static fun.luqing.DMW2.logger;

/**
 * GeminiApi 类实现了 ApiInterface 接口，用于与 Google 的 Gemini API 进行交互。
 * 提供了发送请求和解析响应的功能。
 */
public class GeminiApi implements ApiInterface {

    // Gemini API endpoint（生成内容）
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + fun.luqing.Config.Config.getInstance().getString("GEMINI_MODEL")
            + ":generateContent";

    private static final String API_KEY = fun.luqing.Config.Config.getInstance().getString("GEMINI_API_KEY");

    /**
     * 向 Gemini API 发送聊天请求。
     *
     * @param chatJson 包含聊天信息的 JSON 对象，格式应符合 Chat SDK 的标准
     * @return API 返回的原始响应字符串
     */
    @Override
    public String sendRequest(JSONObject chatJson) {
        logger.info("正在由 Gemini API 处理");

        // 自动转换 Chat SDK JSON -> Gemini REST JSON
        JSONObject geminiJson = convertChatMessagesToGemini(chatJson);

        // 发送请求（带代理）
        return ClientRequest.sendPostRequestWithQueryParam(API_URL, API_KEY, geminiJson.toString());
    }

    /**
     * 解析 Gemini API 的响应字符串，提取出实际的回复文本。
     *
     * @param response Gemini API 返回的原始响应字符串
     * @return 解析后的回复文本，如果解析失败则返回错误提示信息
     */
    @Override
    public String parseResponse(String response) {
        try {
            JSONObject responseObject = new JSONObject(response);

            System.out.println(responseObject);

            if (responseObject.has("candidates")) {
                JSONArray candidates = responseObject.getJSONArray("candidates");
                if (!candidates.isEmpty()) {
                    JSONObject first = candidates.getJSONObject(0);
                    JSONObject content = first.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (!parts.isEmpty()) {
                        return parts.getJSONObject(0).getString("text");
                    }
                }
            }

            return "未解析到有效的 Gemini 回复";
        } catch (Exception e) {
            logger.error("Gemini 解析失败", e);
            return "抱歉，处理 Gemini 回复时出错了";
        }
    }

    /**
     * 将 Chat SDK 风格的 messages 转换为 Gemini REST API 可用的 contents 格式。
     * 所有消息将被拼接为一个文本块，并封装为 Gemini 所需的结构。
     *
     * @param chatJson 原始的 Chat SDK 消息 JSON 对象
     * @return 符合 Gemini API 要求的请求体 JSON 对象
     */
    private JSONObject convertChatMessagesToGemini(JSONObject chatJson) {
        JSONArray messages = chatJson.optJSONArray("messages");
        if (messages == null) messages = new JSONArray();

        StringBuilder combinedText = new StringBuilder();

        for (int i = 0; i < messages.length(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            String role = msg.optString("role");
            String content = msg.optString("content");
            if (!content.isEmpty()) {
                combinedText.append(role).append(": ").append(content).append("\n");
            }
        }

        JSONObject geminiJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentItem = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();


        part.put("text", combinedText.toString().trim());
        parts.put(part);
        contentItem.put("parts", parts);
        contents.put(contentItem);
        geminiJson.put("contents", contents);

        return geminiJson;
    }
}
