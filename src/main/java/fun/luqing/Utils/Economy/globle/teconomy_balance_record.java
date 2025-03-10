package fun.luqing.Utils.Economy.globle;

public class teconomy_balance_record {

    private String context;  // context 字段
    private String currency; // currency 字段
    private String uuid;     // uuid 字段
    private double balance;  // balance 字段
    private long latest;     // latest 字段

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getLatest() {
        return latest;
    }

    public void setLatest(long latest) {
        this.latest = latest;
    }

    public long getId() {
        if (uuid != null && uuid.startsWith("u")) {
            return Long.parseLong(uuid.substring(1));  // 去掉 "U+" 前缀并转换为 long
        }
        throw new IllegalArgumentException("Invalid UUID format");
    }
}
