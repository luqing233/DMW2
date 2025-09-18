package fun.luqing.Plugin.Chat;

import fun.luqing.Config.Config;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static fun.luqing.DMW2.logger;

public class ClientRequest {

    /**
     * 发送 POST 请求
     * @param url 请求的 URL
     * @param apiKey API Key
     * @param jsonBody 请求的 JSON 数据
     * @return 返回响应内容
     */
    public static String sendPostRequest(String url, String apiKey, String jsonBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            return getString(jsonBody, httpClient, httpPost);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "请求失败";
        }
    }

    /**
     * 发送 POST 请求 (Gemini 专用，带代理)
     * @param url 请求的 URL
     * @param apiKey API Key
     * @param jsonBody 请求的 JSON 数据
     * @return 返回响应内容
     */
    public static String sendPostRequestWithQueryParam(String url, String apiKey, String jsonBody) {
        try {
            // 从 Config 读取代理配置（也可以写死）
            String proxyHost = Config.getInstance().getString("GEMINI_PROXY_HOST");
            int proxyPort = Config.getInstance().getInt("GEMINI_PROXY_PORT");

            HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setProxy(proxy) // 只 Gemini 走代理
                    .build()) {

                String finalUrl = url + "?key=" + apiKey;
                HttpPost httpPost = new HttpPost(finalUrl);

                return getString(jsonBody, httpClient, httpPost);
            }
        } catch (Exception e) {
            logger.error("Gemini 请求失败", e);
            return "请求失败";
        }
    }

    @Nullable
    private static String getString(String jsonBody, CloseableHttpClient httpClient, HttpPost httpPost) throws IOException {
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return EntityUtils.toString(entity);
        }
        return null;
    }

}
