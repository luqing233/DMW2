package fun.luqing.Plugin;

import fun.luqing.Plugin.Chat.Chat;
import fun.luqing.Plugin.Config.ConfigManager;
import fun.luqing.Plugin.DMPoke.DMPoke;
import fun.luqing.Plugin.Music.Music;
import fun.luqing.Plugin.PublicDMW.GrantTitle;
import fun.luqing.Plugin.PublicDMW.Pool;
import fun.luqing.Plugin.PublicDMW.SetCard;
import fun.luqing.Plugin.SeTu.Lolicon;
import fun.luqing.Plugin.TTS.TTS;
import fun.luqing.Utils.Message.GroupMessage;
import fun.luqing.Utils.Message.Notice;
import fun.luqing.temp.PT;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Plugin {

    /**
     * 返回需要处理的插件任务列表
     */
    public static List<Runnable> getPlugins(JSONObject msg) {
        List<Runnable> plugins = new ArrayList<>();

        // notice 消息
        if (msg.has("post_type") && msg.getString("post_type").equals("notice")) {
            Notice notice = new Notice(msg);

            plugins.add(() -> new  DMPoke(notice));

        }

        // group 消息
        if (msg.has("message_type") && msg.getString("message_type").equals("group")) {
            GroupMessage message = new GroupMessage(msg);

            plugins.add(() -> new PT(message));
            plugins.add(() -> new Lolicon(message));
            plugins.add(() -> new Chat(message));
            plugins.add(() -> new SetCard(message));
            plugins.add(() -> new GrantTitle(message));
            plugins.add(() -> new TTS(message));
            plugins.add(() -> new Music(message));
            plugins.add(() -> new ConfigManager(message));
            plugins.add(() -> new Pool(message));
        }

        return plugins;
    }
}
