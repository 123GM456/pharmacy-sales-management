package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 中表单 / 底按钮 两段组织
import java.awt.BorderLayout;
// 导入 Dimension：确定按钮尺寸
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
// 导入 ActionEvent：确定按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为确定、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 JButton：确定与取消按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，承载单个字段的修改表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JTextField：新姓名 / 新手机号输入框
import javax.swing.JTextField;
// 导入 SysUser：当前登录用户实体类型
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserService：保存统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SysUserService;

/**
 * - 个人信息修改弹窗（EditMyInfoDialog）：一次只修改一个字段
 * - editName 为 true 时修改姓名，false 时修改手机号；输入框预填当前值
 * - 点“确定”校验通过后调用 SysUserService.updateMyInfo 保存；点“取消”直接关闭，原数据不受影响
 * - 另一个未修改的字段传当前用户原值，保证 Service 两字段非空校验通过
 */
public class EditMyInfoDialog extends JDialog {

    // 用户业务对象：保存统一通过它调用
    private SysUserService userService = new SysUserService();

    // 当前登录用户：保存时提供用户编号与未修改字段的当前值
    private SysUser currentUser;

    // 修改模式标记：true 修改姓名，false 修改手机号
    private boolean editName;

    // 新值输入框：预填当前值
    private JTextField valueField = UiTheme.createTextField(15); // 【可修改参数】输入框推荐列数（15）

    // 确定按钮：校验通过后保存并关闭，登录类配色（主动作）
    private JButton okButton = UiTheme.createRoundButton("确定", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：放弃修改直接关闭，退出类配色
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    // 保存成功标记：确定且 Service 返回 true 时置为 true，供面板判断是否刷新
    private boolean saved;

    // 保存成功后的新值：面板用它同步当前用户对象
    private String newValue;

    /**
     * 构造个人信息修改弹窗
     *
     * @param owner       父窗口，对话框在其上居中
     * @param currentUser 当前登录用户
     * @param editName    true 修改姓名，false 修改手机号
     */
    public EditMyInfoDialog(Window owner, SysUser currentUser, boolean editName) {
        super(owner);
        // 保存当前用户：保存时取编号与未修改字段的当前值
        this.currentUser = currentUser;
        // 保存修改模式
        this.editName = editName;
        // 模态：确定或取消前不能操作主窗口
        setModal(true);
        // 标题按修改模式区分
        setTitle(editName ? "修改姓名" : "修改手机号");
        // 组装表单与按钮
        initComponents();
        setSize(320, 170); // 【可修改参数】对话框大小（宽 320 / 高 170）
        // 在父窗口上居中
        setLocationRelativeTo(owner);
    }

    /**
     * 判断是否保存成功
     *
     * @return 用户点确定且 Service 保存成功返回 true；取消或保存失败返回 false
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * 获取保存成功后的新值
     *
     * @return 新姓名或新手机号；未保存成功时为 null
     */
    public String getNewValue() {
        return newValue;
    }

    // 组装中部表单与底部按钮
    private void initComponents() {
        // 中部：一行表单（标签 + 输入框），白底与对话框统一
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiTheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        // 行间距：标签与输入框之间留白
        gbc.insets = new Insets(8, 0, 8, 6); // 【可修改参数】表单行间距（上下 8 / 标签与框间 6）
        gbc.anchor = GridBagConstraints.EAST;
        // 标签按修改模式显示对应字段名
        formPanel.add(UiTheme.createLabel(editName ? "新姓名：" : "新手机号：", UiTheme.TEXT_DARK), gbc);
        // 输入框列：自然宽度，预填当前值方便小改
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        valueField.setOpaque(false);
        valueField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        valueField.setText(editName ? currentUser.getRealName() : currentUser.getPhone());
        formPanel.add(valueField, gbc);
        add(formPanel, BorderLayout.CENTER);

        // 底部：确定 + 取消按钮行，居中
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12)); // 【可修改参数】按钮区间距（12）
        buttonPanel.setBackground(UiTheme.WHITE);
        // 确定按钮：登录类配色（保存是主动作），点击校验并保存
        okButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】确定按钮大小（宽 90 / 高 32）
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

    // “确定”：校验输入，通过后调 Service 保存；成功记录新值并关闭，失败弹提示且不关闭
    private void doConfirm() {
        // 取出输入并去掉首尾空格
        String value = valueField.getText().trim();
        // 非空校验：字段名按修改模式区分
        if (value.isEmpty()) {
            UiTheme.showMessageDialog(this, editName ? "姓名不能为空" : "手机号不能为空");
            valueField.requestFocusInWindow();
            return;
        }
        // 手机号格式校验：11 位、1 开头、第二位 3-9（国内手机号段）
        if (!editName && !value.matches("1[3-9]\\d{9}")) {
            UiTheme.showMessageDialog(this, "手机号格式不正确（应为 11 位国内手机号）");
            valueField.requestFocusInWindow();
            return;
        }
        // 组装修改对象：编号取当前用户；未修改的字段传当前用户原值（Service 要求两字段都非空）
        SysUser updateInfo = new SysUser();
        updateInfo.setId(currentUser.getId());
        updateInfo.setRealName(editName ? value : currentUser.getRealName());
        updateInfo.setPhone(editName ? currentUser.getPhone() : value);
        // 调 Service 保存：成功返回 true，失败原因由 Service 打印
        if (userService.updateMyInfo(currentUser, updateInfo)) {
            // 记录保存结果与新值，关闭对话框由面板刷新显示
            saved = true;
            newValue = value;
            dispose();
        } else {
            // 失败统一提示，不关闭对话框方便重试
            UiTheme.showMessageDialog(this, "保存失败：更新未生效，请稍后重试");
        }
    }
}
