package com.GM.medicine.ui;

// 导入 BorderLayout：登录窗口按上中下三段组织内容
import java.awt.BorderLayout;
// 导入 Dimension：设置按钮的固定宽高，使两个按钮大小一致
import java.awt.Dimension;
// 导入 GridBagConstraints：配合 GridBagLayout 控制表单项的位置与间距
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：让“标签 + 输入框”两列对齐，避免使用绝对坐标
import java.awt.GridBagLayout;
// 导入 Insets：设置表单项四周的留白
import java.awt.Insets;
// 导入 FlowLayout：底部按钮横向居中排列
import java.awt.FlowLayout;
// 导入 ActionEvent：按钮点击与回车事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为登录、退出按钮及密码框注册的监听接口
import java.awt.event.ActionListener;
// 导入 EmptyBorder：为标题区域添加留白
import javax.swing.BorderFactory;
// 导入 JButton：登录与退出按钮
import javax.swing.JButton;
// 导入 JFrame：登录窗口的基类，是程序启动后显示的第一个窗口
import javax.swing.JFrame;
// 导入 JLabel：展示系统标题与表单字段名
import javax.swing.JLabel;
// 导入 JOptionPane：登录失败或未填写内容时弹出提示框
import javax.swing.JOptionPane;
// 导入 JPanel：承载标题、表单与按钮的容器
import javax.swing.JPanel;
// 导入 JPasswordField：密码输入框，输入内容以圆点显示
import javax.swing.JPasswordField;
// 导入 JTextField：用户名输入框
import javax.swing.JTextField;
// 导入 SysUser：登录成功后得到的当前登录用户对象
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserService：登录校验统一由它完成，本类不直接访问数据库
import com.GM.medicine.service.SysUserService;

/**
 * - 登录窗口
 * - 负责收集用户名与密码并交给 SysUserService 校验
 * - 登录成功时把当前登录用户传递给 MainFrame 并关闭本窗口，失败时留在本窗口提示原因
 * - 界面层不编写 JDBC 与 SQL，所有登录规则均由 Service 决定
 */
public class LoginFrame extends JFrame {

    // 登录业务对象，界面只通过它完成登录校验
    private SysUserService sysUserService = new SysUserService();

    // 用户名输入框
    private JTextField userNameField = new JTextField(16);

    // 密码输入框
    private JPasswordField passwordField = new JPasswordField(16);

    // 登录按钮，主题色实心扁平样式
    private JButton loginButton = UiTheme.createFlatButton("登录", null, UiTheme.PRIMARY, UiTheme.WHITE);

    // 退出按钮，浅灰幽灵扁平样式，带电源图标
    private JButton exitButton = UiTheme.createFlatButton("退出", Icons.power(14), UiTheme.BG, UiTheme.TEXT_DARK);

    /**
     * 构造登录窗口，完成窗口属性与界面组件的初始化
     */
    public LoginFrame() {
        initFrame();
        initComponents();
    }

    // 设置窗口标题、大小、关闭行为，并让窗口显示在屏幕中央
    private void initFrame() {
        setTitle("医药销售管理系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(380, 300);
        // 窗口居中显示
        setLocationRelativeTo(null);
        // 窗口使用纯白底色
        getContentPane().setBackground(UiTheme.WHITE);
    }

    // 组装登录界面的标题区、表单区与按钮区，并为按钮绑定事件
    private void initComponents() {
        setLayout(new BorderLayout());
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        // 密码框按回车等同于点击登录按钮，符合用户操作习惯
        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    // 创建标题区，展示扁平化系统徽标与系统名称
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.WHITE);
        JLabel titleLabel = new JLabel("医药销售管理系统", Icons.logo(40), JLabel.CENTER);
        titleLabel.setFont(UiTheme.FONT_TITLE);
        titleLabel.setForeground(UiTheme.TEXT_DARK);
        titleLabel.setIconTextGap(12);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 10, 16, 10));
        return panel;
    }

    // 创建表单区，用户名与密码按两列布局对齐，标签带扁平小图标
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiTheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 第一行：用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(UiTheme.createLabel("用户名：", Icons.user(15), UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        panel.add(userNameField, gbc);

        // 第二行：密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(UiTheme.createLabel("密　码：", Icons.lock(15), UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        return panel;
    }

    // 创建按钮区，横向居中放置登录与退出按钮
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        panel.setBackground(UiTheme.WHITE);
        loginButton.setPreferredSize(new Dimension(100, 36));
        exitButton.setPreferredSize(new Dimension(100, 36));
        panel.add(loginButton);
        panel.add(exitButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 取消登录直接结束程序
                System.exit(0);
            }
        });
        return panel;
    }

    /**
     * 执行登录：校验输入完整性后调用 SysUserService 完成身份验证
     * - 校验失败或登录失败时保留登录窗口并提示原因
     * - 登录成功时关闭登录窗口并将当前用户交给主窗口
     */
    private void doLogin() {
        // 去掉用户名首尾空白，避免因误输空格导致查询不到用户
        String userName = userNameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (userName.isEmpty()) {
            showMessage("请输入用户名");
            userNameField.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showMessage("请输入密码");
            passwordField.requestFocusInWindow();
            return;
        }

        SysUser currentUser = sysUserService.login(userName, password);
        if (currentUser == null) {
            // Service 对所有失败原因统一返回 null，界面按最可能的原因给出提示
            showMessage("登录失败：用户名或密码错误，或账号已被禁用");
            // 清空密码框并让其获得焦点，方便重新输入；用户名保留避免重复输入
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            return;
        }

        // 登录成功：先关闭登录窗口，再把当前登录用户交给主窗口
        dispose();
        new MainFrame(currentUser).setVisible(true);
    }

    // 统一使用 JOptionPane 弹出提示，避免界面中出现控制台输出
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
    }
}