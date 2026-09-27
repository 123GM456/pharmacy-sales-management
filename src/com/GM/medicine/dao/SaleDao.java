package com.GM.medicine.dao;

// 导入 Connection：所有 SQL 语句都必须先通过它发送给数据库
import java.sql.Connection;
// 导入 Date：销售日期，只精确到日
import java.sql.Date;
// 导入 PreparedStatement：用占位符预编译 SQL，避免拼接字符串带来的注入风险
import java.sql.PreparedStatement;
// 导入 ResultSet：按列名读取查询返回的销售记录数据
import java.sql.ResultSet;
// 导入 SQLException：接收 JDBC 操作抛出的异常，统一在本类中转成方法返回值
import java.sql.SQLException;
// 导入 Timestamp：sale_time 等 DATETIME 列读写时都要用它承载 LocalDateTime
import java.util.ArrayList;
// 导入 List：findAll 方法返回的销售记录集合类型
import java.util.List;
// 导入 LocalDateTime：入参是实体的销售时间，转换时需要显式声明
import com.GM.medicine.common.util.DBUtil;
// 导入 Sale：本 DAO 负责持久化的实体类型
import com.GM.medicine.pojo.entity.Sale;

/**
 * - 销售记录数据访问类
 * - 负责 sale 表的增删改查，是销售记录实体与数据库表之间的桥梁
 * - 向上层业务代码屏蔽 JDBC 细节，使调用方只面对实体对象
 */
public class SaleDao implements BaseDao<Sale> {

