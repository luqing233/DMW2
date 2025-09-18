package fun.luqing.Plugin.Chat;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageRecord;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Config.Config;
import fun.luqing.Plugin.TTS.TTS;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONObject;

import java.util.HashSet;

import static fun.luqing.DMW2.logger;

public class Chat {

    private static final int MAX_MESSAGES = Config.getInstance().getInt("MAX_MESSAGES");
    private final ApiInterface api;
    private final JsonHandler jsonHandler;

    public Chat(GroupMessage message) {
        this.api = getApiFromConfig();
        this.jsonHandler = new JsonHandler(MAX_MESSAGES);

        if (isMessageValid(message)) {
            logger.info("消息命中: [{}({})] {} ⌈{}⌋",message.getGroup_name(), message.getGroup_id(),message.getNickname(), message.getText());


            String response = sendApiRequest(message);
            if (response != null) {
                handleApiResponse(response, message);
            }
        } else if (message.getText().equals("/clear")) {
            logger.info("清除上下文命中");
            clearUserContext(message);
        }
    }
    private String sendApiRequest(GroupMessage message) {
        try {
            JSONObject requestJson = jsonHandler.buildUserMessageJson(message);
            return api.sendRequest(requestJson);
        } catch (Exception e) {
            logger.error("API请求失败", e);
            return null;
        }
    }
    private ApiInterface getApiFromConfig() {
        String apiChoice = Config.getInstance().getString("AI_MODEL");
        if ("deepseek".equalsIgnoreCase(apiChoice)) {
            return new DeepSeekApi();
        }else  if ("gemini".equalsIgnoreCase(apiChoice)) {
            return new GeminiApi();
        }
        else {
            return new BigModelApi();
        }
    }

    private boolean isMessageValid(GroupMessage message) {
        return message.getType().contains("at") && atMe(message.getAt(), message.getSelf_id());
    }



    private void handleApiResponse(String response, GroupMessage message) {
        try {
            JSONObject reply = api.parseResponse(response);
            if (reply.getString("status").equals("ok")) {
                sendReply(reply.getString("data"), message);
            }else if (reply.getString("status").equals("error")) {
                new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), "DMW产生一个意外错误,原因:\n"+reply.getString("data"));

            }
        } catch (Exception e) {
            logger.error("处理API响应失败", e);
        }
    }

    private void sendReply(String reply, GroupMessage message) {
        jsonHandler.appendAssistantMessage(reply, message.getUser_id());

        // 语音合成
        if(Config.getInstance().isTTS_STATUS()) {
            reply = reply.replaceAll("[(（][^)）]*[)）]", "");
            String audioUrl = TTS.speak(reply, Config.getInstance().getString("TTS_MODEL"));
            new SendGroupMessageRecord().send(message.getGroup_id(), audioUrl);
        } else {
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), reply);
        }
    }

    private boolean atMe(HashSet<GroupMessage.At> ats, long myQQ) {
        return ats != null && ats.stream().anyMatch(at -> at.getQq() == myQQ);
    }

    private void clearUserContext(GroupMessage message) {
        new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(),
                new JsonHandler(MAX_MESSAGES).deleteJson(message.getUser_id()));
    }
}
