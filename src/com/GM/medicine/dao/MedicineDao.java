package com.GM.medicine.dao;

// 导入 Connection：所有 SQL 语句都必须先通过它发送给数据库
import java.sql.Connection;
// 导入 Date：DATE 列取出后是 java.sql.Date，写入时也要用它承载 LocalDate
import java.sql.Date;
// 导入 PreparedStatement：用占位符预编译 SQL，避免拼接字符串带来的注入风险
import java.sql.PreparedStatement;
// 导入 ResultSet：按列名读取查询返回的药品数据
import java.sql.ResultSet;
// 导入 SQLException：接收 JDBC 操作抛出的异常，统一在本类中转成方法返回值
import java.sql.SQLException;
// 导入 Timestamp：把 DATETIME 列取出后再转成实体的 LocalDateTime 字段
import java.sql.Timestamp;
// 导入 ArrayList：用动态数组收集查询结果，方便按顺序追加
import java.util.ArrayList;
// 导入 List：findAll 方法返回的药品集合类型
import java.util.List;
// 导入 LocalDate：入参和返回值都是实体的日期类型，转换时需要显式声明
import java.time.LocalDate;
// 导入 DBUtil：负责提供数据库连接与释放 JDBC 资源
import com.GM.medicine.common.util.DBUtil;
// 导入 Medicine：本 DAO 负责持久化的实体类型
import com.GM.medicine.pojo.entity.Medicine;

/**
 * - 药品数据访问类
 * - 负责 medicine 表的增删改查，是药品实体与数据库表之间的桥梁
 * - 向上层业务代码屏蔽 JDBC 细节，使调用方只面对实体对象
 */
public class MedicineDao implements BaseDao<Medicine> {

