package fun.luqing.Utils.Message;

import org.json.JSONObject;

public class Notice extends RootMessage{

    private final String post_type;
    private final String notice_type;
    private final String sub_type;
    private final long target_id;//被戳的人
    private final long operator_id;
    private final long group_id;
    private final String raw_info;


    public Notice(JSONObject msg) {
        super(msg);
        this.post_type = msg.getString("post_type");
        this.notice_type = msg.getString("notice_type");
        this.sub_type = msg.optString("sub_type");
        this.target_id = msg.optLong("target_id");
        this.operator_id = msg.optLong("operator_id");
        this.group_id = msg.getLong("group_id");
        this.raw_info = msg.optString("raw_info");

    }

    public String getPost_type() {
        return post_type;
    }

    public String getNotice_type() {
        return notice_type;
    }

    public String getSub_type() {
        return sub_type;
    }

    public long getTarget_id() {
        return target_id;
    }

    public long getGroup_id() {
        return group_id;
    }

    public String getRaw_info() {
        return raw_info;
    }

    public long getOperator_id() {
        return operator_id;
    }
}
