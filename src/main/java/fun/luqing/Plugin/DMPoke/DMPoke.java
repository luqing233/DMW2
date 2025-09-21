package fun.luqing.Plugin.DMPoke;

import fun.luqing.ApiConnector.Group.GetGroupMemberInfo;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessage;
import fun.luqing.Config.Config;
import fun.luqing.Plugin.Chat.DeepSeekApi;
import fun.luqing.Plugin.Chat.JsonHandler;
import fun.luqing.Utils.Message.Notice;
import org.json.JSONArray;
import org.json.JSONObject;
import static fun.luqing.DMW2.logger;


public class DMPoke {
    public DMPoke(Notice notice) {
        if ("poke".equals(notice.getSub_type()) && notice.getTarget_id() == notice.getSelf_id()) {

            String nickname = GetGroupMemberInfo.get(notice.getGroup_id(), notice.getUser_id()).getJSONObject("data").getString("nickname");
            String text = nickname+extractKeyword(notice);
            logger.info("收到群 {} 戳一戳事件 ⌈{}⌋",notice.getGroup_id(), text);
            DeepSeekApi api = new DeepSeekApi();
            JsonHandler jsonHandler = new JsonHandler(Config.getInstance().getInt("MAX_MESSAGES"));
            JSONObject requestJson = jsonHandler.buildUserMessageJson(notice,nickname,text);
            String rawResponse = api.sendRequest(requestJson);
            JSONObject reply = api.parseResponse(rawResponse);
            if (reply.getString("status").equals("ok")) {
                new SendGroupMessage(notice.getGroup_id(), reply.getString("data"));
                //sendReply(reply.getString("data"), message);
            }
        }
    }

    /**
     * 从原始消息片段中提取关键字，并在中间插入固定字符串 "你"
     * @param notice 原始消息片段
     * @return 拼接后的关键字
     */
    private String extractKeyword(Notice notice) {
        StringBuilder result = new StringBuilder();
//        String nickname = GetGroupMemberInfo.get(notice.getGroup_id(), notice.getUser_id()).getJSONObject("data").getString("nickname");
//        result.append(nickname);
        try {
            JSONArray array = new JSONArray(notice.getRaw_info());
            int norCount = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if ("nor".equals(obj.optString("type"))) {
                    norCount++;
                    result.append(obj.optString("txt"));
                    if (norCount == 1) {
                        result.append("你");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析 rawInfo 出错: {}", notice.getRaw_info(), e);
        }
        return result.toString();
    }
}


