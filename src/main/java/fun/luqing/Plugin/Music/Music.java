package fun.luqing.Plugin.Music;

import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageRecord;
import fun.luqing.ApiConnector.Message.GroupChat.SendGroupMessageReply;
import fun.luqing.Utils.Message.GroupMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fun.luqing.DMW2.logger;

public class Music {
    private static final Logger LOGGER = Logger.getLogger(Music.class.getName());
    private static final String MUSIC_DIR = "music/";

    public Music(GroupMessage message) {
        try {
            if (message.getText().startsWith("点歌")) {
                handleSongRequest(message.getMessage_id(), message.getGroup_id(), message.getUser_id() ,message.getText());
            } else if (message.getText().startsWith("选择")) {
                handleSongSelection(message.getMessage_id(), message.getGroup_id(), message.getUser_id() ,message.getText());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理消息时发生错误", e);
        }
    }

    private void handleSongRequest(int messageId, long groupId, long userId, String message) {
        String name = message.replace("点歌", "").trim();
        logger.info("接收到点歌指令{}",name);
        //System.out.println(ANSI_GREEN + "接收到点歌指令-> " + name + ANSI_RESET);
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);

        MusicSearch musicSearch = new MusicSearch("NyBm8w86Qd52B1nb");
        Map.Entry<String, Map<Integer, Long>> result = musicSearch.getSongData(encodedName);

        String songs = result.getKey();
        System.out.println("获取歌曲信息:\n" + songs);

        new SendGroupMessageReply(groupId,messageId,songs);



        MusicData record = MusicData.getInstance();
        record.removeUser(userId);
        record.addUserGroup(userId, result.getValue());
    }

    private void handleSongSelection(int messageId, long groupId, long userId, String message) {
        try {
            int id = Integer.parseInt(message.replace("选择", "").trim());
            //System.out.println(ANSI_GREEN + "接收到点歌选择指令-> 选择" + id + ANSI_RESET);
            MusicData record = MusicData.getInstance();
            Map<Integer, Long> songMap = record.getUserLongs(userId);

            if (songMap != null && songMap.containsKey(id)) {
                long songId = songMap.get(id);
                String songUrl = "http://music.163.com/song/media/outer/url?id=" + songId;

                // 下载歌曲并保存
                if (saveSongToFile(songId, songUrl)) {
                    new SendGroupMessageRecord().send(groupId, songUrl);
                } else {
                    sendGroupMessageAt(userId, groupId, messageId, "你是不是点VIP歌了＞﹏＜");
                }
            } else {
                sendGroupMessageAt(userId, groupId, messageId, "你序号对吗＞﹏＜");
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "歌曲ID格式无效: " + message, e);
            sendGroupMessageAt(userId, groupId, messageId, "歌曲ID格式无效");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理歌曲选择时发生错误", e);
            sendGroupMessageAt(userId, groupId, messageId, "处理歌曲选择时发生错误");
        }
    }

    private boolean saveSongToFile(long songId, String songUrl) {
        try (InputStream inputStream = new URL(songUrl).openStream();
             FileOutputStream outputStream = new FileOutputStream(createFile(songId))) {

            HttpURLConnection connection = (HttpURLConnection) new URL(songUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                //System.out.println(ANSI_GREEN + "歌曲已保存到文件: " + new File(MUSIC_DIR, songId + ".mp3").getAbsolutePath() + ANSI_RESET);
                return true;
            } else {
                LOGGER.warning("无法下载歌曲, HTTP 响应码: " + responseCode);
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "保存歌曲到文件时发生错误", e);
            return false;
        }
    }

    private File createFile(long songId) throws IOException {
        File musicDir = new File(MUSIC_DIR);
        if (!musicDir.exists() && !musicDir.mkdirs()) {
            throw new IOException("无法创建音乐目录");
        }
        return new File(musicDir, songId + ".mp3");
    }

    private void sendGroupMessageAt(long userId, long groupId, int messageId, String message) {
        new SendGroupMessageReply(groupId,messageId,message,userId);
    }
}