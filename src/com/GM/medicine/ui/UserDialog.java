package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 中表单 / 底按钮 两段组织
import java.awt.BorderLayout;
// 导入 Dimension：按钮尺寸
import java.awt.Dimension;
// 导入 FlowLayout：底部按钮区居中排列
import java.awt.FlowLayout;
// 导入 GridBagConstraints：配合 GridBagLayout 控制表单行位置与对齐
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：表单两列布局，左标签右输入框
import java.awt.GridBagLayout;
// 导入 Insets：表单行间距
import java.awt.Insets;
// 导入 Window：构造方法接收的父窗口类型（主窗口等顶层窗口都适用）
import java.awt.Window;
// 导入 ActionEvent：按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为保存、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：表单区留白
import javax.swing.BorderFactory;
// 导入 JButton：保存与取消按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，承载新增用户的表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JTextField：各文本输入框
import javax.swing.JTextField;
// 导入 SysUser：新增的用户实体类型
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserService：保存统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SysUserService;

/**
 * - 新增用户对话框（仅管理员使用）
 * - 表单：姓名、手机号；用户名由系统自动生成（管理员固定 00000，员工随机 00001~99999），不能手动输入
 * - 状态默认正常、初始密码由 Service 用 BCrypt 加密默认密码 123456；UI 只做基础输入校验（必填、手机号格式）
 */
public class UserDialog extends JDialog {

    // 用户业务对象：保存统一通过它完成
    private SysUserService userService = new SysUserService();

    // 当前登录用户（操作者）：Service 保存时校验其管理员权限
    private SysUser operator;

    // 姓名输入框
    private JTextField realNameField = UiTheme.createTextField(14); // 【可修改参数】姓名框推荐列数（14）

    // 手机号输入框
    private JTextField phoneField = UiTheme.createTextField(14); // 【可修改参数】手机号框推荐列数（14）

    // 保存按钮：主题绿圆角+描边，与登录按钮同款
    private JButton saveButton = UiTheme.createRoundButton("保存", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：浅灰圆角+描边，与登录界面"退出"按钮同款
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    /**
     * 构造新增用户对话框
     *
     * @param owner       父窗口，对话框在其上居中
     * @param currentUser 当前登录用户（操作者），保存时传给 Service 做权限校验
     */
    public UserDialog(Window owner, SysUser currentUser) {
        super(owner);
        // 保存操作者身份：Service 保存时校验其管理员权限
        this.operator = currentUser;
        // 模态：保存或取消前不能操作主窗口
        setModal(true);
        setTitle("新增用户");
        // 组装表单与按钮
        initComponents();
        setSize(360, 210); // 【可修改参数】对话框大小（宽 360 / 高 210）
        // 在父窗口上居中
        setLocationRelativeTo(owner);
    }

    // 组装中部表单与底部按钮
    private void initComponents() {
        // 中部：表单白底
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiTheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        // 行间距：标签与输入框之间留白
        gbc.insets = new Insets(8, 0, 8, 6); // 【可修改参数】表单行间距（上下 8 / 标签与框间 6）
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("姓名：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        realNameField.setOpaque(false);
        realNameField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(realNameField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("手机号：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        phoneField.setOpaque(false);
        phoneField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(phoneField, gbc);
        add(formPanel, BorderLayout.CENTER);

        // 底部：保存 + 取消按钮行，居中
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12)); // 【可修改参数】按钮区间距（12）
        buttonPanel.setBackground(UiTheme.WHITE);
        // 保存按钮：登录类配色（提交是主动作），点击校验并保存
        saveButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】保存按钮大小（宽 90 / 高 32）
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        buttonPanel.add(saveButton);
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

    // “保存”：本地校验通过后调 Service 保存；成功关闭对话框，失败统一提示留在窗口
    private void doSave() {
        // 取出输入内容并去掉首尾空格（用户名由 Service 自动生成，界面不采集）
        String realName = realNameField.getText().trim();
        String phone = phoneField.getText().trim();
        // 依次本地校验，失败聚焦对应输入框
        if (realName.isEmpty()) {
            UiTheme.showMessageDialog(this, "姓名不能为空");
            realNameField.requestFocusInWindow();
            return;
        }
        if (phone.isEmpty()) {
            UiTheme.showMessageDialog(this, "手机号不能为空");
            phoneField.requestFocusInWindow();
            return;
        }
        // 手机号格式校验：11 位国内手机号（1 开头、第二位 3-9），与个人信息页口径一致
        if (!phone.matches("1[3-9]\\d{9}")) {
            UiTheme.showMessageDialog(this, "手机号格式不正确（应为 11 位国内手机号）");
            phoneField.requestFocusInWindow();
            return;
        }
        // 组装新用户交给 Service：用户名自动生成、BCrypt 加密默认密码 123456、写入都在 Service 完成
        SysUser newUser = new SysUser();
        newUser.setRealName(realName);
        newUser.setPhone(phone);
        if (userService.addUser(operator, newUser)) {
            // 成功提示后关闭；列表刷新由 UserPanel 在对话框关闭后统一完成
            UiTheme.showMessageDialog(this, "新增用户成功，初始密码为 123456");
            dispose();
        } else {
            // 失败统一提示：最常见的失败是员工用户名区间已用尽
            UiTheme.showMessageDialog(this, "新增失败：员工用户名可能已用尽或输入无效");
        }
    }
}