    /**
     * 新增一种药品
     *
     * @param medicine 待保存的药品对象，id 由数据库自增生成，无需设置
     * @return 插入成功返回 true，失败返回 false
     */
    @Override
    public boolean add(Medicine medicine) {
        // 只写入业务字段，id 交给自增主键，时间字段交给数据库默认值
        String sql = "INSERT INTO medicine (name, category, specification, manufacturer, batch_number,"
                + " purchase_price, sale_price, stock, warning_stock, production_date, expiry_date, status)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 占位符下标从 1 开始，绑定顺序必须与 SQL 中的书写顺序一致
            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getCategory());
            stmt.setString(3, medicine.getSpecification());
            stmt.setString(4, medicine.getManufacturer());
            stmt.setString(5, medicine.getBatchNumber());
            stmt.setBigDecimal(6, medicine.getPurchasePrice());
            // 实体用 salePrice 表示销售价格，对应数据库的 sale_price 列
            stmt.setBigDecimal(7, medicine.getSalePrice());
            // stock、warning_stock 不允许为空，未指定时按各自的默认值 0、10 写入，避免插入 NULL 触发约束错误
            stmt.setInt(8, medicine.getStock() == null ? 0 : medicine.getStock());
            stmt.setInt(9, medicine.getWarningStock() == null ? 10 : medicine.getWarningStock());
            // 两个日期列允许为空，为空时写入 null，数据库中保持空值
            stmt.setDate(10, toSqlDate(medicine.getProductionDate()));
            stmt.setDate(11, toSqlDate(medicine.getExpiryDate()));
            // status 未指定时按 1（在售）写入
            stmt.setInt(12, medicine.getStatus() == null ? 1 : medicine.getStatus());
            // executeUpdate 返回受影响行数，大于 0 说明插入成功
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 必填字段缺失、字段超长等问题都会走到这里，记录下来并返回失败
            System.err.println("新增药品失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据药品 id 删除药品
     *
     * @param id 待删除的药品 id
     * @return 删除成功返回 true，id 为空时返回 false
     */
    @Override
    public boolean delete(Integer id) {
        // 没有主键就无法定位记录，直接返回失败，避免误删全表
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM medicine WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 药品已被销售记录引用时，外键约束会阻止删除并抛异常
            System.err.println("删除药品失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 更新药品的全部业务字段
     *
     * @param medicine 携带新值并包含 id 的药品对象
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean update(Medicine medicine) {
        // updated_time 列没有 ON UPDATE 属性，因此需要在此显式刷新为当前时间
        String sql = "UPDATE medicine SET name = ?, category = ?, specification = ?, manufacturer = ?,"
                + " batch_number = ?, purchase_price = ?, sale_price = ?, stock = ?, warning_stock = ?,"
                + " production_date = ?, expiry_date = ?, status = ?, updated_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getCategory());
            stmt.setString(3, medicine.getSpecification());
            stmt.setString(4, medicine.getManufacturer());
            stmt.setString(5, medicine.getBatchNumber());
            stmt.setBigDecimal(6, medicine.getPurchasePrice());
            stmt.setBigDecimal(7, medicine.getSalePrice());
            stmt.setInt(8, medicine.getStock() == null ? 0 : medicine.getStock());
            stmt.setInt(9, medicine.getWarningStock() == null ? 10 : medicine.getWarningStock());
            stmt.setDate(10, toSqlDate(medicine.getProductionDate()));
            stmt.setDate(11, toSqlDate(medicine.getExpiryDate()));
            stmt.setInt(12, medicine.getStatus() == null ? 1 : medicine.getStatus());
            stmt.setInt(13, medicine.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新药品失败：" + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 根据 id 查询单个药品
     *
     * @param id 药品编号
     * @return 查询到的药品对象，记录不存在时返回 null
     */
    @Override
    public Medicine findById(Integer id) {
        String sql = "SELECT id, name, category, specification, manufacturer, batch_number, purchase_price,"
                + " sale_price, stock, warning_stock, production_date, expiry_date, status,"
                + " created_time, updated_time FROM medicine WHERE id = ?";
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
            System.err.println("查询药品失败：" + e.getMessage());
            return null;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 查询全部药品
     *
     * @return 药品列表，没有数据时返回空集合而不是 null
     */
    @Override
    public List<Medicine> findAll() {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, name, category, specification, manufacturer, batch_number, purchase_price,"
                + " sale_price, stock, warning_stock, production_date, expiry_date, status,"
                + " created_time, updated_time FROM medicine ORDER BY id";
        List<Medicine> medicineList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                medicineList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询全部药品失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return medicineList;
    }

    /**
     * 把 LocalDate 转成 java.sql.Date，供写入 DATE 列使用
     *
     * @param date 实体中的日期值，允许为 null
     * @return 对应的 java.sql.Date，入参为 null 时返回 null
     */
    private Date toSqlDate(LocalDate date) {
        // DATE 列允许为空，入参为 null 时直接返回 null，让数据库保存空值
        if (date == null) {
            return null;
        }
        // Connector/J 对 setObject 直传 LocalDate 兼容性不稳定，显式转成 java.sql.Date 最可靠
        return Date.valueOf(date);
    }

    /**
     * 查询所有可用药品
     * - 可用需同时满足：状态正常、库存大于 0、未过期
     *
     * @return 所有可用药品的列表
     */
    public List<Medicine> findAvailableMedicines() {
        String sql = "SELECT id, name, category, specification, manufacturer, batch_number, purchase_price,"
                + " sale_price, stock, warning_stock, production_date, expiry_date, status,"
                + " created_time, updated_time FROM medicine"
                + " WHERE status = 1 AND stock > 0 AND expiry_date > CURDATE() ORDER BY id";
        List<Medicine> availableMedicines = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                availableMedicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有可用药品失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return availableMedicines;
    }

    /**
     * 查询所有预警药品
     * - 预警需同时满足：状态正常、库存大于 0、未过期、库存低于预警值
     *
     * @return 所有预警药品的列表
     */
    public List<Medicine> findWarningMedicines() {
        String sql = "SELECT id, name, category, specification, manufacturer, batch_number, purchase_price,"
                + " sale_price, stock, warning_stock, production_date, expiry_date, status,"
                + " created_time, updated_time FROM medicine"
                + " WHERE status = 1 AND stock > 0 AND stock <= warning_stock AND expiry_date > CURDATE() ORDER BY id";
        List<Medicine> warningMedicines = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                warningMedicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有预警药品失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return warningMedicines;
    }

    /**
     * 查询所有过期药品
     * - 过期需同时满足：状态正常、库存大于 0、已过期
     *
     * @return 所有过期药品的列表
     */
    public List<Medicine> findExpiredMedicines() {
        String sql = "SELECT id, name, category, specification, manufacturer, batch_number, purchase_price,"
                + " sale_price, stock, warning_stock, production_date, expiry_date, status,"
                + " created_time, updated_time FROM medicine"
                + " WHERE status = 1 AND stock > 0 AND expiry_date < CURDATE() ORDER BY id";
        List<Medicine> expiredMedicines = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                expiredMedicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有过期药品失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return expiredMedicines;
    }

    /**
     * 减少药品库存
     *
     * @param medicineId 要减少库存的药品编号
     * @param quantity 要减少的库存数量
     */
    public void decreaseStock(Integer medicineId, Integer quantity) {
        String sql = "UPDATE medicine SET stock = stock - ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, medicineId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("减少药品库存失败：" + e.getMessage());
        }
    }

    /**
     * 把 ResultSet 转成 Medicine 实体
     *
     * @param rs 包含药品数据的 ResultSet
     * @return 对应的 Medicine 实体
     * @throws SQLException 如果 ResultSet 操作失败
     */
    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setId(rs.getInt("id"));
        medicine.setName(rs.getString("name"));
        medicine.setCategory(rs.getString("category"));
        medicine.setSpecification(rs.getString("specification"));
        medicine.setManufacturer(rs.getString("manufacturer"));
        medicine.setBatchNumber(rs.getString("batch_number"));
        medicine.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        medicine.setSalePrice(rs.getBigDecimal("sale_price"));
        medicine.setStock(rs.getInt("stock"));
        medicine.setWarningStock(rs.getInt("warning_stock"));
        medicine.setProductionDate(rs.getDate("production_date").toLocalDate());
        medicine.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        medicine.setStatus(rs.getInt("status"));
        // DATETIME 列取出来是 Timestamp，需要转成实体使用的 LocalDateTime；列允许为空，先判空再转换
        Timestamp createdTime = rs.getTimestamp("created_time");
        if (createdTime != null) {
            medicine.setCreatedTime(createdTime.toLocalDateTime());
        }
        Timestamp updatedTime = rs.getTimestamp("updated_time");
        if (updatedTime != null) {
            medicine.setUpdatedTime(updatedTime.toLocalDateTime());
        }
        return medicine;
    }

}