    /**
     * 新增一条销售记录
     *
     * @param sale 待保存的销售记录对象，id 由数据库自增生成，无需设置
     * @return 插入成功返回 true，失败返回 false
     */
    @Override
    public boolean add(Sale sale) {
        // 只写入业务字段，id 交给自增主键，时间字段交给数据库默认值
        String sql = "INSERT INTO sale (medicine_id, operator_id, quantity, sale_price, total_amount, remark)"
        +"VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 外键、数量、金额这些列都没有数据库默认值，漏填时交给数据库约束直接报错，
            stmt.setInt(1, sale.getMedicineId());
            stmt.setInt(2, sale.getOperatorId());
            stmt.setInt(3, sale.getQuantity());
            stmt.setBigDecimal(4, sale.getSalePrice());
            stmt.setBigDecimal(5, sale.getTotalAmount());
            stmt.setString(6, sale.getRemark());
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
     * @param id 销售记录编号
     * @return 删除成功返回 true，对象为空或 id 为空时返回 false
     */
    @Override
    public boolean delete(Integer id) {
        // 没有主键就无法定位记录，直接返回失败，避免误删全表
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM sale WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除销售记录失败：" + e.getMessage());
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
    public Sale findById(Integer id) {
        String sql = "SELECT id, medicine_id, operator_id, quantity, sale_price, total_amount,"
                + " sale_time, remark FROM sale WHERE id = ?";
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
    public List<Sale> findAll() {
        // 按 id 排序，保证多次查询得到的顺序稳定；联表带出药品名称与操作员姓名，供表格直接展示
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询全部销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 根据销售日期查询当天的全部销售记录
     *
     * @param date 销售日期，只精确到日，时分秒由本方法补齐
     * @return 当天查询到的销售记录列表，当天没有数据时返回空集合而不是 null
     */
    public List<Sale> findByDate(Date date) {
        // sale_time 是 DATETIME，用 sale_time = ? 比较只认识零点那一秒，白天的记录全都查不到
        // 改成 [当天 00:00:00, 次日 00:00:00) 的半开区间：输入某一天就能命中当天所有记录
        // 用区间而不是 DATE(sale_time) = ?，是为了让查询条件仍能走 sale_time 索引
        String sql = "SELECT id, medicine_id, operator_id, quantity, sale_price, total_amount,"
                + " sale_time, remark FROM sale WHERE sale_time >= ? AND sale_time < ?"
                + " ORDER BY id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 下界是当天零点，上界是次日零点（不包含），两端的类型都要保持 DATETIME 语义
            stmt.setDate(1, date);
            stmt.setDate(2, Date.valueOf(date.toLocalDate().plusDays(1)));
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 根据药品编号查询销售记录
     *
     * @param medicineId 药品编号
     * @return 查询到的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByMedicineId(Integer medicineId) {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, medicine_id, operator_id, quantity, sale_price, total_amount,"
                + " sale_time, remark FROM sale WHERE medicine_id = ? ORDER BY id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, medicineId);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询药品销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }
    /**
     * 根据订单编号查询销售记录
     *
     * @param orderId 订单编号
     * @return 查询到的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByOrderId(Integer orderId) {
        // 按 id 排序，保证多次查询得到的顺序稳定
        String sql = "SELECT id, medicine_id, operator_id, quantity, sale_price, total_amount,"
                + " sale_time, remark FROM sale WHERE order_id = ? ORDER BY id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询订单销售记录失败：" + e.getMessage());  
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 查询今日销售记录
     *
     * @return 今日销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findTodaySales() {
        // 与 findByDate 相同的半开区间写法：[今天零点, 明天零点)，能命中今天全部记录且走 sale_time 索引；
        // 联表带出药品名称与操作员姓名，供表格直接展示
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE s.sale_time >= CURDATE() AND s.sale_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY)"
                + " ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询今日销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 查询本周销售记录（周一为一周的第一天）
     *
     * @return 本周销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findWeekSales() {
        // WEEKDAY(CURDATE()) 返回今天在一周内的序号（周一为 0），从今天减去它得到本周周一零点；
        // 区间 [本周一零点, 下周一零点) 覆盖整周记录且走 sale_time 索引；联表带出药品名称与操作员姓名
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE s.sale_time >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)"
                + " AND s.sale_time < DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)"
                + " ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询本周销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 查询本月销售记录
     *
     * @return 本月销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findMonthSales() {
        // DATE_FORMAT(CURDATE(), '%Y-%m-01') 得到本月一号零点，区间 [本月一号, 下月一号) 覆盖整月记录；
        // 联表带出药品名称与操作员姓名
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE s.sale_time >= DATE_FORMAT(CURDATE(), '%Y-%m-01')"
                + " AND s.sale_time < DATE_ADD(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 1 MONTH)"
                + " ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询本月销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 按药品名称模糊查询销售记录
     * - 联表 medicine 按药品名称过滤，返回的仍是销售记录列
     *
     * @param keyword 药品名称关键词，null 时按无效入参返回空集合
     * @return 药品名称包含关键词的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByMedicineName(String keyword) {
        // 关键词为 null 时按无效入参处理，直接返回空结果，避免拼出 "%null%" 误查
        if (keyword == null) {
            return new ArrayList<>();
        }
        // 表别名 s/m：sale 自身列加 s. 前缀区分；联表带出药品名称与操作员姓名，名称列与过滤条件共用 medicine 表
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE m.name LIKE ? ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 前后拼 % 表示"包含关键词"的任意位置匹配，通配符在 DAO 层拼接，调用方只传原始关键词
            stmt.setString(1, "%" + keyword + "%");
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("按药品名称查询销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 按销售编号前缀查询销售记录
     * - 只匹配编号开头部分（如 123 能查到 123456，中间的 234 / 结尾的 456 不行）
     * - 结果按销售编号正序排列；CAST(s.id AS CHAR) 把整数编号转成字符串再做前缀匹配
     *
     * @param idPrefix 销售编号前缀，null 时按无效入参返回空集合
     * @return 编号以前缀开头的销售记录列表（含联表药品信息），没有数据时返回空集合而不是 null
     */
    public List<Sale> findByIdPrefix(String idPrefix) {
        // 前缀为 null 时按无效入参处理，直接返回空结果，避免拼出 "%null%" 误查
        if (idPrefix == null) {
            return new ArrayList<>();
        }
        // 表别名 s/m/u：与同类联表查询一致，联表带出药品名称与操作员姓名
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE CAST(s.id AS CHAR) LIKE ? ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // % 只拼在末尾表示"从编号开头匹配"的前缀查找，通配符在 DAO 层拼接，调用方只传原始前缀
            stmt.setString(1, idPrefix + "%");
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合（ORDER BY s.id 保证编号正序）
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("按编号查询销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 按时间区间查询销售记录（区间两端都可为 null，null 表示该端不限制）
     * - 半开区间：sale_time >= 开始日零点 且 < 结束日零点，即包含开始日全天、不包含结束日当天
     *
     * @param start 开始日期（含当天），null 表示不限开始
     * @param end   结束右端点日期（不含当天），null 表示不限结束
     * @return 区间内的销售记录列表（含联表药品信息），没有数据时返回空集合而不是 null
     */
    public List<Sale> findByTimeRange(Date start, Date end) {
        // 无关键词：只按时间区间过滤
        return queryRange(start, end, null, null);
    }

    /**
     * 按时间区间 + 药品名称模糊查询销售记录
     *
     * @param start   开始日期（含当天），null 表示不限开始
     * @param end     结束右端点日期（不含当天），null 表示不限结束
     * @param keyword 药品名称关键词，null 或空白时退化为纯区间查询
     * @return 区间内药品名称包含关键词的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByTimeRangeAndMedicineName(Date start, Date end, String keyword) {
        // 空白关键词退化为纯区间查询
        if (keyword == null || keyword.trim().isEmpty()) {
            return queryRange(start, end, null, null);
        }
        return queryRange(start, end, "m.name", "%" + keyword.trim() + "%");
    }

    /**
     * 按时间区间 + 操作员姓名模糊查询销售记录
     *
     * @param start   开始日期（含当天），null 表示不限开始
     * @param end     结束右端点日期（不含当天），null 表示不限结束
     * @param keyword 操作员姓名关键词，null 或空白时退化为纯区间查询
     * @return 区间内操作员姓名包含关键词的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByTimeRangeAndOperator(Date start, Date end, String keyword) {
        // 空白关键词退化为纯区间查询
        if (keyword == null || keyword.trim().isEmpty()) {
            return queryRange(start, end, null, null);
        }
        return queryRange(start, end, "u.real_name", "%" + keyword.trim() + "%");
    }

    /**
     * 按时间区间 + 销售编号前缀查询销售记录
     *
     * @param start  开始日期（含当天），null 表示不限开始
     * @param end    结束右端点日期（不含当天），null 表示不限结束
     * @param prefix 销售编号前缀，null 或空白时退化为纯区间查询
     * @return 区间内编号以前缀开头的销售记录列表（编号正序），没有数据时返回空集合而不是 null
     */
    public List<Sale> findByTimeRangeAndIdPrefix(Date start, Date end, String prefix) {
        // 空白前缀退化为纯区间查询
        if (prefix == null || prefix.trim().isEmpty()) {
            return queryRange(start, end, null, null);
        }
        // 前缀匹配：% 只拼在末尾，保证"从编号开头匹配"
        return queryRange(start, end, "CAST(s.id AS CHAR)", prefix.trim() + "%");
    }

    // 区间 + 关键词的通用联表查询：动态拼 WHERE（时间半开区间 + 可选 LIKE），四个区间查询方法共用
    // likeExpr 是本类内部写死的列表达式（如 m.name），不是用户输入，拼接无注入风险；
    // likePattern 是含通配符的参数值，始终经占位符传入；两者都为 null 时只按区间查
    private List<Sale> queryRange(Date start, Date end, String likeExpr, String likePattern) {
        // 动态拼 WHERE：条件是否生效决定占位符数量，参数按追加顺序填入
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id WHERE 1=1");
        if (start != null) {
            sql.append(" AND s.sale_time >= ?");
        }
        if (end != null) {
            sql.append(" AND s.sale_time < ?");
        }
        if (likeExpr != null) {
            sql.append(" AND ").append(likeExpr).append(" LIKE ?");
        }
        // 统一按销售编号正序
        sql.append(" ORDER BY s.id");
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql.toString());
            // 占位符计数器：按拼 SQL 的条件顺序依次填参
            int index = 1;
            if (start != null) {
                // 开始端点：当天 00:00:00（java.sql.Date 只含日期）
                stmt.setDate(index++, start);
            }
            if (end != null) {
                // 结束右端点：不含该天，故用 <
                stmt.setDate(index++, end);
            }
            if (likeExpr != null) {
                stmt.setString(index++, likePattern);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("按时间区间查询销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 按操作员姓名模糊查询销售记录
     * - 联表 sys_user 按真实姓名过滤，返回的仍是销售记录列
     *
     * @param keyword 操作员姓名关键词，null 时按无效入参返回空集合
     * @return 操作员姓名包含关键词的销售记录列表，没有数据时返回空集合而不是 null
     */
    public List<Sale> findByOperator(String keyword) {
        // 关键词为 null 时按无效入参处理，直接返回空结果，避免拼出 "%null%" 误查
        if (keyword == null) {
            return new ArrayList<>();
        }
        // 表别名 s/u：sale 自身列加 s. 前缀区分；联表带出药品名称与操作员姓名，姓名列与过滤条件共用 sys_user 表
        String sql = "SELECT s.id, s.medicine_id, s.operator_id, s.quantity, s.sale_price, s.total_amount,"
                + " s.sale_time, s.remark, m.name AS medicine_name, m.category, m.specification, m.manufacturer, u.real_name AS operator_name"
                + " FROM sale s JOIN medicine m ON s.medicine_id = m.id JOIN sys_user u ON s.operator_id = u.id"
                + " WHERE u.real_name LIKE ? ORDER BY s.id";
        List<Sale> saleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            // 前后拼 % 表示"包含关键词"的任意位置匹配，通配符在 DAO 层拼接，调用方只传原始关键词
            stmt.setString(1, "%" + keyword + "%");
            rs = stmt.executeQuery();
            // 结果集可能有多行，逐行转换后加入集合
            while (rs.next()) {
                saleList.add(mapJoinRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("按操作员查询销售记录失败：" + e.getMessage());
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return saleList;
    }

    /**
     * 把联表查询的结果集当前行转换为销售记录对象
     * - 在 mapRow 基础上额外读取联表带来的药品名称、类别、规格、厂家、操作员姓名五个展示列
     *
     * @param rs 指向当前行的结果集
     * @return 填充好字段的销售记录对象
     * @throws SQLException 读取列失败时抛出，由调用方统一处理
     */
    private Sale mapJoinRow(ResultSet rs) throws SQLException {
        Sale sale = mapRow(rs);
        // 名称列只存在于 JOIN 查询的结果集中，供表格展示，不写回 sale 表
        sale.setMedicineName(rs.getString("medicine_name"));
        // 类别、规格、厂家直接用 medicine 表的原列名（sale 表没有同名列，不会混淆）
        sale.setCategory(rs.getString("category"));
        sale.setSpecification(rs.getString("specification"));
        sale.setManufacturer(rs.getString("manufacturer"));
        sale.setOperatorName(rs.getString("operator_name"));
        return sale;
    }

    /**
     * 把结果集当前行转换为销售记录对象
     *
     * @param rs 指向当前行的结果集
     * @return 填充好字段的销售记录对象
     * @throws SQLException 读取列失败时抛出，由调用方统一处理
     */
    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setMedicineId(rs.getInt("medicine_id"));
        sale.setOperatorId(rs.getInt("operator_id"));
        sale.setQuantity(rs.getInt("quantity"));
        sale.setSalePrice(rs.getBigDecimal("sale_price"));
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));
        // sale_time 是必填列，取出 Timestamp 后直接转成 LocalDateTime，无需判空
        sale.setSaleTime(rs.getTimestamp("sale_time").toLocalDateTime());
        sale.setRemark(rs.getString("remark"));
        return sale;
    }

}
