package fun.luqing.Plugin.Music;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicData {
    // 存储用户 ID 及其对应的 long 值和编号
    private final Map<Long, Map<Integer, Long>> userLongsMap = new ConcurrentHashMap<>();

    // 私有构造函数
    private MusicData() {
        // 初始化数据
    }


    // 静态内部类
    private static class Holder {
        private static final MusicData INSTANCE = new MusicData();
    }

    // 提供公共静态方法获取单例实例
    public static MusicData getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 添加用户 ID 及其对应的 long 值和编号
     *
     * @param userId 用户 ID
     * @param longMap 对应的 long 值和编号的映射
     */
    public void addUserGroup(long userId, Map<Integer, Long> longMap) {
        userLongsMap.put(userId, new ConcurrentHashMap<>(longMap));
    }

    /**
     * 删除用户 ID 及其对应的所有数据
     *
     * @param userId 用户 ID
     */
    public void removeUser(long userId) {
        userLongsMap.remove(userId);
    }

    /**
     * 获取用户 ID 对应的所有 long 值和编号
     *
     * @param userId 用户 ID
     * @return 对应的 long 值和编号的映射，或 null 如果用户 ID 不存在
     */
    public Map<Integer, Long> getUserLongs(long userId) {
        return userLongsMap.get(userId);
    }

    /**
     * 打印所有数据
     */
    public void printAllData() {
        userLongsMap.forEach((userId, longMap) -> {
            System.out.println("User ID: " + userId);
            longMap.forEach((number, value) ->
                    System.out.println("  Number: " + number + ", Value: " + value));
        });
    }
}
