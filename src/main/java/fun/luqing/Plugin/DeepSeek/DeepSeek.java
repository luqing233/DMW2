
package fun.luqing.Plugin.DeepSeek;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageRecord;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Config.Config;
import fun.luqing.Plugin.TTS.TTS;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

import static fun.luqing.DMW2.logger;

public class DeepSeek {
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = "sk-75c38aaf00fb439c95b4d0796e1a1216";
    // 自定义最大消息条数（包括系统消息）
    private static final int MAX_MESSAGES = Config.getInstance().getMAX_MESSAGES();

    public DeepSeek(GroupMessage message) {
        if (isValidMessage(message)) {
            logger.info("消息命中: {}:{}", message.getNickname(), message.getText());
            String response = sendDeepSeekRequest(message);
            if (response != null) {
                processApiResponse(response, message);
            }
        } else if (message.getText().equals("/clear")) {
            logger.info("操作命中");
            new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(),
                    new JsonBuilder(MAX_MESSAGES).deleteJson(message.getUser_id()));
        }
    }

    private boolean isValidMessage(GroupMessage message) {
        return message.getType().contains("at") && atMe(message.getAt(), message.getSelf_id());
    }

    private String sendDeepSeekRequest(GroupMessage message) {
        JsonBuilder jb = new JsonBuilder(MAX_MESSAGES);
        String request = jb.addUserMessage(message);
        return ClientDeepSeek.sendRequest(DEEPSEEK_API_URL, API_KEY, request);
    }

    private void processApiResponse(String response, GroupMessage message) {
        String reply = parseApiResponseContent(response);
        if (reply != null) {
            JsonBuilder jb = new JsonBuilder(MAX_MESSAGES);
            jb.appendAssistantMessage(reply, message.getUser_id());
            reply = reply.replaceAll("[\\(（][^\\)）]*[\\)）]", "");//（轻轻放下咖啡杯）

            String url=TTS.speak(reply,"bronya");
            if (!url.equals("0") && !url.equals("-1")) {
                new SendGroupMessageRecord().send(message.getGroup_id(), url);
            }else {
                new SendGroupMessageReply(message.getGroup_id(), message.getMessage_id(), reply);
            }

            //TTS.speak(reply, message.getGroup_id(),"bronya");

        }
    }

    private String parseApiResponseContent(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray choices = obj.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            logger.info("推理过程: {}", message.optString("reasoning_content", "无"));
            return message.getString("content");
        } catch (Exception e) {
            logger.error("解析DeepSeek响应失败", e);
            return "抱歉，处理回复时出错了";
        }
    }

    private boolean atMe(HashSet<GroupMessage.At> ats, long myQQ) {
        return ats != null && ats.stream().anyMatch(at -> at.getQq() == myQQ);
    }
}