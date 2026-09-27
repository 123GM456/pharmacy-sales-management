package com.GM.medicine.service;

// 导入 BigDecimal：单价、总金额与零值的比较需要用它
import java.math.BigDecimal;
// 导入 Date：根据销售日期查询销售记录时需要它
import java.sql.Date;
// 导入 LocalDate：范围查询的日期参数类型（今日/本周/本月/自定义区间的边界计算）
import java.time.LocalDate;
// 导入 DayOfWeek：本周范围取"本周一"用
import java.time.DayOfWeek;
// 导入 List：findAll 方法返回的销售记录集合类型
import java.util.List;
// 导入 ArrayList：关键词为空时返回空集合，避免无效入参传到 DAO
import java.util.ArrayList;
// 导入 MedicineDao：根据药品编号查询药品信息时需要它
import com.GM.medicine.dao.MedicineDao;
// 导入 SaleDao：数据库访问通过它完成
import com.GM.medicine.dao.SaleDao;
// 导入 SaleDTO：业务方法操作的销售记录数据传输对象类型
import com.GM.medicine.pojo.dto.SaleDTO;
// 导入 Medicine：根据药品编号查询药品信息时需要它
import com.GM.medicine.pojo.entity.Medicine;
// 导入 Sale：业务方法操作的销售记录实体类型
import com.GM.medicine.pojo.entity.Sale;

/**
 * - 销售记录业务类
 * - 负责销售过程的业务规则处理，如销售时扣减库存、按数量与单价计算总金额、记录操作员
 * - 介于界面层与 SaleDao 之间，本类只做业务校验与规则判断，不编写 JDBC 代码
 */
public class SaleService {

    // 数据访问对象，负责真正读写数据库，业务校验通过后才调用它
    private SaleDao saleDao = new SaleDao();

