package com.GM.medicine.pojo.entity;

// 导入 BigDecimal：精确十进制类型，用来存金额，避免 double 的浮点误差
import java.math.BigDecimal;
// 导入 LocalDateTime：表示年月日时分秒，对应数据库的 DATETIME 类型
import java.time.LocalDateTime;
// 导入 SaleDTO：新增销售记录时界面只提供部分字段，构造器需要从中取值
import com.GM.medicine.pojo.dto.SaleDTO;

public class Sale {

    private Integer id;

    private Integer medicineId; 

    private Integer operatorId; // 操作员编号

    private Integer quantity; // 销售数量

    private BigDecimal salePrice; // 销售单价

    private BigDecimal totalAmount; // 销售总金额

    private LocalDateTime saleTime; 

    private String remark; // 备注

    // 加上下面的 DTO 构造器后编译器不再生成默认无参构造器，而 JDBC 从结果集还原记录时要靠它来实例化
    public Sale() {
    }

    /**
     * 用界面传入的销售数据构造一条销售记录
     *
     * @param saleDTO 携带药品编号、操作员编号、销售数量与备注的传输对象
     */
    public Sale(SaleDTO saleDTO) {
        this.medicineId = saleDTO.getMedicineId();
        this.quantity = saleDTO.getQuantity();
        this.remark = saleDTO.getRemark();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getSaleTime() {
        return saleTime;
    }

    public void setSaleTime(LocalDateTime saleTime) {
        this.saleTime = saleTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}