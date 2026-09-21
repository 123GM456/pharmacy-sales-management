package com.GM.medicine.dao;

// 导入 Connection：所有 SQL 语句都必须先通过它发送给数据库
import java.sql.Connection;
// 导入 PreparedStatement：用占位符预编译 SQL，避免拼接字符串带来的注入风险
import java.sql.PreparedStatement;
// 导入 ResultSet：按列名读取查询返回的客户数据
import java.sql.ResultSet;
// 导入 SQLException：接收 JDBC 操作抛出的异常，统一在本类中转成方法返回值
import java.sql.SQLException;
// 导入 Timestamp：把 DATETIME 列取出后再转成实体的 LocalDateTime 字段
import java.sql.Timestamp;
// 导入 ArrayList：用动态数组收集查询结果，方便按顺序追加
import java.util.ArrayList;
// 导入 List：findAll 方法返回的客户集合类型
import java.util.List;
// 导入 DBUtil：负责提供数据库连接与释放 JDBC 资源
import com.GM.medicine.common.util.DBUtil;
// 导入 Customer：本 DAO 负责持久化的实体类型
import com.GM.medicine.pojo.entity.Customer;

/**
 * - 客户数据访问类
 * - 负责 customer 表的增删改查，是客户实体与数据库表之间的桥梁
 * - 向上层业务代码屏蔽 JDBC 细节，使调用方只面对实体对象
 */
public class CustomerDao implements BaseDao<Customer> {

    /**
     * 新增一个客户
     *
     * @param customer 待保存的客户对象，id 由数据库自增生成，无需设置
     * @return 插入成功返回 true，失败返回 false
     */
    @Override
    public boolean add(Customer customer) {
        // 只写入业务字段，id 交给自增主键，时间字段交给数据库默认值
        String sql = "INSERT INTO customer (name, sex, phone, address, remark) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 占位符下标从 1 开始，绑定顺序必须与 SQL 中的书写顺序一致
            stmt.setString(1, customer.getName());
            // sex 列不允许为空且默认值为 0，未指定时按 0 写入，避免插入 NULL 触发约束错误
            stmt.setInt(2, customer.getSex() == null ? 0 : customer.getSex());
            // 电话、地址、备注都允许为空，直接绑定即可，为空时就存 NULL
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getRemark());
            // executeUpdate 返回受影响行数，大于 0 说明插入成功
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 姓名为空、字段超长等问题都会走到这里，记录下来并返回失败
            System.err.println("新增客户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据客户 id 删除客户
     *
     * @param id 待删除的客户 id
     * @return 删除成功返回 true，id 为空时返回 false
     */
    @Override 
    public boolean delete(Integer id) {
        // 没有主键就无法定位记录，直接返回失败，避免误删全表
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM customer WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 客户已被销售记录引用时，外键约束会阻止删除并抛异常
            System.err.println("删除客户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 更新客户的全部业务字段
     *
     * @param customer 携带新值并包含 id 的客户对象
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    public boolean update(Customer customer) {
        // updated_time 列没有 ON UPDATE 属性，因此需要在此显式刷新为当前时间
        String sql = "UPDATE customer SET name = ?, sex = ?, phone = ?, address = ?, remark = ?,"
                + " updated_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, customer.getName());
            stmt.setInt(2, customer.getSex() == null ? 0 : customer.getSex());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getRemark());
            stmt.setInt(6, customer.getId());      
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新客户失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 查询单个客户
     *
     * @param id 客户编号
     * @return 查询到的客户对象，记录不存在时返回 null
     */
    @Override
    public Customer findById(Integer id) {
        String sql = "SELECT id, name, sex, phone, address, remark, created_time, updated_time"
                + " FROM customer WHERE id = ?";
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
            System.err.println("查询客户失败：" + e.getMessage());
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 查询全部客户
     *
     * @return 客户列表，没有数据时返回空集合而不是 null
     */
    @Override
    public List<Customer> findAll() {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, name, sex, phone, address, remark, created_time, updated_time"
                + " FROM customer ORDER BY id";
        List<Customer> customerList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                customerList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询全部客户失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return customerList;
    }

    /**
     * 把结果集当前行转换为客户对象
     *
     * @param rs 指向当前行的结果集
     * @return 填充好字段的客户对象
     * @throws SQLException 读取列失败时抛出，由调用方统一处理
     */
    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setName(rs.getString("name"));
        customer.setSex(rs.getInt("sex"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setRemark(rs.getString("remark"));
        // DATETIME 列取出来是 Timestamp，需要转成实体使用的 LocalDateTime；列允许为空，先判空再转换
        Timestamp createdTime = rs.getTimestamp("created_time");
        if (createdTime != null) {
            customer.setCreatedTime(createdTime.toLocalDateTime());
        }
        Timestamp updatedTime = rs.getTimestamp("updated_time");
        if (updatedTime != null) {
            customer.setUpdatedTime(updatedTime.toLocalDateTime());
        }
        return customer;
    }

}