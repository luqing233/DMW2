package fun.luqing.Config;

import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static volatile Config instance;

    private String WS_URL;
    private String HTTP_URL;
    private String AI_CHARACTER;
    private int MAX_MESSAGES;
    private String DEEPSEEK_MODEL;

    private static final String CONFIG_DIR = "Config";
    private static final String CONFIG_FILE = "./Config/Config.json";

    private Config() {
        loadConfigurations();
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
            if (!Files.exists(configPath)) {
                createDefaultConfigFile(configPath);
            }
            parseConfigFile(configPath);
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultValues();
        }
    }

    private void createDefaultConfigFile(Path configPath) {
        JSONObject defaultConfig = new JSONObject();
        defaultConfig.put("WS_URL", "ws://localhost:3001");
        defaultConfig.put("HTTP_URL", "http://localhost:3000");
        defaultConfig.put("AI_CHARACTER", " ");
        defaultConfig.put("MAX_MESSAGES", 20);
        defaultConfig.put("DEEPSEEK_MODEL", "deepseek-chat");

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            writer.write(defaultConfig.toString(2)); // 格式化输出
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultValues();
        }
    }

    private void parseConfigFile(Path configPath) {
        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            JSONObject configData = new JSONObject(content.toString());

            WS_URL = configData.optString("WS_URL", "ws://localhost:3001");
            HTTP_URL = configData.optString("HTTP_URL", "http://localhost:3000");
            AI_CHARACTER = configData.optString("AI_CHARACTER", " ");
            MAX_MESSAGES = configData.optInt("MAX_MESSAGES", 20);
            DEEPSEEK_MODEL = configData.optString("DEEPSEEK_MODEL", "deepseek-chat");
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        WS_URL = "ws://localhost:3001";
        HTTP_URL = "http://localhost:3000";
        AI_CHARACTER = " ";
        MAX_MESSAGES = 20;
        DEEPSEEK_MODEL = "deepseek-chat";
    }

    public String getWS_URL() {
        return WS_URL;
    }

    public String getHTTP_URL() {
        return HTTP_URL;
    }

    public String getAI_CHARACTER() {
        return AI_CHARACTER;
    }

    public int getMAX_MESSAGES() {
        return MAX_MESSAGES;
    }

    public String getDEEPSEEK_MODEL() {
        return DEEPSEEK_MODEL;
    }
}