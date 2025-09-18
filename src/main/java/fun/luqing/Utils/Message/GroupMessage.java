package fun.luqing.Utils.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class GroupMessage extends RootMessage {

    private final int message_id;             // 消息id
    private final long message_seq;           // 消息序号
    private final String message_type;        // 消息类型
    private final long group_id;              // 群id
    private final String group_name;          // 群名
    private final String nickname;            // 发送者昵称
    private final String card;                // 群昵称
    private final String role;                // 角色（成员/管理员/群主）
    private final String title;               // 头衔
    private final HashSet<String> type;       // 消息类型集合
    private final HashSet<At> at;             // 存储 @ 对象
    private long replyid;                     // 回复 id
    private final String text;                // 正文
    private final ArrayList<Integer> faceid;  // 表情 id
    private final ArrayList<img> image;       // 图片
    private final ArrayList<vid> video;       // 视频


    public static class At {
        private final long qq;
        private final String name;

        public At(JSONObject info) {
            this.qq = info != null ? info.optLong("qq", -1L) : -1L;
            this.name = info != null ? info.optString("name", "") : "";
        }

        public long getQq() {
            return qq;
        }

        public String getName() {
            return name;
        }
    }

    public static class img {
        private final String url;
        private final int subType;

        public img(JSONObject info) {
            this.url = info != null ? info.optString("url", "") : "";
            this.subType = info != null ? info.optInt("subType", 0) : 0;
        }

        public String getUrl() {
            return url;
        }

        public int getSubType() {
            return subType;
        }
    }

    public static class vid {
        private final String url;
        private final String path;

        public vid(JSONObject info) {
            this.url = info != null ? info.optString("url", "") : "";
            this.path = info != null ? info.optString("path", "") : "";
        }

        public String getUrl() {
            return url;
        }

        public String getPath() {
            return path;
        }
    }

    public GroupMessage(JSONObject msg) {
        super(msg);

        this.message_id = msg.optInt("message_id", -1);
        this.message_seq = msg.optLong("message_seq", -1L);
        this.message_type = msg.optString("message_type", "");
        this.group_id = msg.optLong("group_id", -1L);
        this.group_name = msg.optString("group_name", "");

        JSONObject sender = msg.optJSONObject("sender");
        if (sender != null) {
            this.nickname = sender.optString("nickname", "");
            this.card = sender.optString("card", "");
            this.role = sender.optString("role", "");
            this.title = sender.optString("title", "");
        } else {
            this.nickname = "";
            this.card = "";
            this.role = "";
            this.title = "";
        }

        this.type = new HashSet<>();
        this.at = new HashSet<>();
        this.image = new ArrayList<>();
        this.video = new ArrayList<>();
        this.faceid = new ArrayList<>();

        StringBuilder textBuilder = new StringBuilder();
        JSONArray messageArray = msg.optJSONArray("message");
        if (messageArray != null) {
            for (int i = 0; i < messageArray.length(); i++) {
                JSONObject mso = messageArray.optJSONObject(i);
                if (mso == null) continue;

                String type_t = mso.optString("type", "");
                this.type.add(type_t);

                JSONObject data = mso.optJSONObject("data");
                if (data == null) continue;

                switch (type_t) {
                    case "at":
                        this.at.add(new At(data));
                        break;
                    case "reply":
                        this.replyid = data.optLong("id", -1L);
                        break;
                    case "text":
                        textBuilder.append(data.optString("text", ""));
                        break;
                    case "face":
                        this.faceid.add(data.optInt("id", 0));
                        break;
                    case "image":
                        this.image.add(new img(data));
                        break;
                    case "video":
                        this.video.add(new vid(data));
                        break;
                    default:
                        // 忽略未知类型
                }
            }
        }

        this.text = textBuilder.toString();
    }


    public int getMessage_id() {
        return message_id;
    }

    public long getMessage_seq() {
        return message_seq;
    }

    public String getMessage_type() {
        return message_type;
    }

    public long getGroup_id() {
        return group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCard() {
        return card;
    }

    public String getRole() {
        return role;
    }

    public String getTitle() {
        return title;
    }

    public HashSet<String> getType() {
        return type;
    }

    public HashSet<At> getAt() {
        return at;
    }

    public long getReplyid() {
        return replyid;
    }

    public String getText() {
        return text.strip();
    }

    public ArrayList<Integer> getFaceid() {
        return faceid;
    }

    public ArrayList<img> getImage() {
        return image;
    }

    public ArrayList<vid> getVideo() {
        return video;
    }
}
