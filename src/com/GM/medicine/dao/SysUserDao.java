package com.GM.medicine.dao;

// 导入 Connection：所有 SQL 语句都必须先通过它发送给数据库
import java.sql.Connection;
// 导入 PreparedStatement：用占位符预编译 SQL，避免拼接字符串带来的注入风险
import java.sql.PreparedStatement;
// 导入 ResultSet：按列名读取查询返回的用户数据
import java.sql.ResultSet;
// 导入 SQLException：接收 JDBC 操作抛出的异常，统一在本类中转成方法返回值
import java.sql.SQLException;
// 导入 Timestamp：把 DATETIME 列取出后再转成实体的 LocalDateTime 字段
import java.sql.Timestamp;
// 导入 ArrayList：用动态数组收集查询结果，方便按顺序追加
import java.util.ArrayList;
// 导入 List：findAll 方法返回的用户集合类型
import java.util.List;
// 导入 DBUtil：负责提供数据库连接与释放 JDBC 资源
import com.GM.medicine.common.util.DBUtil;
// 导入 SysUser：本 DAO 负责持久化的实体类型
import com.GM.medicine.pojo.entity.SysUser;

/**
 * - 用户数据访问类
 * - 负责 sys_user 表的增删改查，是用户实体与数据库表之间的桥梁
 * - 向上层业务代码屏蔽 JDBC 细节，使调用方只面对实体对象
 */
public class SysUserDao implements BaseDao<SysUser> {

    /**
     * 新增一个用户
     * - password 由 Service 层完成 BCrypt 哈希后传入，本方法原样入库，不做任何密码处理
     * - role、status 不写入，使用数据库默认值
     *
     * @param sysUser 待保存的用户对象，id 由数据库自增生成，无需设置
     * @return 插入成功返回 true，失败返回 false
     */
    @Override
    public boolean add(SysUser sysUser) {
        // 只写入业务字段，id 交给自增主键，role、status 交给数据库默认值，时间字段交给数据库默认值
        String sql = "INSERT INTO sys_user (username, password, real_name, phone)"
                + " VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 占位符下标从 1 开始，绑定顺序必须与 SQL 中的书写顺序一致
            stmt.setString(1, sysUser.getUserName());
            stmt.setString(2, sysUser.getPassword());
            stmt.setString(3, sysUser.getRealName());
            stmt.setString(4, sysUser.getPhone());
            // executeUpdate 返回受影响行数，大于 0 说明插入成功
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 用户名唯一约束冲突、字段超长等问题都会走到这里，记录下来并返回失败
            System.err.println("新增用户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据用户 id 删除用户
     *
     * @param id 待删除的用户 id
     * @return 删除成功返回 true，id 为空时返回 false
     */
    @Override 
    public boolean delete(Integer id) {
        // 没有主键就无法定位记录，直接返回失败，避免误删全表
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM sys_user WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 用户已被销售记录引用时，外键约束会阻止删除并抛异常
            System.err.println("删除用户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 更新用户的基本信息
     * - 只修改 real_name、phone；id、username、password、role、status 均不在本 SQL 中
     * - 密码修改走 updatePassword，角色与状态变更由数据库默认值和管理员操作控制
     *
     * @param sysUser 携带新值并包含 id 的用户对象
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    public boolean update(SysUser sysUser) {
        // updated_time 列没有 ON UPDATE 属性，因此需要在此显式刷新为当前时间
        String sql = "UPDATE sys_user SET real_name = ?, phone = ?, updated_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, sysUser.getRealName());
            stmt.setString(2, sysUser.getPhone());
            stmt.setInt(3, sysUser.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新用户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 查询单个用户
     *
     * @param id 用户编号
     * @return 查询到的用户对象，记录不存在时返回 null
     */
    @Override
    public SysUser findById(Integer id) {
        String sql = "SELECT id, username, password, real_name, phone, role, status,"
                + " created_time, updated_time FROM sys_user WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            // 主键唯一，最多只有一行，取到就转换后返回
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询用户失败：" + e.getMessage());
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 查询全部用户
     *
     * @return 用户列表，没有数据时返回空集合而不是 null
     */
    @Override
    public List<SysUser> findAll() {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, username, password, real_name, phone, role, status,"
                + " created_time, updated_time FROM sys_user ORDER BY id";
        List<SysUser> sysUserList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                sysUserList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询全部用户失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return sysUserList;
    }

    /**
     * 根据用户名查询用户
     *
     * @param userName 登录用户名
     * @return 查询到的用户对象，用户名不存在时返回 null
     */
    public SysUser findByUserName(String userName) {
        // 用户名为空时不可能命中任何记录，直接返回 null，省一次数据库访问
        if (userName == null || userName.isEmpty()) {
            return null;
        }
        String sql = "SELECT id, username, password, real_name, phone, role, status,"
                + " created_time, updated_time FROM sys_user WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
            rs = stmt.executeQuery();
            // username 上有唯一索引，最多只有一行
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("按用户名查询用户失败：" + e.getMessage());
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 只更新指定用户的密码
     * - 不复用 update 方法：整行更新会把用户名、角色等字段一起覆盖
     * - 密码修改是高频且敏感的操作，单独一条 SQL 更安全也更高效
     *
     * @param id          用户编号
     * @param newPassword 新密码
     * @return 修改成功返回 true，失败返回 false
     */
    public boolean updatePassword(int id, String newPassword) {
        // updated_time 列没有 ON UPDATE 属性，修改密码同样需要显式刷新
        String sql = "UPDATE sys_user SET password = ?, updated_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPassword);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("修改用户密码失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 把结果集当前行转换为用户对象
     *
     * @param rs 指向当前行的结果集
     * @return 填充好字段的用户对象
     * @throws SQLException 读取列失败时抛出，由调用方统一处理
     */
    private SysUser mapRow(ResultSet rs) throws SQLException {
        SysUser sysUser = new SysUser();
        sysUser.setId(rs.getInt("id"));
        sysUser.setUserName(rs.getString("username"));
        sysUser.setPassword(rs.getString("password"));
        sysUser.setRealName(rs.getString("real_name"));
        sysUser.setPhone(rs.getString("phone"));
        sysUser.setRole(rs.getInt("role"));
        sysUser.setStatus(rs.getInt("status"));
        // DATETIME 列取出来是 Timestamp，需要转成实体使用的 LocalDateTime；列允许为空，先判空再转换
        Timestamp createdTime = rs.getTimestamp("created_time");
        if (createdTime != null) {
            sysUser.setCreatedTime(createdTime.toLocalDateTime());
        }
        Timestamp updatedTime = rs.getTimestamp("updated_time");
        if (updatedTime != null) {
            sysUser.setUpdatedTime(updatedTime.toLocalDateTime());
        }
        return sysUser;
    }

}