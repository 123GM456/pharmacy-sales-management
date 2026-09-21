package com.GM.medicine.common.util;

// 导入 Connection：表示数据库连接对象，是所有 SQL 操作的入口
import java.sql.Connection;
// 导入 DriverManager：根据连接地址与账号密码创建数据库连接
import java.sql.DriverManager;
// 导入 ResultSet：接收查询语句返回的结果集
import java.sql.ResultSet;
// 导入 SQLException：表示数据库操作过程中发生的异常
import java.sql.SQLException;
// 导入 Statement：用于执行 SQL 语句
import java.sql.Statement;

/**
 * - JDBC 数据库工具类
 * - 集中保存数据库连接配置，并提供获取连接与释放 JDBC 资源的方法
 * - 作为 DAO 层访问数据库的统一入口，使 DAO 不必关心连接地址与账号密码
 */
public final class DBUtil {

    // 数据库驱动类名，MySQL 8 及以后版本使用 com.mysql.cj.jdbc.Driver
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // 数据库连接地址，由协议、主机、端口、数据库名组成，末尾拼接连接参数
    private static final String URL = "jdbc:mysql://localhost:3306/medicine_sales_db"
            // 关闭 SSL 加密，本地开发环境无需加密通信
            + "?useSSL=false"
            // 指定服务器时区，否则 MySQL 8 会报 server time zone value is unrecognized
            + "&serverTimezone=Asia/Shanghai"
            // 指定字符编码为 UTF-8，防止药品名称等中文出现乱码
            + "&characterEncoding=utf8"
            // 允许获取服务器公钥，MySQL 8 默认认证方式在非 SSL 连接下必须开启此项
            + "&allowPublicKeyRetrieval=true";

    // 数据库登录账号
    private static final String USERNAME = "root";

    // 数据库登录密码
    private static final String PASSWORD = "050826";

    /**
     * - 类加载时注册 MySQL 驱动
     * - 驱动只需注册一次，放在静态块中可避免每次获取连接时重复注册
     */
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            // 没有驱动就无法建立任何连接，属于环境配置问题；直接抛出错误终止，比让后续操作逐个失败更容易定位
            throw new ExceptionInInitializerError(
                    "MySQL 驱动加载失败，请确认已将 mysql-connector-j 的 jar 加入项目类路径：" + e.getMessage());
        }
    }

    // 私有构造方法，阻止外部创建实例，本类只对外提供静态方法
    private DBUtil() {
    }

    /**
     * 获取一个数据库连接
     *
     * @return 可用的数据库连接对象
     * @throws SQLException 连接失败时抛出，例如账号密码错误、MySQL 服务未启动、数据库不存在
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * - 关闭查询操作涉及的三个资源
     * - 释放顺序为 ResultSet → Statement → Connection，与创建顺序相反
     * - 若先关闭连接，结果集与语句将无法正常释放，因此顺序不可颠倒
     *
     * @param conn 数据库连接，允许为 null
     * @param stmt 语句对象，允许为 null
     * @param rs   结果集对象，允许为 null
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    /**
     * - 关闭增删改操作涉及的两个资源
     * - 释放顺序为 Statement → Connection
     *
     * @param conn 数据库连接，允许为 null
     * @param stmt 语句对象，允许为 null
     */
    public static void close(Connection conn, Statement stmt) {
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    /**
     * - 静默关闭单个资源，不向外抛出异常
     * - 关闭失败通常说明连接已断开，调用方无从补救，因此只输出提示信息
     * - 目的是避免在 finally 中因关闭异常覆盖掉真正的业务异常
     *
     * @param resource 实现了 AutoCloseable 的资源，如 ResultSet、Statement、Connection
     */
    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            // 只提示不抛出，防止关闭资源的异常掩盖业务异常
            System.err.println("关闭 JDBC 资源失败：" + e.getMessage());
        }
    }

}