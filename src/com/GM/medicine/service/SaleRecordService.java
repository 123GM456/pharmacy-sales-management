package com.GM.medicine.service;

// 导入 BigDecimal：单价、总金额与零值的比较需要用它
import java.math.BigDecimal;
// 导入 List：findAll 方法返回的销售记录集合类型
import java.util.List;
// 导入 SaleRecordDao：数据库访问通过它完成
import com.GM.medicine.dao.SaleRecordDao;
// 导入 SaleRecord：业务方法操作的销售记录实体类型
import com.GM.medicine.pojo.entity.SaleRecord;

/**
 * - 销售记录业务类
 * - 负责销售过程的业务规则处理，如销售时扣减库存、按数量与单价计算总金额、记录操作员
 * - 介于界面层与 SaleRecordDao 之间，本类只做业务校验与规则判断，不编写 JDBC 代码
 */
public class SaleRecordService {

    // 数据访问对象，负责真正读写数据库，业务校验通过后才调用它
    private SaleRecordDao saleRecordDao = new SaleRecordDao();

    /**
     * 新增销售记录，新增前校验必填字段
     *
     * @param saleRecord 待新增的销售记录对象
     * @return 新增成功返回 true；对象为空、必填字段缺失、数量或金额非法或插入失败返回 false
     */
    public boolean addSaleRecord(SaleRecord saleRecord) {
        if (saleRecord == null) {
            System.out.println("新增销售记录失败：销售记录对象不能为空");
            return false;
        }
        if (saleRecord.getMedicineId() == null) {
            System.out.println("新增销售记录失败：药品编号不能为空");
            return false;
        }
        if (saleRecord.getOperatorId() == null) {
            System.out.println("新增销售记录失败：操作员编号不能为空");
            return false;
        }
        // 数量、单价、总金额都必须大于 0，否则是一条没有意义的销售记录
        if (saleRecord.getQuantity() == null || saleRecord.getQuantity() <= 0) {
            System.out.println("新增销售记录失败：销售数量必须大于 0");
            return false;
        }
        if (saleRecord.getUnitPrice() == null || saleRecord.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("新增销售记录失败：销售单价必须大于 0");
            return false;
        }
        if (saleRecord.getTotalAmount() == null || saleRecord.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("新增销售记录失败：销售总金额必须大于 0");
            return false;
        }
        // sale_time 列没有数据库默认值，为空时数据库会直接报错，这里先拦截
        if (saleRecord.getSaleTime() == null) {
            System.out.println("新增销售记录失败：销售时间不能为空");
            return false;
        }
        boolean result = saleRecordDao.add(saleRecord);
        // 按插入结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("新增销售记录成功：药品编号 " + saleRecord.getMedicineId());
        } else {
            System.out.println("新增销售记录失败：数据库写入失败");
        }
        return result;
    }

    /**
     * 更新销售记录，更新前校验编号与必填字段
     *
     * @param saleRecord 携带新值并包含 id 的销售记录对象
     * @return 更新成功返回 true；对象或编号为空、必填字段缺失、数量或金额非法或更新失败返回 false
     */
    public boolean updateSaleRecord(SaleRecord saleRecord) {
        if (saleRecord == null || saleRecord.getId() == null) {
            System.out.println("更新销售记录失败：销售记录对象或记录编号不能为空");
            return false;
        }
        if (saleRecord.getMedicineId() == null) {
            System.out.println("更新销售记录失败：药品编号不能为空");
            return false;
        }
        if (saleRecord.getOperatorId() == null) {
            System.out.println("更新销售记录失败：操作员编号不能为空");
            return false;
        }
        // 数量、单价、总金额都必须大于 0，否则是一条没有意义的销售记录
        if (saleRecord.getQuantity() == null || saleRecord.getQuantity() <= 0) {
            System.out.println("更新销售记录失败：销售数量必须大于 0");
            return false;
        }
        if (saleRecord.getUnitPrice() == null || saleRecord.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("更新销售记录失败：销售单价必须大于 0");
            return false;
        }
        if (saleRecord.getTotalAmount() == null || saleRecord.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("更新销售记录失败：销售总金额必须大于 0");
            return false;
        }
        // sale_time 列没有数据库默认值，为空时数据库会直接报错，这里先拦截
        if (saleRecord.getSaleTime() == null) {
            System.out.println("更新销售记录失败：销售时间不能为空");
            return false;
        }
        boolean result = saleRecordDao.update(saleRecord);
        // 按更新结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("更新销售记录成功：记录编号 " + saleRecord.getId());
        } else {
            System.out.println("更新销售记录失败：记录不存在或数据库更新失败");
        }
        return result;
    }

    /**
     * 根据编号查询单条销售记录
     *
     * @param id 销售记录编号
     * @return 查询到的销售记录对象，编号为空或记录不存在时返回 null
     */
    public SaleRecord findById(Integer id) {
        if (id == null) {
            System.out.println("查询销售记录失败：记录编号不能为空");
            return null;
        }
        SaleRecord saleRecord = saleRecordDao.findById(id);
        if (saleRecord == null) {
            System.out.println("查询销售记录失败：编号 " + id + " 不存在");
        } else {
            System.out.println("查询销售记录成功：" + saleRecord);
        }
        return saleRecord;
    }

    /**
     * 查询全部销售记录
     *
     * @return 销售记录列表，没有数据时为空集合
     */
    public List<SaleRecord> findAll() {
        List<SaleRecord> saleRecords = saleRecordDao.findAll();
        if (saleRecords.isEmpty()) {
            System.out.println("查询所有销售记录失败：当前没有销售记录数据");
        } else {
            System.out.println("查询所有销售记录成功：共 " + saleRecords.size() + " 条数据");
        }
        return saleRecords;
    }

}