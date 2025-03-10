package fun.luqing.ApiConnector;

import fun.luqing.WebSocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class PostSender {

    public static CompletableFuture<JSONObject> send(JSONObject message) {
        return WebSocket.getInstance().sendMessage(message.toString());
    }

    public static CompletableFuture<JSONObject> sendGroupMessage(long groupId, JSONArray messageArray) {
        JSONObject message = PostBuilder.buildBaseMessage("send_group_msg", params -> {
            params.put(PostBuilder.GROUP_ID, groupId);
            params.put(PostBuilder.MESSAGE, messageArray);
        });

        return send(message);
    }

    public static CompletableFuture<JSONObject> sendPost(JSONObject message) {
        return send(message);
    }

}
