package fun.luqing.Plugin.Ruling;

import fun.luqing.ApiConnector.Group.GetGroupMemberList;
import fun.luqing.ApiConnector.Utils.GroupMember;
import fun.luqing.Utils.Message.GroupMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ruling {

    // 存放当前正在投票的群ID及其投票数据
    private static final Map<Long, VotingSession> votingSessions = new ConcurrentHashMap<>();

    // 禁止重复触发裁决的时间窗口(单位秒)，比如 300秒(5分钟)
    private static final int VOTING_DURATION_SECONDS = 300;

    public Ruling(GroupMessage message) throws Exception {
        long groupId = message.getGroup_id();

        // 判断是否已有投票活动进行，防止重复触发
        if (votingSessions.containsKey(groupId)) {
            // 当前群投票正在进行，拒绝新触发或提醒
            // 可以发送提示消息，这里用System.out代替
            System.out.println("群 " + groupId + " 投票尚未结束，无法重复发起裁决。");
            return;
        }

        if (message.getType().contains("at") && message.getText().startsWith("裁决")) {
            // 1. 获取群成员
            GetGroupMemberList getGroupMemberList = new GetGroupMemberList();
            List<GroupMember> members = getGroupMemberList.get(groupId);

            // 2. 创建新的投票会话
            VotingSession session = new VotingSession(groupId, members);

            // 3. 放入活动Map中，代表投票开始
            votingSessions.put(groupId, session);

            // 4. 开启计时器，结束后输出结果并清理
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                VotingSession finishedSession = votingSessions.remove(groupId);
                if (finishedSession != null) {
                    String result = finishedSession.getResult();
                    // 这里可以用消息接口发回群，暂时用打印代替
                    System.out.println("群 " + groupId + " 裁决投票结束，结果：\n" + result);
                }
                scheduler.shutdown();
            }, VOTING_DURATION_SECONDS, TimeUnit.SECONDS);

            // 提示投票开始
            System.out.println("群 " + groupId + " 裁决投票开始，发消息“1”同意，“0”否决，有效时间 " + VOTING_DURATION_SECONDS + " 秒。");
        }
    }

    /**
     * 处理投票消息，消息格式为 "1" 或 "0"
     * 该方法应在外部消息接收入口调用，传入收到的消息
     */
    public static void handleVoteMessage(GroupMessage message) {
        long groupId = message.getGroup_id();
        long userId = message.getUser_id();
        String text = message.getText().trim();

        // 如果该群没有投票进行，直接返回
        if (!votingSessions.containsKey(groupId)) {
            return;
        }

        VotingSession session = votingSessions.get(groupId);

        // 只有“1”或“0”视为有效投票
        if ("1".equals(text) || "0".equals(text)) {
            session.vote(userId, text.equals("1"));
            // 可选：即时反馈收到投票
            System.out.println("收到群 " + groupId + " 用户 " + userId + " 的投票：" + text);
        }
    }

    /**
     * 内部类管理投票数据
     */
    private static class VotingSession {
        private final long groupId;
        private final List<GroupMember> members;

        // 用户投票Map，key为用户ID，value为投票true=同意，false=否决
        private final Map<Long, Boolean> votes = new ConcurrentHashMap<>();

        public VotingSession(long groupId, List<GroupMember> members) {
            this.groupId = groupId;
            this.members = members;
        }

        public void vote(long userId, boolean agree) {
            boolean isMember = members.stream().anyMatch(m -> m.getUser_id() == userId);
            if (!isMember) return;

            if (votes.containsKey(userId)) {
                System.out.println("用户 " + userId + " 已经投过票，忽略重复投票");
                return;
            }

            votes.put(userId, agree);
        }


        public String getResult() {
            long agreeCount = votes.values().stream().filter(v -> v).count();
            long rejectCount = votes.values().stream().filter(v -> !v).count();
            int totalMembers = members.size();
            int totalVotes = votes.size();

            StringBuilder sb = new StringBuilder();
            sb.append("总群成员: ").append(totalMembers).append("\n");
            sb.append("参与投票人数: ").append(totalVotes).append("\n");
            sb.append("同意票: ").append(agreeCount).append("\n");
            sb.append("否决票: ").append(rejectCount).append("\n");
            sb.append("投票详情:\n");

            for (GroupMember member : members) {
                long uid = member.getUser_id();
                String voteStr = votes.containsKey(uid) ? (votes.get(uid) ? "同意" : "否决") : "未投票";
                sb.append(member.getNickname()).append(" (").append(uid).append("): ").append(voteStr).append("\n");
            }
            return sb.toString();
        }
    }
}
