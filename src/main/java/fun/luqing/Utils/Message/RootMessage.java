package fun.luqing.Utils.Message;

import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RootMessage {
    private final long self_id; // bot qq
    private final long user_id;//发送者
    private final long time;

    public RootMessage(JSONObject msg) {
        self_id = msg.getLong("self_id");
        time = msg.getLong("time");

        // 兼容 user_id 可能为 int 或 long
        Number userIdNumber = msg.optNumber("user_id", 0); // 默认为 0，防止 NullPointerException
        user_id = userIdNumber.longValue();
    }

    public String getTime() {
        Instant instant = Instant.ofEpochSecond(time);
        // 将 Instant 转换为 LocalDateTime 对象
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC+8"));
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public long getUser_id() {
        return user_id;
    }

    public long getSelf_id() {
        return self_id;
    }
}
