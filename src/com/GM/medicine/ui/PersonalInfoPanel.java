package com.GM.medicine.ui;

// 导入 BorderLayout：卡片占满面板内容区组织
import java.awt.BorderLayout;
// 导入 FlowLayout：卡片底部"修改密码"按钮居中排列
import java.awt.FlowLayout;
// 导入 Font：个人信息展示字体（比全局普通字体大一号）
import java.awt.Font;
// 导入 GridBagConstraints：配合 GridBagLayout 控制信息行位置与对齐
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：信息卡片两列布局，左标签右内容
import java.awt.GridBagLayout;
// 导入 Insets：信息行间距
import java.awt.Insets;
// 导入 ActionEvent：修改按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为修改按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：信息卡片细边框与留白
import javax.swing.BorderFactory;
// 导入 JButton：姓名、手机号旁的修改按钮
import javax.swing.JButton;
// 导入 JLabel：只读信息显示与行标签
import javax.swing.JLabel;
// 导入 JPanel：承载卡片、行内容与按钮的容器
import javax.swing.JPanel;
// 导入 SwingUtilities：getWindowAncestor 找修改弹窗的父窗口
import javax.swing.SwingUtilities;
// 导入 SysUser：当前登录用户实体类型
import com.GM.medicine.pojo.entity.SysUser;

/**
 * - 个人信息面板
 * - 只读展示当前登录用户的个人信息：用户名、姓名、手机号、角色、账号状态
 * - 姓名与手机号旁有"修改"按钮，点击弹出修改弹窗，在弹窗内保存成功后本页与顶栏同步刷新
 * - 卡片底部有"修改密码"按钮（主题绿实心），点击弹出修改密码弹窗
 * - 本面板不直接调 Service 保存（保存在各弹窗内完成），只负责展示与打开弹窗
 */
public class PersonalInfoPanel extends JPanel {

    // 个人信息展示字体：微软雅黑常规 16 号，比全局普通字体大一号，展示页更饱满
    private final Font infoFont = new Font("微软雅黑", Font.PLAIN, 16); // 【可修改参数】个人信息文字大小（16）

    // 当前登录用户：回填数据来源；与主窗口持有的是同一对象，弹窗保存成功后同步修改它
    private SysUser currentUser;

    // 主窗口引用：弹窗保存成功后调用它的 refreshUserBar 同步顶栏姓名显示
    private MainFrame mainFrame;

    // 姓名显示标签：只读，修改通过弹窗完成
    private JLabel realNameValue = new JLabel();

    // 手机号显示标签：只读，修改通过弹窗完成
    private JLabel phoneValue = new JLabel();

    // 用户名显示标签：只读
    private JLabel userNameValue = new JLabel();

    // 角色显示标签：只读
    private JLabel roleValue = new JLabel();

    // 账号状态显示标签：只读
    private JLabel statusValue = new JLabel();

    // 姓名旁的修改按钮：弹出修改姓名弹窗
    private JButton editNameButton = createEditButton();

    // 手机号旁的修改按钮：弹出修改手机号弹窗
    private JButton editPhoneButton = createEditButton();

    // 底部"修改密码"按钮：主题绿实心，与登录按钮同款，点击弹出修改密码弹窗
    private JButton passwordButton = UiTheme.createRoundButton("修改密码", UiTheme.PRIMARY, UiTheme.WHITE);

    /**
     * 构造个人信息面板：保存登录用户与主窗口引用，组装界面并回填数据
     *
     * @param currentUser 当前登录用户，与主窗口持有的是同一对象
     * @param mainFrame   主窗口引用，修改保存成功后用于刷新顶栏姓名显示
     */
    public PersonalInfoPanel(SysUser currentUser, MainFrame mainFrame) {
        // 保存当前登录用户：回填以它为准
        this.currentUser = currentUser;
        // 保存主窗口引用：修改保存成功后刷新顶栏
        this.mainFrame = mainFrame;
        // 面板底色浅灰，与主窗口内容区一致（颜色可修改参数：UiTheme.BG）
        setBackground(UiTheme.BG);
        // 组装信息卡片与只读表单
        initComponents();
        // 回填当前用户信息
        fillInfo();
    }

    // 创建行内"修改"按钮：主题绿圆角实心，与登录按钮同款
    private JButton createEditButton() {
        JButton button = UiTheme.createRoundButton("修改", UiTheme.PRIMARY, UiTheme.WHITE);
        button.setPreferredSize(new java.awt.Dimension(70, 32)); // 【可修改参数】修改按钮大小（宽 70 / 高 32，保证"修改"二字完整显示）
        return button;
    }

