package fun.luqing.Utils.FileManage;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

import static fun.luqing.DMW2.logger;

public class FileUtil {
    private final String directoryPath;

    public FileUtil(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /** ======================== JSON 处理 ======================== */

    /**
     * 读取 JSON 文件并解析为 JSONObject
     *
     * @param fileName 文件名
     * @return 解析后的 JSONObject，如果文件不存在或解析失败，则返回 null
     */
    public JSONObject readJsonFile(String fileName) {
        File file = new File(directoryPath, fileName);
        if (!file.exists()) {
            System.out.println("文件不存在：" + file.getAbsolutePath());
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            return new JSONObject(tokener);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 JSONObject 数据写入 JSON 文件
     *
     * @param fileName 文件名
     * @param jsonData 要写入的 JSONObject 数据
     */
    public void writeJsonFile(String fileName, JSONObject jsonData) {
        File file = new File(directoryPath, fileName);
        ensureParentDirExists(file);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonData.toString(4)); // 格式化输出，缩进 4 空格
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** ======================== YAML 处理 ======================== */

    /**
     * 读取 YAML 文件并解析为 Map
     *
     * @param fileName 文件名
     * @return 解析后的 YAML 数据（Map），如果文件不存在或解析失败，则返回 null
     */
    public Map<String, Object> readYamlFile(String fileName) {
        File file = new File(directoryPath, fileName);
        if (!file.exists()) {
            System.out.println("文件不存在：" + file.getAbsolutePath());
            return null;
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 Map 数据写入 YAML 文件
     *
     * @param fileName 文件名
     * @param yamlData 要写入的 YAML 数据（Map）
     */
    public void writeYamlFile(String fileName, Map<String, Object> yamlData) {
        File file = new File(directoryPath, fileName);
        ensureParentDirExists(file);
        try (FileWriter writer = new FileWriter(file)) {
            Yaml yaml = new Yaml();
            yaml.dump(yamlData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** ======================== 公共方法 ======================== */

    /**
     * 确保父目录存在
     */
    private void ensureParentDirExists(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    /**
     * 删除指定文件
     *
     * @param fileName 文件名（如 "123.json" 或 "config.yaml"）
     * @return 删除成功返回 true，否则返回 false
     */
    public boolean deleteFile(String fileName) {
        File file = new File(directoryPath, fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        logger.info("删除失败，文件不存在：{}", file.getAbsolutePath());
        return false;
    }
}
