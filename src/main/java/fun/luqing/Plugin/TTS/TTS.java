package fun.luqing.Plugin.TTS;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageRecord;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Utils.Message.GroupMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static fun.luqing.DMW2.logger;
import static fun.luqing.Utils.Else.NumberToChineseConverter.convertNumbersToChinese;

public class TTS {

    private static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    static {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public TTS(GroupMessage message) {
        String text = message.getText();
        Long groupId = message.getGroup_id();

        String trigger = " 说";
        int idx = text.indexOf(trigger);
        if (idx > 0) {
            String model   = text.substring(0, idx).trim();
            String content = text.substring(idx + trigger.length()).trim();
            content=convertNumbersToChinese(content);
            //enqueueTask(content, groupId, model);
            String url = speak(content,model);
            if (!url.equals("0") && !url.equals("-1")) {
                new SendGroupMessageRecord().send(groupId, url);
            }else if(url.equals("-1")){
                new SendGroupMessageReply(groupId,message.getMessage_id(),"语音合成服务未开启");
            }
            else {
                logger.warn("缺少模型 {}",model);
                new SendGroupMessageReply(groupId,message.getMessage_id(),"还没有"+model+"这个模型哦");
            }
        }
    }

    public static String speak(String text, String model) {
        String[] fallbackVersions = {"v4", "v2Pro","v2ProPlus", "v2"};
        logger.info("使用模型 {} 合成 ⌈{}⌋ ",model,text);

        for (String version : fallbackVersions) {
            JSONObject json = getJsonObject(text, model, version);


            try {
                String resp = sendPostRequest(
                        "http://127.0.0.1:8000/infer_single",
                        json.toString());

                String url = new JSONObject(resp).optString("audio_url", "");
                if (url.isBlank()) {                         // 拿到响应但字段缺失
                    throw new RuntimeException("返回中无音频链接");
                }

                //logger.info("{}:{}",model,url);

                return url.replace("http://0.0.0.0:8000/outputs/", "E:\\SoVITS\\GPT-SoVITS-0725-cu124\\outputs\\");// 成功！
            } catch (java.net.ConnectException
                   | java.net.SocketTimeoutException
                   | java.net.UnknownHostException ioEx) {

                logger.error("无法连接本地 TTS 服务: {}", ioEx.getMessage());
                return "-1";
            } catch (Exception e) {
                logger.info("使用版本 {} 失败，尝试下一个…", version);
            }
        }
        return "0";
    }


    private static JSONObject getJsonObject(String text, String model, String version) {
        JSONObject json = new JSONObject();
        json.put("model_name", model);
        json.put("prompt_text_lang", "中文");
        json.put("emotion", "默认");
        json.put("text", text);
        json.put("text_lang", "中文");
        json.put("top_k", 10);
        json.put("top_p", 1);
        json.put("temperature", 1);
        json.put("text_split_method", "按标点符号切");
        json.put("batch_size", 10);
        json.put("batch_threshold", 0.75);
        json.put("split_bucket", true);


        if(model.equals("银狼")) {
            json.put("speed_facter", 0.95);
        }else {
            json.put("speed_facter", 1);
        }


        json.put("fragment_interval", 0.3);
        json.put("media_type", "wav");
        json.put("parallel_infer", true);
        json.put("repetition_penalty", 1.35);
        json.put("seed", -1);
        json.put("sample_steps", 16);
        json.put("if_sr", false);
        json.put("access_token", "");
        json.put("version", version);
        return json;
    }


    public static String sendPostRequest(String urlString, String jsonInput) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();
        }
    }
}
