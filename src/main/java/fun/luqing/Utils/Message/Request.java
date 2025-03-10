package fun.luqing.Utils.Message;

import org.json.JSONObject;

public class Request extends RootMessage {


    private final String post_type;
    private final String request_type;
    private final String sub_type;
    private final String comment;
    private final String flag;
    private final long group_id;

    public Request(JSONObject msg) {
        super(msg);
        this.post_type = msg.getString("post_type");
        this.request_type = msg.getString("request_type");
        this.sub_type = msg.getString("sub_type");
        this.comment = msg.getString("comment");
        this.flag = msg.getString("flag");
        this.group_id = msg.getLong("group_id");
    }

    public String getPost_type() {
        return post_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public String getSub_type() {
        return sub_type;
    }

    public String getComment() {
        return comment;
    }

    public String getFlag() {
        return flag;
    }

    public long getGroup_id() {
        return group_id;
    }
}
