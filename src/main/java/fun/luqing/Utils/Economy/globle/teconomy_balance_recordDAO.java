/*
package fun.luqing.Utils.Economy.globle;

import fun.luqing.DataSource.DataSourceManager ;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class teconomy_balance_recordDAO {

    // 随机选择 n 条记录
    public List<teconomy_balance_record> selectRandom(int victim) throws SQLException {
        List<teconomy_balance_record> list = new ArrayList<>();
        String sql = "SELECT * FROM economy_balance_record WHERE balance > 0 and context='global-economy' ORDER BY RAND() LIMIT ?";

        // 使用 try-with-resources 确保资源被自动关闭
        try (Connection connection = DataSourceProvider.getDataSource("economy").getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, victim);  // 设置 LIMIT 参数
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    teconomy_balance_record r = new teconomy_balance_record();
                    setvalue(r, resultSet);
                    list.add(r);
                }
            }
        }
        return list;
    }

    // 更新指定 uuid 用户的 balance
    public void updateBalance(String uuid, double newBalance) throws SQLException {
        String sql = "UPDATE economy_balance_record SET balance = ? WHERE uuid = ? and context='global-economy' ";

        try (Connection connection = DataSourceProvider.getDataSource("economy").getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // 设置参数
            statement.setDouble(1, newBalance);  // 设置新的 balance
            statement.setString(2, uuid);        // 设置指定的 uuid

            // 执行更新操作
            statement.executeUpdate();
        }
    }

    // 根据 uuid 获取记录
    public teconomy_balance_record getRecordByUuid(String uuid) throws SQLException {
        String sql = "SELECT * FROM economy_balance_record WHERE uuid = ? and context='global-economy'";
        try (Connection connection = DataSourceProvider.getDataSource("economy").getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    teconomy_balance_record record = new teconomy_balance_record();
                    setvalue(record, resultSet);
                    return record;
                }
            }
        }
        return null;
    }

    public boolean increaseBalance(String uuid, double amount) throws SQLException {
        String sql = "UPDATE your_table SET balance = balance + ? WHERE uuid = ?";

        // 使用 try-with-resources 确保资源被自动关闭
        try (Connection connection = DataSourceProvider.getDataSource("economy").getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // 设置参数
            statement.setDouble(1, amount);  // 设置增加的金额
            statement.setString(2, uuid);    // 设置指定的 uuid

            // 执行更新操作
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;  // 返回更新是否成功
        }
    }

    private void setvalue(teconomy_balance_record bean, ResultSet resultSet) throws SQLException {
        bean.setContext(resultSet.getString("context"));
        bean.setCurrency(resultSet.getString("currency"));
        bean.setUuid(resultSet.getString("uuid"));
        bean.setBalance(resultSet.getDouble("balance"));
        bean.setLatest(resultSet.getLong("latest"));
    }

}
*/
