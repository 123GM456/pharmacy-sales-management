package com.GM.medicine.pojo.dto;

/**
 * 销售数据传输对象（DTO）
 */
public class SaleDTO {

    private Integer medicineId;

    private Integer quantity;

    private String remark;

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
