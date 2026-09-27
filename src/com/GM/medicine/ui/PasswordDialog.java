package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 中表单 / 底按钮 两段组织
import java.awt.BorderLayout;
// 导入 Dimension：确定按钮尺寸
import java.awt.Dimension;
// 导入 FlowLayout：底部按钮区居中排列
import java.awt.FlowLayout;
// 导入 GridBagConstraints：配合 GridBagLayout 控制表单行位置与对齐
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：表单两列布局，左标签右密码框
import java.awt.GridBagLayout;
// 导入 Insets：表单行间距
import java.awt.Insets;
// 导入 Window：构造方法接收的父窗口类型（主窗口等顶层窗口都适用）
import java.awt.Window;
// 导入 ActionEvent：确认按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为确认、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：表单区留白
import javax.swing.BorderFactory;
// 导入 JButton：确认与取消按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，承载修改密码表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JPasswordField：密码输入框，输入内容用圆点遮盖
import javax.swing.JPasswordField;
// 导入 SysUser：当前登录用户实体类型
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserPasswordDTO：修改密码参数对象（当前密码 + 新密码）
import com.GM.medicine.pojo.dto.SysUserPasswordDTO;
// 导入 SysUserService：保存统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SysUserService;

/**
 * - 修改密码对话框：当前登录用户修改自己的密码
 * - 表单三项：当前密码、新密码、确认密码；底部确认 / 取消按钮
 * - UI 只做本地基础校验（必填、两次一致）；旧密码 BCrypt 校验、新旧不同校验、新密码哈希入库都在 SysUserService 完成
 */
public class PasswordDialog extends JDialog {

    // 用户业务对象：保存统一通过它调用
    private SysUserService userService = new SysUserService();

    // 当前登录用户：修改密码的身份来源
    private SysUser currentUser;

    // 当前密码输入框
    private JPasswordField oldField = UiTheme.createPasswordField(15); // 【可修改参数】当前密码框推荐列数（15）

    // 新密码输入框
    private JPasswordField newField = UiTheme.createPasswordField(15); // 【可修改参数】新密码框推荐列数（15）

    // 确认密码输入框：再次输入新密码防输错
    private JPasswordField confirmField = UiTheme.createPasswordField(15); // 【可修改参数】确认密码框推荐列数（15）

    // 确认按钮：本地校验通过后调 Service 保存，登录类配色（主动作）
    private JButton okButton = UiTheme.createRoundButton("确认", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：放弃修改直接关闭，退出类配色
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    /**
     * 构造修改密码对话框
     *
     * @param currentUser 当前登录用户，由主窗口传入
     * @param owner       父窗口，对话框在其上居中
     */
    public PasswordDialog(SysUser currentUser, Window owner) {
        super(owner);
        // 保存当前登录用户：保存时作为修改密码的身份
        this.currentUser = currentUser;
        // 模态：确认或取消前不能操作主窗口
        setModal(true);
        // 标题与用途一致
        setTitle("修改密码");
        // 组装表单与按钮
        initComponents();
        setSize(380, 250); // 【可修改参数】对话框大小（宽 380 / 高 250）
        // 在父窗口上居中
        setLocationRelativeTo(owner);
    }

    // 组装中部表单与底部按钮
    private void initComponents() {
        // 中部：三行表单（当前密码 / 新密码 / 确认密码），白底与对话框统一
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiTheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        // 行间距：标签与输入框之间留白
        gbc.insets = new Insets(8, 0, 8, 6); // 【可修改参数】表单行间距（上下 8 / 标签与框间 6）
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("当前密码：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        // 与其他输入框同款：关闭不透明填充 + 浅灰圆角描边（白底由工厂自绘）
        oldField.setOpaque(false);
        oldField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(oldField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("新密码：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        newField.setOpaque(false);
        newField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(newField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("确认密码：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        confirmField.setOpaque(false);
        confirmField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(confirmField, gbc);
        // 确认密码框内回车等同点击确认
        confirmField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doConfirm();
            }
        });
        add(formPanel, BorderLayout.CENTER);

        // 底部：确认 + 取消按钮行，居中
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12)); // 【可修改参数】按钮区间距（12）
        buttonPanel.setBackground(UiTheme.WHITE);
        // 确认按钮：登录类配色（提交修改是主动作），点击校验并保存
        okButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】确认按钮大小（宽 90 / 高 32）
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doConfirm();
            }
        });
        buttonPanel.add(okButton);
        // 取消按钮：退出类配色，直接关闭不保存
        cancelButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】取消按钮大小（宽 90 / 高 32）
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // “确认”：本地校验通过后调 Service 保存；成功提示并关闭，失败统一提示并清空重填
    private void doConfirm() {
        // 取出三个密码框内容（密码不 trim，空格属于合法密码字符）
        String oldPassword = new String(oldField.getPassword());
        String newPassword = new String(newField.getPassword());
        String confirmPassword = new String(confirmField.getPassword());
        // 依次本地校验：三个框都必填，失败聚焦对应框
        if (oldPassword.isEmpty()) {
            UiTheme.showMessageDialog(this, "请输入当前密码");
            oldField.requestFocusInWindow();
            return;
        }
        if (newPassword.isEmpty()) {
            UiTheme.showMessageDialog(this, "请输入新密码");
            newField.requestFocusInWindow();
            return;
        }
        if (confirmPassword.isEmpty()) {
            UiTheme.showMessageDialog(this, "请再次输入新密码");
            confirmField.requestFocusInWindow();
            return;
        }
        // 两次输入的新密码必须一致
        if (!newPassword.equals(confirmPassword)) {
            UiTheme.showMessageDialog(this, "两次输入的新密码不一致");
            confirmField.setText("");
            confirmField.requestFocusInWindow();
            return;
        }
        // 新密码不能与当前密码相同：两个都是明文，直接比对提前拦截（Service 内还有 BCrypt 流程兜底）
        if (newPassword.equals(oldPassword)) {
            UiTheme.showMessageDialog(this, "修改失败，新密码与旧密码相同");
            newField.setText("");
            confirmField.setText("");
            newField.requestFocusInWindow();
            return;
        }
        // 组装 DTO 交给 Service：旧密码 BCrypt 校验、新旧不同校验、新密码哈希入库都在 Service 完成
        SysUserPasswordDTO passwordDTO = new SysUserPasswordDTO();
        passwordDTO.setOldPassword(oldPassword);
        passwordDTO.setNewPassword(newPassword);
        if (userService.changePassword(currentUser, passwordDTO)) {
            // 成功提示后关闭；修改的是数据库密码，当前登录会话不受影响
            UiTheme.showMessageDialog(this, "密码修改成功，请牢记新密码");
            dispose();
        } else {
            // 失败统一提示（当前密码错误最常见），清空三个框方便重填
            UiTheme.showMessageDialog(this, "修改失败，请检查当前密码是否正确");
            oldField.setText("");
            newField.setText("");
            confirmField.setText("");
            oldField.requestFocusInWindow();
        }
    }
}
