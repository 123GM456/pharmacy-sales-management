package com.GM.medicine.service;

// 导入 BigDecimal：单价、总金额与零值的比较需要用它
import java.math.BigDecimal;
// 导入 Date：根据销售日期查询销售记录时需要它
import java.sql.Date;
// 导入 List：findAll 方法返回的销售记录集合类型
import java.util.List;
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
     * @return 新增成功返回 true；对象为空、必填字段缺失、数量或金额非法或插入失败返回 false
     */
    public boolean addSale(SaleDTO saleDTO) {
        if (saleDTO == null) {
            System.out.println("新增销售记录失败：销售记录对象不能为空");
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