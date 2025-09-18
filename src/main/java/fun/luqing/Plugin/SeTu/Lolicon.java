package fun.luqing.Plugin.SeTu;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupForwardMsg;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fun.luqing.DMW2.logger;

public class Lolicon {

    public Lolicon(GroupMessage groupMessage) {
        String content = groupMessage.getText().trim();

        if (content.equalsIgnoreCase("涩图 help")) {
            logger.info("触发涩图帮助指令");
            String helpMsg = "涩图 API 使用帮助：\n" +
                    "格式：涩图 参数\n\n" +
                    "可用参数：\n" +
                    "- num=1~3（图片数量）\n" +
                    "- tag=萝莉/白丝（多个tag支持“/”分隔表示“或”，组与组之间用逗号、空格、分号等多种中英文标点分隔均支持）\n" +
                    "  例如：tag=萝莉/少女, 白丝/黑丝\n\n" +
                    "- r18=0/1/2（是否R18）\n" +
                    "- uid=123,456（指定画师）\n\n";
            new SendGroupMessageReply(groupMessage.getGroup_id(), groupMessage.getMessage_id(), helpMsg);
            return;
        }

        if (content.matches("^涩图(\\s.*)?$")) {
            Map<String, Object> params = parseParameters(content.substring(2).trim());

            // 默认用POST请求，可改为GET测试
            JSONObject response = sendApiRequest(params);
            //JSONObject response = sendApiGetRequest(params); // 如果想用GET请求，取消注释这一行，注释上一行

            handleApiResponse(response, groupMessage);
        }
    }

    private Map<String, Object> parseParameters(String input) {
        Map<String, Object> params = new HashMap<>();

        // 匹配key=value形式的参数
        Pattern pattern = Pattern.compile("(\\w+)=([^\\s]+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2);

            try {
                switch (key) {
                    case "num":
                    case "r18":
                        params.put(key, Integer.parseInt(value));
                        break;
                    case "proxy":
                        params.put(key, value);
                        break;
                    case "uid":
                        JSONArray uidArray = new JSONArray();
                        for (String uid : value.split("[,，]")) { // 支持中文英文逗号分割
                            uidArray.put(Integer.parseInt(uid.trim()));
                        }
                        params.put("uid", uidArray);
                        break;
                    case "tag":
                        JSONArray tagArray = new JSONArray();

                        // 支持多组tag，用多种中英文标点分隔：
                        // 逗号,， 空格 \s 分号;；等，先统一用正则切分
                        String[] tagGroups = value.split("[,，;；\\s]+");

                        for (String tagGroup : tagGroups) {
                            // 支持用 '/' 代替 '|'
                            tagGroup = tagGroup.replace('/', '|').replace('／', '|');
                            tagArray.put(tagGroup);
                        }
                        params.put("tag", tagArray);
                        break;
                }
            } catch (Exception e) {
                logger.warn("参数解析失败：" + key + "=" + value + "，原因：" + e.getMessage());
            }
        }

        return params;
    }

    // POST请求
    private JSONObject sendApiRequest(Map<String, Object> customParams) {
        try {
            String apiUrl = "https://api.lolicon.app/setu/v2";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();

            // 默认参数
            payload.put("num", 1);
            payload.put("r18", 0);
            payload.put("ai", 2);
            payload.put("level", "0-6");
            payload.put("proxy", "i.pixiv.re");
            payload.put("full", 0);

            // 添加自定义参数
            if (customParams != null) {
                for (Map.Entry<String, Object> entry : customParams.entrySet()) {
                    payload.put(entry.getKey(), entry.getValue());
                }
            }

            logger.info("发送涩图 API 请求到：{}", apiUrl);
            logger.info("请求 JSON 内容：{}", payload.toString());

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.info("API 响应状态码：{}", responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseText = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseText.append(line.trim());
            }

            logger.info("API 返回内容：{}", responseText);

            return new JSONObject(responseText.toString());
        } catch (Exception e) {
            logger.warn("涩图 API 请求失败：{}", e.getMessage());
            return new JSONObject().put("err", "API请求失败：" + e.getMessage());
        }
    }

    // GET请求
    private JSONObject sendApiGetRequest(Map<String, Object> customParams) {
        try {
            String baseUrl = "https://lolisuki.cn/api/setu/v1";
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?");

            // 先处理tag参数，多个tag用多个tag参数拼接
            if (customParams.containsKey("tag")) {
                Object tagObj = customParams.get("tag");
                if (tagObj instanceof JSONArray) {
                    JSONArray tagArray = (JSONArray) tagObj;
                    for (int i = 0; i < tagArray.length(); i++) {
                        String tagGroup = tagArray.getString(i);
                        urlBuilder.append("tag=").append(URLEncoder.encode(tagGroup, StandardCharsets.UTF_8)).append("&");
                    }
                }
                customParams.remove("tag");
            }

            // 处理其它参数
            for (Map.Entry<String, Object> entry : customParams.entrySet()) {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .append("&");
            }

            // 去掉末尾多余的&
            if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
                urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            }

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            logger.info("API 响应状态码(GET)：{}", responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseText = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseText.append(line.trim());
            }

            logger.info("API 返回内容(GET)：{}", responseText);

            return new JSONObject(responseText.toString());

        } catch (Exception e) {
            logger.warn("涩图 API GET 请求失败：{}", e.getMessage());
            return new JSONObject().put("err", "API请求失败：" + e.getMessage());
        }
    }

    private void handleApiResponse(JSONObject resp, GroupMessage groupMessage) {
        if (resp.has("err")) {
            new SendGroupMessageReply(groupMessage.getGroup_id(), groupMessage.getMessage_id(), resp.getString("err"));
            new Lolisuki(groupMessage);
            return;
        }

        if (resp.getJSONArray("data").isEmpty()) {
            new SendGroupMessageReply(groupMessage.getGroup_id(), groupMessage.getMessage_id(), "没有找到符合条件的涩图哦~");
            new Lolisuki(groupMessage);
            return;
        }

        JSONArray data = resp.getJSONArray("data");
        JSONArray msgChain = new JSONArray();

        for (int i = 0; i < data.length(); i++) {
            JSONObject setu = data.getJSONObject(i);
            msgChain.put(PostBuilder.createText("标题：" + setu.optString("title", "无标题")));
            msgChain.put(PostBuilder.createText("作者：" + setu.optString("author", "匿名")));
            try {
                JSONObject fullUrls = setu.getJSONObject("urls");
                String imageUrl = fullUrls.getString("original");
                msgChain.put(PostBuilder.createImage(imageUrl));
            } catch (Exception e) {
                msgChain.put(PostBuilder.createText("图片加载失败"));
            }
        }


        SendGroupForwardMsg.send(groupMessage.getGroup_id(), msgChain)
                .thenAccept(response -> {
                    if ("ok".equals(response.optString("status"))) {
                        logger.info("涩图发送成功");
                    } else {
                        new SendGroupMessageReply(groupMessage.getGroup_id(), groupMessage.getMessage_id(),"图片发送失败，可能是网络波动或资源已迁移");
                        logger.info("发送失败");
                    }
                });

    }
}
