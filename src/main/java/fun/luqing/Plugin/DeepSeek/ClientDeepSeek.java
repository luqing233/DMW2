package fun.luqing.Plugin.DeepSeek;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static fun.luqing.DMW2.logger;

public class ClientDeepSeek {

    /**
     * 发送POST请求到DashScope API
     * @param apiUrl API地址
     * @param apiKey 认证密钥
     * @param jsonBody 请求体JSON字符串
     * @return API响应内容 或 null（请求失败时）
     */
    public static String sendRequest(String apiUrl, String apiKey, String jsonBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);

            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            // 设置请求体
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));
            logger.info("等待DeepSeek回复");

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            // 处理响应内容
            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                System.out.println(result);
                return result;
            }
            return null;
        } catch (Exception e) {
            logger.error( "与DeepSeek链接出错",e);
            return null;
        }
    }
}