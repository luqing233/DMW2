package fun.luqing.Utils.Message;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashSet;




public class GroupMessage extends RootMessage {

    private final int message_id;//消息id
    private final long message_seq;
    private final String message_type;//消息类型
    private final long group_id;//群id
    private final String nickname;//发送者昵称
    private final String card;//发送者群昵称
    private final String role;//发送者角色（成员，管理员，群主）
    private final String title;//发送者头衔
    private final HashSet<String> type;
    private final HashSet<At> at;//存储艾特的对象
    private long replyid;//存储回复的id(如果有)
    private final String text;//存储正文
    private ArrayList<Integer> faceid;//存储表情id(如果有)
    private final ArrayList<img> image;//存储image
    private ArrayList<vid> video;//存储video


    public static class At{
        private final long qq;
        private final String name;
        public At(JSONObject info){
            qq = info.optNumber("qq").longValue();
            name = info.getString("name");
        }
        public long getQq() {
            return qq;
        }

        public String getName() {
            return name;
        }
    }

    public static class img{
        private final String url;
        private final int subType;
        public img(JSONObject info){
            url = info.getString("url");
            subType = info.getInt("subType");
        }

        public String getUrl() {
            return url;
        }

        public int getSubType() {
            return subType;
        }
    }
    public static class vid{
        private final String url;
        private final String path;
        public vid(JSONObject info){
            url = info.getString("url");
            path = info.getString("path");
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
        this.message_id = msg.getInt("message_id");
        this.message_seq = msg.getLong("message_seq");
        this.message_type = msg.getString("message_type");
        this.group_id = msg.getLong("group_id");
        JSONObject sender = msg.getJSONObject("sender");
        this.nickname = sender.getString("nickname");
        this.card = sender.getString("card");
        this.role = sender.getString("role");
        this.title = sender.getString("title");

        this.type = new HashSet<>();
        this.at = new HashSet<>();
        this.image = new ArrayList<>();
        this.video = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        JSONArray message = msg.getJSONArray("message");
        for(int i = 0; i < message.length(); i++){
            JSONObject mso = message.getJSONObject(i);
            String type_t = mso.getString("type");
            this.type.add(type_t);
            switch (type_t) {
                case "at":
                    at.add(new At(mso.getJSONObject("data")));
                    break;
                case "reply":
                    replyid = mso.getJSONObject("data").getLong("id");
                    break;
                case "text":
                    textBuilder.append(mso.getJSONObject("data").getString("text"));
                    break;
                case "image":
                    image.add(new img(mso.getJSONObject("data")));
                    break;
                case "video":
                    video.add(new vid(mso.getJSONObject("data")));
                    break;
                default:
            }

        }
        this.text = textBuilder.toString();


    }

    public int getMessage_id() {
        return message_id;
    }

    public ArrayList<vid> getVideo() {
        return video;
    }

    public ArrayList<img> getImage() {
        return image;
    }

    public ArrayList<Integer> getFaceid() {
        return faceid;
    }

    public String getText() {
        return text.strip();
    }

    public long getReplyid() {
        return replyid;
    }

    public HashSet<At> getAt() {
        return at;
    }

    public HashSet<String> getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getRole() {
        return role;
    }

    public String getCard() {
        return card;
    }

    public String getNickname() {
        return nickname;
    }

    public long getGroup_id() {
        return group_id;
    }

    public String getMessage_type() {
        return message_type;
    }

    public long getMessage_seq() {
        return message_seq;
    }
}
