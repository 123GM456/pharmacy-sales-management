package com.GM.medicine.ui;

// 导入 BorderLayout：登录窗口按上中下三段（标题 / 表单 / 按钮）组织内容
import java.awt.BorderLayout;
// 导入 Dimension：给按钮设置固定宽高，使登录、退出两个按钮大小保持一致
import java.awt.Dimension;
// 导入 GridBagConstraints：配合 GridBagLayout 控制表单项的位置、间距、对齐方式
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：让左侧标签和右侧输入框两列对齐，避免绝对坐标难以维护
import java.awt.GridBagLayout;
// 导入 Insets：设置表单项四周的留白（上 / 左 / 下 / 右）
import java.awt.Insets;
// 导入 FlowLayout：底部按钮区域按水平方向居中排列
import java.awt.FlowLayout;
// 导入 ActionEvent：按钮点击事件与密码框回车事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为登录、退出按钮及密码框注册点击 / 回车监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：创建 EmptyBorder 给标题区域添加留白
import javax.swing.BorderFactory;
// 导入 JButton：登录与退出按钮的组件类型
import javax.swing.JButton;
// 导入 JFrame：登录窗口的基类，是程序启动后显示的第一个顶层窗口
import javax.swing.JFrame;
// 导入 JLabel：展示纯文字系统标题与表单左侧字段标签
import javax.swing.JLabel;
// 导入 JOptionPane：登录失败或未填写内容时弹出带警告图标的提示框
import javax.swing.JOptionPane;
// 导入 JPanel：承载标题、表单、按钮的容器组件
import javax.swing.JPanel;
// 导入 JPasswordField：密码输入框，用户输入内容以圆点显示，不暴露明文
import javax.swing.JPasswordField;
// 导入 JTextField：用户名输入框，用户输入内容以明文显示
import javax.swing.JTextField;
// 导入 SysUser：登录成功后 Service 返回的当前登录用户对象类型
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserService：登录校验统一由它完成，本界面类绝不直接访问 JDBC / SQL
import com.GM.medicine.service.SysUserService;

/**
 * - 登录窗口
 * - 负责收集用户名与密码并交给 SysUserService 校验
 * - 登录成功时关闭本窗口、把当前登录用户传给 MainFrame；失败时留在本窗口弹提示
 * - 界面层不编写 JDBC 与 SQL，所有登录规则（密码哈希校验、账号禁用判断等）均由 Service 决定
 */
public class LoginFrame extends JFrame {

    // 登录业务对象：界面只通过它做登录校验，一次登录一个实例足够
    private SysUserService sysUserService = new SysUserService();

    // 用户名输入框：参数 16 是推荐列数，实际宽度由 GridBagLayout 决定
    private JTextField userNameField = new JTextField(16);

    // 密码输入框：同上，内部自带密码遮罩（默认圆点）
    private JPasswordField passwordField = new JPasswordField(16);

    // 登录按钮：调用 UiTheme 工厂方法创建扁平样式，文字"登录"、主题绿实心背景、白色字
    private JButton loginButton = UiTheme.createFlatButton("登录", UiTheme.PRIMARY, UiTheme.WHITE);

    // 退出按钮：文字"退出"、浅灰幽灵按钮样式、深灰字
    private JButton exitButton = UiTheme.createFlatButton("退出", UiTheme.BG, UiTheme.TEXT_DARK);

    /**
     * 构造登录窗口：初始化窗口属性 + 组装界面组件
     * 两步拆开是为了让职责清晰——initFrame 管窗口本身，initComponents 管里面放什么
     */
    public LoginFrame() {
        // 先配置窗口自身：标题、尺寸、关闭行为、居中
        initFrame();
        // 再组装窗口内部：标题区、表单区、按钮区 + 事件监听
        initComponents();
    }

    // 设置窗口标题、大小、关闭行为、背景色，并让窗口显示在屏幕中央
    private void initFrame() {
        // 窗口标题栏文字；操作系统任务栏也会显示这个标题
        setTitle("医药销售管理系统 - 登录");
        // 点击窗口右上角 X 时执行 EXIT_ON_CLOSE：直接结束整个 JVM 进程（不是只关窗口）
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 禁止用户拖拽改变窗口大小，保证内部布局不会被挤压错位
        setResizable(false);
        // 窗口尺寸：宽 380、高 300 像素（高度足够容纳带图标徽标 + 表单 + 按钮）
        setSize(380, 300);
        // null 表示以屏幕中心点为锚位，让窗口在整个显示器上居中显示
        setLocationRelativeTo(null);
        // 整个内容面板背景设为纯白，配合扁平风格让窗口整体干净通透
        getContentPane().setBackground(UiTheme.WHITE);
    }

