package fun.luqing.ApiConnector.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class GroupMember {
    private final long group_id;
    private final long user_id;
    private final String nickname;
    private final String card;
    private final String sex;
    private final int age;
    private final String area;
    private final String level;
    private final int qq_level;
    private final long join_time;       // 时间戳
    private final long last_sent_time;  // 时间戳
    private final long title_expire_time; // 时间戳
    private final boolean unfriendly;
    private final boolean card_changeable;
    private final boolean is_robot;
    private final long shut_up_timestamp; // 时间戳
    private final String role;
    private final String title;

    public GroupMember(JSONObject json) {
        this.group_id = json.getLong("group_id");
        this.user_id = json.getLong("user_id");
        this.nickname = json.getString("nickname");
        this.card = json.getString("card");
        this.sex = json.getString("sex");
        this.age = json.getInt("age");
        this.area = json.getString("area");
        this.level = json.getString("level");
        this.qq_level = json.getInt("qq_level");
        this.join_time = json.getLong("join_time");
        this.last_sent_time = json.getLong("last_sent_time");
        this.title_expire_time = json.getLong("title_expire_time");
        this.unfriendly = json.getBoolean("unfriendly");
        this.card_changeable = json.getBoolean("card_changeable");
        this.is_robot = json.getBoolean("is_robot");
        this.shut_up_timestamp = json.getLong("shut_up_timestamp");
        this.role = json.getString("role");
        this.title = json.getString("title");
    }

    // 时间戳转日期字符串，格式为 yyyy-MM-dd HH:mm:ss
    private String timestampToDate(long timestamp) {
        if (timestamp <= 0) return "无";
        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public long getGroup_id() {
        return group_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCard() {
        return card.isEmpty() ? "无" : card;
    }

    public String getSex() {
        if (Objects.equals(sex, "female")) return "女";
        if (Objects.equals(sex, "male")) return "男";
        return "未知";
    }

    public int getAge() {
        return age;
    }

    public String getArea() {
        return area.isEmpty() ? "未知" : area;
    }

    public String getLevel() {
        return level.isEmpty() ? "0" : level;
    }

    public int getQq_level() {
        return qq_level;
    }

    public String getJoin_time() {
        return timestampToDate(join_time);
    }

    public String getLast_sent_time() {
        return timestampToDate(last_sent_time);
    }

    public String getTitle_expire_time() {
        return timestampToDate(title_expire_time);
    }

    public boolean isUnfriendly() {
        return unfriendly;
    }

    public boolean isCard_changeable() {
        return card_changeable;
    }

    public boolean isRobot() {
        return is_robot;
    }

    public String getShut_up_timestamp() {
        return timestampToDate(shut_up_timestamp);
    }

    public String getRole() {
        return role.isEmpty() ? "无" : role;
    }

    public String getTitle() {
        return title.isEmpty() ? "无" : title;
    }
}
