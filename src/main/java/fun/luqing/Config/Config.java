package fun.luqing.Config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fun.luqing.DMW2.logger;

public class Config {
    private static volatile Config instance;

    private static final String CONFIG_DIR = "Config";
    private static final String CONFIG_FILE = "./Config/Config.json";

    private final Map<String, Object> values = new LinkedHashMap<>();

    private static final Map<String, Object> DEFAULTS = new LinkedHashMap<>() {{
        put("WS_URL", "ws://localhost:3001");
        put("HTTP_URL", "http://localhost:3000");

        put("GEMINI_PROXY_HOST", "127.0.0.1");
        put("GEMINI_PROXY_PORT", 7897);

        put("AI_CHARACTER", " ");
        put("MAX_MESSAGES", 20);

        put("AI_MODEL", "deepseek");
        put("DEEPSEEK_MODEL", "deepseek-chat");
        put("BIGMODEL_MODEL", "glm-4.5");
        put("GEMINI_MODEL", "gemini-2.5-flash");
        put("GEMINI_API_KEY", "dfk");

        put("TTS_MODEL", "符玄");

        put("master", new JSONArray().put(3253912136L));
    }};

    private boolean TTS_STATUS;

    private Config() {
        loadConfigurations();
        checkTTSStatus();
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    public static synchronized void reload() {
        instance = new Config();
    }

    private void loadConfigurations() {
        Path configPath = Paths.get(CONFIG_FILE);
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));

            JSONObject configData = null;
            if (Files.exists(configPath)) {
                String content = Files.readString(configPath);
                configData = new JSONObject(content);
            }

            for (String key : DEFAULTS.keySet()) {
                if (configData != null && configData.has(key)) {
                    values.put(key, configData.get(key));
                } else {
                    values.put(key, DEFAULTS.get(key));
                }
            }

            saveConfigFile();

        } catch (IOException e) {
            logger.error("加载配置失败，使用默认值: {}", e.toString());
            values.clear();
            values.putAll(DEFAULTS);
            saveConfigFile();
        }
    }

    private void saveConfigFile() {
        Path configPath = Paths.get(CONFIG_FILE);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            JSONObject ordered = new JSONObject();
            for (String key : DEFAULTS.keySet()) {
                Object val = values.getOrDefault(key, DEFAULTS.get(key));
                if (val instanceof Collection) {
                    ordered.put(key, new JSONArray((Collection<?>) val));
                } else if (val instanceof JSONArray) {
                    ordered.put(key, val);
                } else {
                    ordered.put(key, val);
                }
            }
            writer.write(ordered.toString(2));
        } catch (IOException e) {
            logger.error("写入配置失败: {}", e.toString());
        }
    }

    public String getString(String key) {
        return Objects.toString(values.getOrDefault(key, DEFAULTS.get(key)));
    }

    public int getInt(String key) {
        Object val = values.getOrDefault(key, DEFAULTS.get(key));
        return (val instanceof Number) ? ((Number) val).intValue() : Integer.parseInt(val.toString());
    }

    public List<Long> getMasters() {
        Object val = values.get("master");
        List<Long> masters = new ArrayList<>();
        if (val instanceof JSONArray arr) {
            for (int i = 0; i < arr.length(); i++) {
                masters.add(arr.optLong(i, -1L));
            }
        } else if (val instanceof Collection<?> col) {
            for (Object o : col) {
                if (o instanceof Number num) {
                    masters.add(num.longValue());
                } else {
                    try {
                        masters.add(Long.parseLong(o.toString()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return masters;
    }



    public void set(String key, Object value) {
        values.put(key, value);
        saveConfigFile();
    }

    private void checkTTSStatus() {
        try {
            URI uri = URI.create("http://127.0.0.1:8000/api");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            this.TTS_STATUS = (code == 200);
        } catch (IOException e) {
            this.TTS_STATUS = false;
            logger.warn("TTS 服务连接失败: {}", String.valueOf(e));
        }
    }

    public boolean isTTS_STATUS() {
        checkTTSStatus();
        return TTS_STATUS;
    }


    public boolean isMaster(long qq) {
        return getMasters().contains(qq);
    }

    /** 添加主人 */
    public void addMaster(long qq) {
        List<Long> masters = new ArrayList<>(getMasters());
        if (!masters.contains(qq)) {
            masters.add(qq);
            values.put("master", masters);
            saveConfigFile();
        }
    }

    /** 删除主人 */
    public void removeMaster(long qq) {
        List<Long> masters = new ArrayList<>(getMasters());
        if (masters.remove(qq)) {
            values.put("master", masters);
            saveConfigFile();
        }
    }

    /** 批量设置主人（覆盖原有） */
    public void setMasters(Collection<Long> masters) {
        values.put("master", new ArrayList<>(masters));
        saveConfigFile();
    }

}
