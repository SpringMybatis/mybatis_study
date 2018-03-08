package com.xgw.wwx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by EX-ZHONGJUN001 on 2018/3/8.
 */
public class JdbcUtil {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtil.class);

    private static String url = null;
    private static String user = null;
    private static String password = null;
    private static String driverClass = null;
    private static JdbcUtil jdbcUtil = null;

    private JdbcUtil() {

    }

    public static JdbcUtil getInstance() {
        if (jdbcUtil == null) {
            synchronized (JdbcUtil.class) {
                jdbcUtil = new JdbcUtil();
            }
        }
        return jdbcUtil;
    }

    static {
        try {
            //读取application.properties文件
            Properties props = new Properties();
            InputStream in = JdbcUtil.class.getResourceAsStream("/application.properties");
            props.load(in);

            //读取信息
            url = props.getProperty("spring.datasource.url");
            user = props.getProperty("spring.datasource.username");
            password = props.getProperty("spring.datasource.password");
            driverClass = props.getProperty("spring.datasource.driver-class-name");

            // 注册驱动
            Class.forName(driverClass);
        } catch (Exception e) {
            logger.error("-- 注册驱动程序异常 --", e);
            logger.error("-- 静态代码块加载异常 --", e);
        }
    }

    /**
     * 获取连接
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.error("-- 获取连接异常 --", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 释放资源
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("-- 释放资源异常 --", e);
            throw new RuntimeException(e);
        }
    }

}






String sql = "SELECT `id` as id, `user_name` as userName, `password` as passWord FROM t_auth_user where id = ?";
JdbcUtil jdbcUtil = JdbcUtil.getInstance();
Connection connection = jdbcUtil.getConnection();
PreparedStatement ps = connection.prepareStatement(sql);
ps.setLong(1, id);

ResultSet rs = ps.executeQuery();
while (rs.next()) {
    Long dbId = rs.getLong("id");
    String userName = rs.getString("userName");
    String passWord = rs.getString("passWord");

    logger.info("-- id: {}, userName: {}, passWord: {} --", dbId, userName, passWord);
}

JdbcUtil.close(connection, ps, rs);






