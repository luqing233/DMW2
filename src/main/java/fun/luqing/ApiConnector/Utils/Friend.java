package fun.luqing.ApiConnector.Utils;

import org.json.JSONObject;

import java.util.Objects;

public class Friend {
    private final String birthday;
    private final String longNick; // 签名
    private final int age;
    private final String sex;
    private final long user_id;
    private final String nickname;

    public Friend(JSONObject friend) {
        // 格式化生日为YYYY-MM-DD，确保月份和日为两位数
        int year = friend.getInt("birthday_year");
        int month = friend.getInt("birthday_month");
        int day = friend.getInt("birthday_day");
        this.birthday = String.format("%d-%02d-%02d", year, month, day);

        this.longNick = friend.getString("longNick");
        this.age = friend.getInt("age");
        this.sex = friend.getString("sex"); // 修正拼写错误
        this.user_id = friend.getLong("user_id");
        this.nickname = friend.getString("nickname");
    }


    public String getNickname() { return nickname; }
    public long getUser_id() { return user_id; }
    public String getSex() {
        if(Objects.equals(sex, "female")){
            return "女";
        }
        if(Objects.equals(sex, "male")){
            return "男";
        }
        return "未知";
    }
    public int getAge() { return age; }
    public String getLongNick() { return Objects.equals(longNick,"")?"无":longNick; }
    public String getBirthday() { return birthday; }
}