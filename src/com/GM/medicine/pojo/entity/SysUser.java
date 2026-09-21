package com.GM.medicine.pojo.entity;

// 导入 LocalDateTime：表示年月日时分秒，对应数据库的 DATETIME 类型
import java.time.LocalDateTime;

public class SysUser {

    // 管理员角色值，与数据库 role 列的注释保持一致
    public static final int ROLE_ADMIN = 1;

    // 普通用户角色值
    public static final int ROLE_STAFF = 0;

    // 启用状态值，与数据库 status 列的注释保持一致
    public static final int STATUS_ENABLED = 1;

    // 禁用状态值
    public static final int STATUS_DISABLED = 0;

    private Integer id;

    private String userName; 

    private String password;

    private String realName; 

    private String phone;

    private Integer role; // 角色

    private Integer status;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
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