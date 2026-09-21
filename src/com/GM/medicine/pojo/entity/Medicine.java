package com.GM.medicine.pojo.entity;

// 导入 BigDecimal：精确十进制类型，用来存金额，避免 double 的浮点误差
import java.math.BigDecimal;
// 导入 LocalDate：只表示年月日，不含时分秒，对应数据库的 DATE 类型
import java.time.LocalDate;
// 导入 LocalDateTime：表示年月日时分秒，对应数据库的 DATETIME 类型
import java.time.LocalDateTime;

public class Medicine {

    private Integer id;

    private String name;

    private String category;

    private String specification; // 规格

    private String manufacturer; // 生产厂家

    private String batchNumber; // 生产批号

    private BigDecimal purchasePrice; // 进货价格

    private BigDecimal salePrice; // 销售价格

    private Integer stock;

    private Integer warningStock; // 库存预警值

    private LocalDate productionDate; // 生产日期

    private LocalDate expiryDate; // 有效期

    private Integer status;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getWarningStock() {
        return warningStock;
    }

    public void setWarningStock(Integer warningStock) {
        this.warningStock = warningStock;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

}