    // 组装登录界面的标题区、表单区、按钮区，并为按钮绑定事件
    private void initComponents() {
        // 整体采用 BorderLayout：上 NORTH 放标题、中 CENTER 放表单、下 SOUTH 放按钮
        setLayout(new BorderLayout());
        // 三块子面板按 BorderLayout 方位加到窗口内容面板上
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // 密码框注册回车监听器：用户输完密码直接按回车就能登录，和浏览器表单习惯一致
        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 回车等同点击登录按钮，直接调同一个方法避免复制粘贴逻辑
                doLogin();
            }
        });
    }

    // 创建标题区：顶部一行，水平居中显示"医药销售管理系统"粗体标题
    private JPanel createTitlePanel() {
        // 标题区用 BorderLayout，内部只有一个居中的标题标签
        JPanel panel = new JPanel(new BorderLayout());
        // 标题区背景纯白，和窗口整体背景一致
        panel.setBackground(UiTheme.WHITE);

        // 创建纯文字标题标签：水平居中显示
        JLabel titleLabel = new JLabel("医药销售管理系统", JLabel.CENTER);
        // 使用主题标题字体：微软雅黑、粗体、20 号
        titleLabel.setFont(UiTheme.FONT_TITLE);
        // 标题文字颜色设为深灰，比纯黑柔和不刺眼
        titleLabel.setForeground(UiTheme.TEXT_DARK);

        // 把标题标签放到面板中央区域
        panel.add(titleLabel, BorderLayout.CENTER);
        // 给标题区四周围空：上 28 / 左右 10 / 下 16 像素，让标题不贴紧窗口边缘
        panel.setBorder(BorderFactory.createEmptyBorder(24, 10, 16, 10));
        return panel;
    }

    // 创建表单区：左侧是字段标签，右侧是输入框；两行按 GridBagLayout 对齐
    private JPanel createFormPanel() {
        // 表单容器用 GridBagLayout：灵活的网格布局，让左右两列标签和输入框对齐
        JPanel panel = new JPanel(new GridBagLayout());
        // 表单区背景纯白
        panel.setBackground(UiTheme.WHITE);
        
        // 为两个输入框安装焦点边框效果：未选中浅灰 1px，选中黑色加粗 2px
        UiTheme.installFocusBorder(userNameField);
        UiTheme.installFocusBorder(passwordField);


        // GridBagConstraints 描述每个单元格的约束，设一次后两行可复用
        GridBagConstraints gbc = new GridBagConstraints();
        // 单元格四周留白：上 8 / 左 8 / 下 8 / 右 8 像素，让标签、输入框之间有间距
        gbc.insets = new Insets(18, 0, 0, 0);
        // 单元格内组件左对齐（WEST），避免文字居中显得松散
        gbc.anchor = GridBagConstraints.WEST;

        // ===== 第一行：用户名 =====
        // gridx=0 / gridy=0 表示放在第 0 列第 0 行
        gbc.gridx = 0;
        gbc.gridy = 0;
        // 左侧标签：文字"用户名："、深灰色
        panel.add(UiTheme.createLabel("用户名：", UiTheme.TEXT_DARK), gbc);
        // gridx=1 表示同一行的右侧第 1 列放输入框
        gbc.gridx = 1;
        panel.add(userNameField, gbc);

        // ===== 第二行：密码 =====
        // gridx 保持 1（右侧）、gridy 改为 1（下一行）
        gbc.gridx = 0;
        gbc.gridy = 1;
        // 左侧标签：文字"密　码："、深灰色
        panel.add(UiTheme.createLabel("密　码：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        return panel;
    }

    // 创建按钮区：底部一行，登录按钮居左、退出按钮居右，中间留间距
    private JPanel createButtonPanel() {
        // FlowLayout 居中对齐：组件间距 20 像素、行内上下 16 像素
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 40));
        // 按钮区背景纯白
        panel.setBackground(UiTheme.WHITE);

        // 统一按钮尺寸：宽 100、高 36 像素，两个按钮整齐对齐
        loginButton.setPreferredSize(new Dimension(100, 36));
        exitButton.setPreferredSize(new Dimension(100, 36));
        // 按 "登录 → 退出" 顺序依次加到面板里，FlowLayout 会自动居中排列
        panel.add(loginButton);
        panel.add(exitButton);

        // 登录按钮点击事件：调用 doLogin() 统一处理登录校验逻辑
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        // 退出按钮点击事件：直接调 System.exit(0) 结束 JVM，不做额外清理（当前就一个窗口）
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        return panel;
    }

    /**
     * 执行登录：本地非空校验 → 调 SysUserService 校验 → 成功进主窗 / 失败弹提示
     * - 校验失败或 Service 返回 null 时留在登录窗，弹提示并让用户继续改
     * - Service 返回非 null SysUser 时关闭登录窗、传用户对象给 MainFrame 构造主窗
     */
    private void doLogin() {
        // 取出用户名并 trim：去掉用户不小心输进去的首尾空格，避免因空格查不到用户
        String userName = userNameField.getText().trim();
        // getPassword() 返回 char[]（用完可被 GC，比 getText() 安全），再转成 String 传给 Service
        String password = new String(passwordField.getPassword());

        // 第一重校验：用户名为空则弹提示并把焦点还给用户名框，让用户继续输入
        if (userName.isEmpty()) {
            showMessage("请输入用户名");
            userNameField.requestFocusInWindow();
            return;
        }
        // 第二重校验：密码为空同样提示并聚焦密码框
        if (password.isEmpty()) {
            showMessage("请输入密码");
            passwordField.requestFocusInWindow();
            return;
        }

        // 真正调用 Service 登录校验：内部会做 BCrypt 密码哈希比对、账号禁用状态检查
        // 返回 null 表示登录失败；非 null SysUser 表示登录成功（内含 id / 角色 / 姓名等完整信息）
        SysUser currentUser = sysUserService.login(userName, password);

        if (currentUser == null) {
            // Service 对所有失败原因（用户名不存在 / 密码不对 / 账号禁用）统一返回 null
            // 界面按最常见的"用户名或密码错误"给出合并提示，避免泄露账号是否存在的信息
            showMessage("登录失败：用户名或密码错误，或账号已被禁用");
            // 清空密码框让用户重新输；用户名保留让用户确认是对的
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            return;
        }

        // 登录成功：dispose() 关闭登录窗（只关这个 JFrame，不结束 JVM）
        dispose();
        // 创建主窗：把当前登录用户对象传给 MainFrame 构造方法，主窗知道"是谁登录进来了"
        new MainFrame(currentUser).setVisible(true);
    }

    // 统一弹警告提示：标题"提示"、黄色感叹号图标，避免各处自己 new JOptionPane 样式不一致
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
    }
}