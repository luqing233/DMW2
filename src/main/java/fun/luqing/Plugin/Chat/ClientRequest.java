package fun.luqing.Plugin.Chat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
            httpPost.setHeader("Content-Type", "application/json");

            // 设置请求体
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            // 解析响应内容
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "请求失败";
        }
    }
}
