package com.GM.medicine.pojo.dto;

/**
 * - 用户修改密码数据传输对象
 * - 封装修改自己密码所需的旧密码与新密码
 * - 用户编号不在此封装，由服务端从当前登录用户取得，防止越权修改他人密码
 */
public class SysUserPasswordDTO {

    private String oldPassword; // 旧密码

    private String newPassword; // 新密码

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