    // 组装界面：白色信息卡片占满内容区（标题 + 五行只读信息）
    private void initComponents() {
        // 修改按钮监听：姓名行按钮改姓名，手机号行按钮改手机号
        editNameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEditDialog(true);
            }
        });
        editPhoneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEditDialog(false);
            }
        });
        // 修改密码按钮监听：打开修改密码弹窗
        passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPasswordDialog();
            }
        });
        // 整体 BorderLayout：卡片占满整个内容区，四周留白与其他页面一致
        setLayout(new BorderLayout());
        // 外层留白：卡片不贴面板边缘
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24)); // 【可修改参数】卡片外留白（24）

        // 白色信息卡片：细边框与表格外框同风格
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiTheme.WHITE);
        card.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 1)); // 【可修改参数】卡片边框颜色与粗细

        // 卡片顶部标题：与主窗口顶栏字体一致
        JLabel titleLabel = new JLabel("个人信息", JLabel.LEFT);
        titleLabel.setFont(UiTheme.FONT_TITLE);
        titleLabel.setForeground(UiTheme.TEXT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28)); // 【可修改参数】标题留白（上 24 / 左右 28 / 下 12）
        card.add(titleLabel, BorderLayout.NORTH);

        // 中部信息表单：五行两列（左标签右内容），全部只读
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiTheme.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28)); // 【可修改参数】表单左右留白（28）
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        // 行间距：信息行之间留白（加大让展示页不显空旷）
        gbc.insets = new Insets(14, 0, 14, 10); // 【可修改参数】信息行间距（上下 14 / 标签与内容间 10）

        // 第 1 行：用户名（只读，无修改入口）
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createInfoLabel("用户名："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        styleValueLabel(userNameValue);
        formPanel.add(userNameValue, gbc);

        // 第 2 行：姓名（只读展示 + 右侧修改按钮）
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createInfoLabel("姓名："), gbc);
        // 值列：weightx=1 吃掉剩余宽度，把按钮列推到卡片右缘
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        styleValueLabel(realNameValue);
        formPanel.add(realNameValue, gbc);
        // 按钮列：与表单右缘对齐，各行按钮在同一垂直线上
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(editNameButton, gbc);

        // 第 3 行：手机号（只读展示 + 右侧修改按钮）
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createInfoLabel("手机号："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        styleValueLabel(phoneValue);
        formPanel.add(phoneValue, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(editPhoneButton, gbc);

        // 第 4 行：角色（只读，无修改入口）
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createInfoLabel("角色："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        styleValueLabel(roleValue);
        formPanel.add(roleValue, gbc);

        // 第 5 行：账号状态（只读，无修改入口）
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createInfoLabel("账号状态："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        styleValueLabel(statusValue);
        formPanel.add(statusValue, gbc);

        // 末尾填充行：吃掉卡片剩余的全部高度，让五行信息整体靠上显示（不再垂直居中）
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0; // 【可修改参数】填充行权重（1.0 = 信息栏整体靠上）
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        formPanel.add(filler, gbc);
        card.add(formPanel, BorderLayout.CENTER);

        // 卡片底部按钮区：居中放"修改密码"按钮，作为本页的主动作入口
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16)); // 【可修改参数】底部按钮区留白（上下 16）
        buttonPanel.setBackground(UiTheme.WHITE);
        passwordButton.setPreferredSize(new java.awt.Dimension(120, 36)); // 【可修改参数】修改密码按钮大小（宽 120 / 高 36）
        buttonPanel.add(passwordButton);
        card.add(buttonPanel, BorderLayout.SOUTH);

        // 卡片占满内容区（信息修改与修改密码都在弹窗内完成）
        add(card, BorderLayout.CENTER);
    }

    // 统一只读信息文字样式：展示字体（16 号）+ 深灰
    private void styleValueLabel(JLabel label) {
        label.setFont(infoFont);
        label.setForeground(UiTheme.TEXT_DARK);
    }

    // 创建个人信息行标签：次要灰色 + 展示字体（比全局普通字体大一号）
    private JLabel createInfoLabel(String text) {
        JLabel label = UiTheme.createLabel(text, UiTheme.TEXT_GRAY);
        label.setFont(infoFont);
        return label;
    }

    // 用 currentUser 的最新数据回填全部信息行（弹窗保存成功后也调用它刷新）
    private void fillInfo() {
        realNameValue.setText(currentUser.getRealName() == null || currentUser.getRealName().isEmpty()
                ? "—" : currentUser.getRealName());
        phoneValue.setText(currentUser.getPhone() == null || currentUser.getPhone().isEmpty()
                ? "—" : currentUser.getPhone());
        userNameValue.setText(currentUser.getUserName());
        roleValue.setText(roleText());
        // 状态判空再比较，与角色判断同样的防御方式
        boolean enabled = currentUser.getStatus() != null && currentUser.getStatus() == SysUser.STATUS_ENABLED;
        statusValue.setText(enabled ? "正常" : "禁用");
    }

    // 角色显示文字：与主窗口顶栏 roleText 保持一致（管理员 / 普通用户）
    private String roleText() {
        // 先判空再比较：避免 currentUser.getRole() 返回 null 时空指针异常
        if (currentUser.getRole() != null && currentUser.getRole() == SysUser.ROLE_ADMIN) {
            return "管理员";
        }
        // 其他所有情况都归为"普通用户"（包括 role 为 0 或 null）
        return "普通用户";
    }

    /**
     * 打开修改弹窗：模态弹出，确定保存成功后同步当前用户并刷新本页与顶栏
     *
     * @param editName true 修改姓名，false 修改手机号
     */
    private void openEditDialog(boolean editName) {
        // 模态弹窗：阻塞到确定或取消，取消直接关窗、不影响原数据
        PersonalInfoDialog dialog = new PersonalInfoDialog(SwingUtilities.getWindowAncestor(this), currentUser, editName);
        dialog.setVisible(true);
        // 用户点了确定且保存成功：同步当前用户对象（与主窗口同一引用）并刷新显示
        if (dialog.isSaved()) {
            if (editName) {
                currentUser.setRealName(dialog.getNewValue());
            } else {
                currentUser.setPhone(dialog.getNewValue());
            }
            // 刷新本页只读区并同步顶栏姓名
            fillInfo();
            mainFrame.refreshUserBar();
        }
    }

    // 打开修改密码弹窗：复用现有 PasswordDialog，模态弹出，修改的是密码不影响本页信息
    private void openPasswordDialog() {
        PasswordDialog dialog = new PasswordDialog(currentUser, SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }
}
