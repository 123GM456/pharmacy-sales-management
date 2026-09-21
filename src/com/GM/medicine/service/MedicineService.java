package com.GM.medicine.service;

// 导入 BigDecimal：价格与零值的比较需要用它
import java.math.BigDecimal;
// 导入 List：findAll 方法返回的用户集合类型
import java.util.List;
// 导入 MedicineDao：数据库访问通过它完成
import com.GM.medicine.dao.MedicineDao;
// 导入 Medicine：业务方法操作的药品实体类型
import com.GM.medicine.pojo.entity.Medicine;

/**
 * - 药品业务类
 * - 负责药品新增等业务规则处理
 * - 介于界面层与 MedicineDao 之间，本类只做业务校验与规则判断，不编写 JDBC 代码
 */
public class MedicineService {

    // 数据访问对象，负责真正读写数据库，业务校验通过后才调用它
    private MedicineDao medicineDao = new MedicineDao();

    /**
     * 新增药品，新增前校验名称与销售价格
     *
     * @param medicine 待新增的药品对象
     * @return 新增成功返回 true；名称为空、价格非法或插入失败返回 false
     */
    public boolean addMedicine(Medicine medicine) {
        // 校验药品名称是否为空
        if (medicine.getName() == null || medicine.getName().isEmpty()) {
            System.out.println("新增药品失败：药品名称为空");
            return false;
        }
        if (medicine.getCategory() == null || medicine.getCategory().isEmpty()) {
            System.out.println("新增药品失败：药品分类不能为空");
            return false;
        }
        if (medicine.getSpecification() == null || medicine.getSpecification().isEmpty()) {
            System.out.println("新增药品失败：药品规格不能为空");
            return false;
        }
        if (medicine.getManufacturer() == null || medicine.getManufacturer().isEmpty()) {
            System.out.println("新增药品失败：生产厂家不能为空");
            return false;
        }
        if (medicine.getBatchNumber() == null || medicine.getBatchNumber().isEmpty()) {
            System.out.println("新增药品失败：批次号不能为空");
            return false;
        }
        if (medicine.getPurchasePrice() == null || medicine.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("新增药品失败：采购价格必须大于 0");
            return false;
        }
        // 校验药品价格是否为负数
        if (medicine.getSalePrice() == null || medicine.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("新增药品失败：销售价格必须大于 0");
            return false;
        }
        if (medicine.getStock() <= 0) {
            System.out.println("新增药品失败：库存数量必须大于 0");
            return false;
        }
        if(medicine.getWarningStock() <= 0) {
            System.out.println("新增药品失败：预警库存数量必须大于 0");
            return false;
        }
        if (medicine.getProductionDate() == null) {
            System.out.println("新增药品失败：生产日期不能为空");
            return false;
        }
        if (medicine.getExpiryDate() == null) {
            System.out.println("新增药品失败：过期日期不能为空");
            return false;
        }
        boolean result = medicineDao.add(medicine);
        // 按插入结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("新增药品成功：" + medicine.getName());
        } else {
            System.out.println("新增药品失败：数据库写入失败");
        }
        return result;
    }

    /**
     * 更新药品信息
     *
     * @param medicine 待更新的药品对象
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateMedicine(Medicine medicine) {
        // 校验药品名称是否为空
        if (medicine.getName() == null || medicine.getName().isEmpty()) {
            System.out.println("更新药品失败：药品名称为空");
            return false;
        }
        if (medicine.getCategory() == null || medicine.getCategory().isEmpty()) {
            System.out.println("更新药品失败：药品分类不能为空");
            return false;
        }
        if (medicine.getSpecification() == null || medicine.getSpecification().isEmpty()) {
            System.out.println("更新药品失败：药品规格不能为空");
            return false;
        }
        if (medicine.getManufacturer() == null || medicine.getManufacturer().isEmpty()) {
            System.out.println("更新药品失败：生产厂家不能为空");
            return false;
        }
        if (medicine.getBatchNumber() == null || medicine.getBatchNumber().isEmpty()) {
            System.out.println("更新药品失败：批次号不能为空");
            return false;
        }
        if (medicine.getPurchasePrice() == null || medicine.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("更新药品失败：采购价格必须大于 0");
            return false;
        }
        // 校验药品价格是否为负数
        if (medicine.getSalePrice() == null || medicine.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("更新药品失败：销售价格必须大于 0");
            return false;
        }
        if (medicine.getStock() <= 0) {
            System.out.println("更新药品失败：库存数量必须大于 0");
            return false;
        }
        if(medicine.getWarningStock() <= 0) {
            System.out.println("更新药品失败：预警库存数量必须大于 0");
            return false;
        }
        if (medicine.getProductionDate() == null) {
            System.out.println("更新药品失败：生产日期不能为空");
            return false;
        }
        if (medicine.getExpiryDate() == null) {
            System.out.println("更新药品失败：过期日期不能为空");
            return false;
        }
        boolean result = medicineDao.update(medicine);
        // 按更新结果显示不同的提示，便于控制台测试时确认本次操作是成功还是失败
        if (result) {
            System.out.println("更新药品成功：" + medicine.getName());
        } else {
            System.out.println("更新药品失败：药品不存在或数据库更新失败");
        }
        return result;
    }

    public Medicine findById(Integer id) {
        Medicine medicine = medicineDao.findById(id);
        if (medicine == null) {
            System.out.println("查询药品失败：编号 " + id + " 不存在");
        } else {
            System.out.println("查询药品成功：" + medicine);
        }
        return medicine;
    }

    public List<Medicine> findAll() {
        List<Medicine> medicines = medicineDao.findAll();
        if (medicines.isEmpty()) {
            System.out.println("查询所有药品失败：数据库中没有药品数据");
        } else {
            System.out.println("查询所有药品成功：共 " + medicines.size() + " 条数据");
        }
        return medicines;
    }


}
