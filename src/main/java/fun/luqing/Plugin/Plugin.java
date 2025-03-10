package fun.luqing.Plugin;

import fun.luqing.Plugin.DeepSeek.DeepSeek;
import fun.luqing.Plugin.Music.Music;
import fun.luqing.Plugin.PublicDMW.GrantTitle;
import fun.luqing.Plugin.PublicDMW.SetCard;
import fun.luqing.Plugin.TTS.TTS;
import fun.luqing.Utils.Message.GroupMessage;
import fun.luqing.Utils.Message.Notice;
import fun.luqing.temp.PT;
import org.json.JSONObject;

public class Plugin {

    public Plugin(String msg_s) {
        JSONObject msg = new JSONObject(msg_s);
        register(msg);
    }
    private void register(JSONObject msg) {
        if (msg.has("post_type")&&msg.getString("post_type").equals("notice")) {
            Notice notice =new Notice(msg);


        }
        if (msg.has("message_type")&&msg.getString("message_type").equals("group")) {
            GroupMessage message=new GroupMessage(msg);
            new PT(message);
            new DeepSeek(message);
            new SetCard(message);
            new GrantTitle(message);
            new TTS(message);
            new Music(message);


        }
    }


}
