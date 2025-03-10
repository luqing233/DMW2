package fun.luqing.ApiConnector.User;

import fun.luqing.ApiConnector.PostBuilder;
import fun.luqing.ApiConnector.PostSender;
import fun.luqing.ApiConnector.Utils.Friend;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static fun.luqing.DMW2.logger;

public class GetFriendList {

    // 异步版本返回Future
    public CompletableFuture<ArrayList<Friend>> getAsync() {
        JSONObject request = PostBuilder.buildBaseMessage("get_friend_list", params -> {
            params.put("no_cache", false);
        });

        return PostSender.send(request)
                .thenApply(record -> {
                    ArrayList<Friend> friends = new ArrayList<>();
                    JSONArray friendsArray = record.getJSONArray("data");
                    for (int i = 0; i < friendsArray.length(); i++) {
                        JSONObject friendObj = friendsArray.getJSONObject(i);
                        friends.add(new Friend(friendObj));
                    }
                    logger.info("好友列表获取成功，状态: {}", record.getString("status"));
                    return friends;
                })
                .exceptionally(e -> {
                    logger.error("获取好友列表失败", e);
                    return new ArrayList<>(); // 返回空列表或根据需求处理
                });
    }

    // 同步版本（会阻塞当前线程）
    public ArrayList<Friend> get() throws Exception {
        JSONObject request = PostBuilder.buildBaseMessage("get_friend_list", params -> {
            params.put("no_cache", false);
        });

        try {
            JSONObject response = PostSender.send(request).join(); // 阻塞等待结果
            ArrayList<Friend> friends = new ArrayList<>();
            JSONArray friendsArray = response.getJSONArray("data");

            for (int i = 0; i < friendsArray.length(); i++) {
                friends.add(new Friend(friendsArray.getJSONObject(i)));
            }

            logger.info("成功获取{}个好友，状态: {}", friends.size(), response.getString("status"));
            return friends;
        } catch (Exception e) {
            logger.error("获取好友列表时发生异常", e);
            throw new Exception("获取好友列表失败", e);
        }
    }
}