    /**
     * 新增销售记录，新增前校验必填字段
     *
     * @param saleDTO 待新增的销售记录对象
     * @param operatorId 操作员编号（当前登录用户），销售记录必须落到具体操作员头上
     * @return 新增成功返回 true；对象为空、必填字段缺失、数量或金额非法或插入失败返回 false
     */
    public boolean addSale(SaleDTO saleDTO, Integer operatorId) {
        if (saleDTO == null) {
            System.out.println("新增销售记录失败：销售记录对象不能为空");
            return false;
        }
        // sale.operator_id 是必填外键，缺身份直接拒绝，避免空值插入
        if (operatorId == null) {
            System.out.println("新增销售记录失败：操作员编号不能为空");
            return false;
        }
        if (saleDTO.getMedicineId() == null) {
            System.out.println("新增销售记录失败：药品编号不能为空");
            return false;
        }
        if (saleDTO.getQuantity() == null || saleDTO.getQuantity() <= 0) {
            System.out.println("新增销售记录失败：销售数量必须大于 0");
            return false;
        }
        // 药品数据访问对象，销售时需要按药品编号取出药品信息
        MedicineDao medicineDao = new MedicineDao();
        Medicine medicine = medicineDao.findById(saleDTO.getMedicineId());
        if (medicine == null) {
            System.out.println("新增销售记录失败：药品编号 " + saleDTO.getMedicineId() + " 不存在");
            return false;
        }
        if (medicine.getStock() < saleDTO.getQuantity()) {
            System.out.println("新增销售记录失败：库存不足");
            return false;
        }
        BigDecimal salePrice = medicine.getSalePrice();
        // 计算总金额
        BigDecimal totalPrice = salePrice.multiply(BigDecimal.valueOf(saleDTO.getQuantity()));
        // sale_time 由数据库默认值 CURRENT_TIMESTAMP 在插入时自动填充，调用方无需传值
        Sale sale = new Sale(saleDTO);
        sale.setOperatorId(operatorId);
        sale.setSalePrice(salePrice);
        sale.setTotalAmount(totalPrice);
        boolean result = saleDao.add(sale);
        // 按插入结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            medicineDao.decreaseStock(saleDTO.getMedicineId(), saleDTO.getQuantity());
            System.out.println("新增销售记录：药品编号 " + saleDTO.getMedicineId());
        } else {
            System.out.println("新增销售记录失败：数据库写入失败");
        }
        return result;
    }

    /**
     * 根据编号查询单条销售记录
     *
     * @param id 销售记录编号
     * @return 查询到的销售记录对象，编号为空或记录不存在时返回 null
     */
    public Sale findById(Integer id) {
        if (id == null) {
            System.out.println("查询销售记录失败：记录编号不能为空");
            return null;
        }
        Sale sale = saleDao.findById(id);
        if (sale == null) {
            System.out.println("查询销售记录：编号 " + id + " 不存在");
        } else {
            System.out.println("查询销售记录：" + sale);
        }
        return sale;
    }

    /**
     * 查询全部销售记录
     *
     * @return 销售记录列表，没有数据时为空集合
     */
    public List<Sale> findAll() {
        List<Sale> sales = saleDao.findAll();
        if (sales.isEmpty()) {
            System.out.println("查询所有销售记录：当前没有销售记录数据");
        } else {
            System.out.println("查询所有销售记录：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 查询今日销售记录
     *
     * @return 今日销售记录列表，没有数据时为空集合
     */
    public List<Sale> findTodaySales() {
        List<Sale> sales = saleDao.findTodaySales();
        if (sales.isEmpty()) {
            System.out.println("查询今日销售：今天没有销售记录");
        } else {
            System.out.println("查询今日销售：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 查询本周销售记录（周一为一周的第一天）
     *
     * @return 本周销售记录列表，没有数据时为空集合
     */
    public List<Sale> findWeekSales() {
        List<Sale> sales = saleDao.findWeekSales();
        if (sales.isEmpty()) {
            System.out.println("查询本周销售：本周没有销售记录");
        } else {
            System.out.println("查询本周销售：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 查询本月销售记录
     *
     * @return 本月销售记录列表，没有数据时为空集合
     */
    public List<Sale> findMonthSales() {
        List<Sale> sales = saleDao.findMonthSales();
        if (sales.isEmpty()) {
            System.out.println("查询本月销售：本月没有销售记录");
        } else {
            System.out.println("查询本月销售：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 按药品名称模糊查询销售记录
     *
     * @param keyword 药品名称关键词，空白时返回空集合
     * @return 药品名称包含关键词的销售记录列表
     */
    public List<Sale> findByMedicineName(String keyword) {
        // 空白关键词不产生数据库查询，直接返回空结果
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("按药品名称查询销售记录：关键词为空");
            return new ArrayList<>();
        }
        List<Sale> sales = saleDao.findByMedicineName(keyword.trim());
        if (sales.isEmpty()) {
            System.out.println("按药品名称查询销售记录：没有匹配的记录");
        } else {
            System.out.println("按药品名称查询销售记录：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 按销售编号前缀查询销售记录：只匹配编号开头部分（如 123 能查到 123456，234 / 456 不行），结果按编号正序
     *
     * @param idPrefix 销售编号前缀，空白或非数字时返回空集合
     * @return 编号以前缀开头的销售记录列表
     */
    public List<Sale> findByIdPrefix(String idPrefix) {
        // 空白前缀不产生数据库查询，直接返回空结果
        if (idPrefix == null || idPrefix.trim().isEmpty()) {
            System.out.println("按编号查询销售记录：前缀为空");
            return new ArrayList<>();
        }
        // 编号是纯数字：非数字前缀在数据库里不可能匹配到编号，提前拦截省一次查询
        if (!idPrefix.trim().matches("\\d+")) {
            System.out.println("按编号查询销售记录：前缀必须为数字");
            return new ArrayList<>();
        }
        List<Sale> sales = saleDao.findByIdPrefix(idPrefix.trim());
        if (sales.isEmpty()) {
            System.out.println("按编号查询销售记录：没有匹配的记录");
        } else {
            System.out.println("按编号查询销售记录：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 销售组合查询统一入口：按"时间范围 + 查询类型"两个维度组合查询
     * - 范围：all 不限；today 今日；week 本周（周一起）；month 本月（1 号起）；custom 自定义 [开始, 结束+1 天)
     * - 自定义的结束日期为 null 时按开始日期当天查询（右端点 = 开始 + 1 天）
     * - 关键词空白时退化为纯范围查询；编号前缀非数字直接返回空集合
     *
     * @param rangeKey    时间范围标识：all / today / week / month / custom
     * @param searchType  查询类型标识：none 不按关键词 / name 药品名称 / operator 操作员 / id 编号前缀
     * @param keyword     关键词（模糊词或编号前缀），可为空白
     * @param customStart 自定义开始日期（仅范围是 custom 时使用，界面上已校验必填）
     * @param customEnd   自定义结束日期，可为 null 表示与开始同一天
     * @return 组合条件下的销售记录列表
     */
    public List<Sale> searchSales(String rangeKey, String searchType, String keyword, LocalDate customStart, LocalDate customEnd) {
        // 一、按范围标识计算半开区间 [start, end)：all 不限两端，其余范围在 Service 层算好边界
        LocalDate start = null;
        LocalDate end = null;
        if ("custom".equals(rangeKey)) {
            // 自定义：开始必填（界面已校验），结束为空按开始当天查询（右端点 = 开始 + 1 天）
            start = customStart;
            end = (customEnd == null ? customStart.plusDays(1) : customEnd.plusDays(1));
        } else if ("today".equals(rangeKey)) {
            // 今日：[今天零点, 明天零点)，与 findTodaySales 的 SQL（CURDATE）口径一致
            start = LocalDate.now();
            end = start.plusDays(1);
        } else if ("week".equals(rangeKey)) {
            // 本周：[本周一零点, 下周一零点)，与 findWeekSales 的 SQL（WEEKDAY 周一为 0）口径一致
            start = LocalDate.now().with(DayOfWeek.MONDAY);
            end = start.plusDays(7);
        } else if ("month".equals(rangeKey)) {
            // 本月：[本月 1 号零点, 下月 1 号零点)，与 findMonthSales 的 SQL 口径一致
            start = LocalDate.now().withDayOfMonth(1);
            end = start.plusMonths(1);
        }
        // 二、关键词规整：空白按无关键词处理
        String trimmed = (keyword == null ? "" : keyword.trim());
        boolean hasKeyword = !trimmed.isEmpty();
        // 三、编号前缀必须是纯数字：非数字不可能匹配到编号，提前拦截省一次查询
        if ("id".equals(searchType) && hasKeyword && !trimmed.matches("\\d+")) {
            System.out.println("销售组合查询：编号前缀必须为数字");
            return new ArrayList<>();
        }
        // 四、LocalDate 转 java.sql.Date（只含日期，即当天零点）后交给 DAO
        Date startDate = (start == null ? null : Date.valueOf(start));
        Date endDate = (end == null ? null : Date.valueOf(end));
        // 五、按查询类型分发到对应的区间查询方法
        List<Sale> sales;
        if ("name".equals(searchType)) {
            sales = saleDao.findByTimeRangeAndMedicineName(startDate, endDate, hasKeyword ? trimmed : null);
        } else if ("operator".equals(searchType)) {
            sales = saleDao.findByTimeRangeAndOperator(startDate, endDate, hasKeyword ? trimmed : null);
        } else if ("id".equals(searchType)) {
            sales = saleDao.findByTimeRangeAndIdPrefix(startDate, endDate, hasKeyword ? trimmed : null);
        } else {
            // none：不按关键词，只按时间范围
            sales = saleDao.findByTimeRange(startDate, endDate);
        }
        System.out.println("销售组合查询：范围=" + rangeKey + "，类型=" + searchType + "，共 " + sales.size() + " 条数据");
        return sales;
    }

    /**
     * 按操作员姓名模糊查询销售记录
     *
     * @param keyword 操作员姓名关键词，空白时返回空集合
     * @return 操作员姓名包含关键词的销售记录列表
     */
    public List<Sale> findByOperator(String keyword) {
        // 空白关键词不产生数据库查询，直接返回空结果
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("按操作员查询销售记录：关键词为空");
            return new ArrayList<>();
        }
        List<Sale> sales = saleDao.findByOperator(keyword.trim());
        if (sales.isEmpty()) {
            System.out.println("按操作员查询销售记录：没有匹配的记录");
        } else {
            System.out.println("按操作员查询销售记录：共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 根据销售日期查询当天的全部销售记录
     *
     * @param date 销售日期，只精确到日
     * @return 当天查询到的销售记录列表，当天没有数据时为空集合
     */
    public List<Sale> findByDate(Date date) {
        if (date == null) {
            System.out.println("查询销售记录失败：销售日期不能为空");
            return null;
        }
        List<Sale> sales = saleDao.findByDate(date);
        if (sales.isEmpty()) {
            System.out.println("查询销售记录：日期 " + date + " 当天没有销售记录");
        } else {
            System.out.println("查询销售记录：日期 " + date + " 共 " + sales.size() + " 条数据");
        }
        return sales;
    }

    /**
     * 根据药品编号查询销售记录
     *
     * @param medicineId 药品编号
     * @return 查询到的销售记录列表，药品编号不存在时为空集合
     */
    public List<Sale> findByMedicineId(Integer medicineId) {
        if (medicineId == null) {
            System.out.println("查询销售记录失败：药品编号不能为空");
            return null;
        }
        List<Sale> sales = saleDao.findByMedicineId(medicineId);
        if (sales.isEmpty()) {
            System.out.println("查询销售记录：药品编号 " + medicineId + " 不存在");
        } else {
            System.out.println("查询销售记录：" + sales);
        }
        return sales;
    }

    public List<Sale> findByOrderId(Integer orderId) {
        if (orderId == null) {
            System.out.println("查询销售记录失败：订单编号不能为空");
            return null;
        }
        List<Sale> sales = saleDao.findByOrderId(orderId);
        if (sales.isEmpty()) {
            System.out.println("查询销售记录：订单编号 " + orderId + " 不存在");
        } else {
            System.out.println("查询销售记录：" + sales);
        }
        return sales;
    }
}