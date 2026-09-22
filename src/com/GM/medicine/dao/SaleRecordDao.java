package com.GM.medicine.dao;

// 导入 Connection：所有 SQL 语句都必须先通过它发送给数据库
import java.sql.Connection;
// 导入 PreparedStatement：用占位符预编译 SQL，避免拼接字符串带来的注入风险
import java.sql.PreparedStatement;
// 导入 ResultSet：按列名读取查询返回的销售记录数据
import java.sql.ResultSet;
// 导入 SQLException：接收 JDBC 操作抛出的异常，统一在本类中转成方法返回值
import java.sql.SQLException;
// 导入 Timestamp：sale_time 等 DATETIME 列读写时都要用它承载 LocalDateTime
import java.sql.Timestamp;
// 导入 ArrayList：用动态数组收集查询结果，方便按顺序追加
import java.util.ArrayList;
// 导入 List：findAll 方法返回的销售记录集合类型
import java.util.List;
// 导入 LocalDateTime：入参是实体的销售时间，转换时需要显式声明
import java.time.LocalDateTime;
// 导入 DBUtil：负责提供数据库连接与释放 JDBC 资源
import com.GM.medicine.common.util.DBUtil;
// 导入 SaleRecord：本 DAO 负责持久化的实体类型
import com.GM.medicine.pojo.entity.SaleRecord;

/**
 * - 销售记录数据访问类
 * - 负责 sale_record 表的增删改查，是销售记录实体与数据库表之间的桥梁
 * - 向上层业务代码屏蔽 JDBC 细节，使调用方只面对实体对象
 */
public class SaleRecordDao implements BaseDao<SaleRecord> {

    /**
     * 新增一条销售记录
     *
     * @param saleRecord 待保存的销售记录对象，id 由数据库自增生成，无需设置
     * @return 插入成功返回 true，失败返回 false
     */
    @Override
    public boolean add(SaleRecord saleRecord) {
        // 只写入业务字段，id 交给自增主键，时间字段交给数据库默认值
        String sql = "INSERT INTO sale_record (medicine_id, operator_id, quantity, unit_price,"
                + " total_amount, sale_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 外键、数量、金额、销售时间这些列都没有数据库默认值，用 setObject 绑定：
            // 调用方漏填时由数据库约束直接报错，而不是在这里补一个 0 造成无效的销售记录
            stmt.setInt(1, saleRecord.getMedicineId());
            stmt.setInt(2, saleRecord.getOperatorId());
            stmt.setInt(3, saleRecord.getQuantity());
            stmt.setBigDecimal(4, saleRecord.getUnitPrice());
            stmt.setBigDecimal(5, saleRecord.getTotalAmount());
            stmt.setTimestamp(6, toTimestamp(saleRecord.getSaleTime()));
            stmt.setString(7, saleRecord.getRemark());
            // executeUpdate 返回受影响行数，大于 0 说明插入成功
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 药品或操作员不存在（外键校验失败）、必填字段为空等问题都会走到这里
            System.err.println("新增销售记录失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据销售记录 id 删除记录
     *
     * @param saleRecord 至少需要包含 id 的销售记录对象
     * @return 删除成功返回 true，对象为空或 id 为空时返回 false
     */
    @Override 
    public boolean delete(Integer id) {
        // 没有主键就无法定位记录，直接返回失败，避免误删全表
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM sale_record WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除销售记录失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 更新销售记录的全部业务字段
     *
     * @param saleRecord 携带新值并包含 id 的销售记录对象
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    public boolean update(SaleRecord saleRecord) {
        // updated_time 列没有 ON UPDATE 属性，因此需要在此显式刷新为当前时间
        String sql = "UPDATE sale_record SET medicine_id = ?, operator_id = ?, quantity = ?,"
                + " unit_price = ?, total_amount = ?, sale_time = ?, remark = ?, updated_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, saleRecord.getMedicineId());
            stmt.setObject(2, saleRecord.getOperatorId());
            stmt.setObject(3, saleRecord.getQuantity());
            stmt.setObject(4, saleRecord.getUnitPrice());
            stmt.setObject(5, saleRecord.getTotalAmount());
            stmt.setObject(6, toTimestamp(saleRecord.getSaleTime()));
            stmt.setString(7, saleRecord.getRemark());
            stmt.setObject(8, saleRecord.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新销售记录失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 查询单条销售记录
     *
     * @param id 销售记录编号
     * @return 查询到的销售记录对象，记录不存在时返回 null
     */
    @Override
    public SaleRecord findById(Integer id) {
        String sql = "SELECT id, medicine_id, operator_id, quantity, unit_price, total_amount,"
                + " sale_time, remark FROM sale_record WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id);
            rs = stmt.executeQuery();
            // 主键唯一，最多只有一行，取到就转换后返回
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询销售记录失败：" + e.getMessage());
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 查询全部销售记录
     *
     * @return 销售记录列表，没有数据时返回空集合而不是 null
     */
    @Override
    public List<SaleRecord> findAll() {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, medicine_id, operator_id, quantity, unit_price, total_amount,"
                + " sale_time, remark FROM sale_record ORDER BY id";
        List<SaleRecord> saleRecordList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleRecordList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询全部销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleRecordList;
    }

    /**
     * 把结果集当前行转换为销售记录对象
     *
     * @param rs 指向当前行的结果集
     * @return 填充好字段的销售记录对象
     * @throws SQLException 读取列失败时抛出，由调用方统一处理
     */
    private SaleRecord mapRow(ResultSet rs) throws SQLException {
        SaleRecord saleRecord = new SaleRecord();
        saleRecord.setId(rs.getInt("id"));
        saleRecord.setMedicineId(rs.getInt("medicine_id"));
        saleRecord.setOperatorId(rs.getInt("operator_id"));
        saleRecord.setQuantity(rs.getInt("quantity"));
        saleRecord.setUnitPrice(rs.getBigDecimal("unit_price"));
        saleRecord.setTotalAmount(rs.getBigDecimal("total_amount"));
        // sale_time 是必填列，取出 Timestamp 后直接转成 LocalDateTime，无需判空
        saleRecord.setSaleTime(rs.getTimestamp("sale_time").toLocalDateTime());
        saleRecord.setRemark(rs.getString("remark"));
        // created_time、updated_time 允许为空，先判空再转换
        Timestamp createdTime = rs.getTimestamp("created_time");
        if (createdTime != null) {
            saleRecord.setCreatedTime(createdTime.toLocalDateTime());
        }
        Timestamp updatedTime = rs.getTimestamp("updated_time");
        if (updatedTime != null) {
            saleRecord.setUpdatedTime(updatedTime.toLocalDateTime());
        }
        return saleRecord;
    }

    /**
     * 把实体的 LocalDateTime 转成 JDBC 使用的 java.sql.Timestamp
     *
     * @param localDateTime 实体中的销售时间，允许为 null
     * @return 对应的 java.sql.Timestamp，入参为 null 时返回 null
     */
    private Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
    }

}