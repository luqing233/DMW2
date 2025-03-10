package fun.luqing.Plugin.Music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MusicSearch {

    private final String apiToken;

    // 构造函数
    public MusicSearch(String apiToken) {
        this.apiToken = apiToken;
    }

    /**
     * 从 API 获取歌曲数据并存储在 Map 和格式化的字符串中
     *
     * @param name 歌曲名称
     * @return 一个包含格式化字符串和歌曲 ID 的 Map
     */
    public Map.Entry<String, Map<Integer, Long>> getSongData(String name) {
        String urlString = "https://v2.alapi.cn/api/music/search?limit=10&keyword=" +name + "&token=" + apiToken;
        Map<Integer, Long> songMap = new HashMap<>();
        StringBuilder formattedString = new StringBuilder();

        try {
            HttpURLConnection connection = setupConnection(urlString);
            String response = getResponse(connection);

            JSONObject jsonObject = new JSONObject(response);
            JSONArray songsArray = jsonObject.getJSONObject("data").getJSONArray("songs");

            for (int i = 0; i < songsArray.length(); i++) {
                JSONObject songObject = songsArray.getJSONObject(i);
                long id = songObject.getLong("id");
                String songName = songObject.getString("name");
                String artistName = songObject.getJSONArray("artists").getJSONObject(0).getString("name");
                songMap.put(i + 1, id);
                formattedString.append(i + 1).append(": ").append(songName).append("\n       ").append(artistName).append("\n");
            }

        } catch (Exception e) {
            formattedString.append("获取数据时出错: ").append(e.getMessage());
        }

        return Map.entry(formattedString.toString().trim(), songMap);
    }

    private HttpURLConnection setupConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 设置连接超时
        connection.setReadTimeout(5000);    // 设置读取超时
        return connection;
    }

    private String getResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }
}
