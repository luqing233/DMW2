package fun.luqing.Config;

import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fun.luqing.DMW2.logger;

public class Config {
    private static volatile Config instance;

    private String WS_URL;
    private String HTTP_URL;
    private String AI_CHARACTER;
    private int MAX_MESSAGES;

    private String AI_MODEL;

    private String DEEPSEEK_MODEL;
    private String BIGMODEL_MODEL;
    private String TTS_MODEL;

    private boolean TTS_STATUE;


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
        defaultConfig.put("AI_MODEL", "deepseek");
        defaultConfig.put("DEEPSEEK_MODEL", "deepseek-chat");
        defaultConfig.put("BIGMODEL_MODEL", "glm-4.5");
        defaultConfig.put("TTS_MODEL", "符玄");

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
            AI_MODEL = configData.optString("AI_MODEL", "deepseek");
            DEEPSEEK_MODEL = configData.optString("DEEPSEEK_MODEL", "deepseek-chat");
            BIGMODEL_MODEL = configData.optString("BIGMODEL_MODEL", "glm-4.5");
            TTS_MODEL = configData.optString("TTS_MODEL", "符玄");
        } catch (IOException e) {
            logger.info(e.getMessage());
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        WS_URL = "ws://localhost:3001";
        HTTP_URL = "http://localhost:3000";
        AI_CHARACTER = " ";
        MAX_MESSAGES = 20;
        AI_MODEL = "deepseek";
        DEEPSEEK_MODEL = "deepseek-chat";
        BIGMODEL_MODEL = "glm-4.5";
        TTS_MODEL = "符玄";
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

    public String getTTS_MODEL(){
        return TTS_MODEL;
    }

    // Setters with file update
    public void setWS_URL(String WS_URL) {
        this.WS_URL = WS_URL;
        updateConfigFile("WS_URL", WS_URL);
    }

    public void setHTTP_URL(String HTTP_URL) {
        this.HTTP_URL = HTTP_URL;
        updateConfigFile("HTTP_URL", HTTP_URL);
    }

    public void setAI_CHARACTER(String AI_CHARACTER) {
        this.AI_CHARACTER = AI_CHARACTER;
        updateConfigFile("AI_CHARACTER", AI_CHARACTER);
    }

    public void setMAX_MESSAGES(int MAX_MESSAGES) {
        this.MAX_MESSAGES = MAX_MESSAGES;
        updateConfigFile("MAX_MESSAGES", MAX_MESSAGES);
    }

    public void setDEEPSEEK_MODEL(String DEEPSEEK_MODEL) {
        this.DEEPSEEK_MODEL = DEEPSEEK_MODEL;
        updateConfigFile("DEEPSEEK_MODEL", DEEPSEEK_MODEL);
    }

    public void setTTS_MODEL(String TTS_MODEL) {
        this.TTS_MODEL = TTS_MODEL;
        updateConfigFile("TTS_MODEL", TTS_MODEL);
    }

    public String getAI_MODEL() {
        return AI_MODEL;
    }

    public void setAI_MODEL(String AI_MODEL) {
        this.AI_MODEL = AI_MODEL;
        updateConfigFile("AI_MODEL", AI_MODEL);
    }

    // Method to update the JSON config file
    private void updateConfigFile(String key, Object value) {
        Path configPath = Paths.get(CONFIG_FILE);
        try {
            JSONObject configData = new JSONObject(new String(Files.readAllBytes(configPath)));
            configData.put(key, value);

            try (Writer writer = Files.newBufferedWriter(configPath)) {
                writer.write(configData.toString(2)); // 格式化输出
            }
        } catch (IOException e) {
            logger.error(e.toString());

        }
    }


    public String getBIGMODEL_MODEL() {
        return BIGMODEL_MODEL;
    }

    public void setBIGMODEL_MODEL(String BIGMODEL_MODEL) {
        this.BIGMODEL_MODEL = BIGMODEL_MODEL;
    }
}
