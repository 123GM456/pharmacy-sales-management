package com.GM.medicine.ui;

// 导入 BorderLayout：主窗口按 顶栏（北）/ 左导航（西）/ 内容区（中）三段组织
import java.awt.BorderLayout;
// 导入 CardLayout：右侧内容区的页面切换器，按注册名切换显示不同面板
import java.awt.CardLayout;
// 导入 Cursor：菜单按钮上显示手型光标，暗示可以点击
import java.awt.Cursor;
// 导入 Component：给导航按钮、菜单小标题设置水平居中对齐
import java.awt.Component;
// 导入 Dimension：定义导航栏宽度、菜单按钮大小、窗口最小尺寸
import java.awt.Dimension;
// 导入 GridBagLayout：占位页面用它把提示文字水平垂直居中
import java.awt.GridBagLayout;
// 导入 ActionEvent：菜单按钮、退出按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为导航按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 Box：创建菜单按钮之间的固定间距（Strut）和把"退出登录"推到底部的弹性空隙（Glue）
import javax.swing.Box;
// 导入 BoxLayout：左侧导航菜单沿垂直方向排列
import javax.swing.BoxLayout;
// 导入 BorderFactory：创建顶栏底部分割线、导航栏右侧分割线、面板留白、导航按钮状态边框
import javax.swing.BorderFactory;
// 导入 Border：声明导航按钮的普通 / 选中两种状态边框类型
import javax.swing.border.Border;
// 导入 JButton：左侧导航菜单按钮
import javax.swing.JButton;
// 导入 JFrame：主窗口基类，程序运行期间一直存在的顶层窗口
import javax.swing.JFrame;
// 导入 JLabel：顶栏系统标题、用户信息，占位页提示文字
import javax.swing.JLabel;
// 导入 JPanel：承载顶栏、导航栏、内容区的容器
import javax.swing.JPanel;
// 导入 SysUser：构造方法接收当前登录用户，顶栏展示与后续页面身份传递都以它为准
import com.GM.medicine.pojo.entity.SysUser;
// 导入 MedicinePanel：药品管理模块面板（现有面板，直接复用）
import com.GM.medicine.ui.MedicinePanel;
// 导入 SalePanel：销售管理模块面板（现有面板，直接复用）
import com.GM.medicine.ui.SalePanel;
// 导入 PasswordDialog：修改密码对话框（现有对话框，点击菜单时弹出复用）
import com.GM.medicine.ui.PasswordDialog;

/**
 * - 主窗口
 * - 登录成功后由 LoginFrame 创建，负责顶部用户展示、左侧功能导航、右侧页面切换
 * - 通过构造方法接收当前登录用户对象，不重新查询数据库，也不构造虚假用户
 * - 只承担窗口、导航与页面切换：不写 SQL、不做业务计算，各页面业务由对应面板通过 Service 完成
 * - 界面的颜色、字体统一在 UiTheme 常量中修改；本类中的尺寸、间距、组件大小都在"可修改参数"注释处调整
 */
public class MainFrame extends JFrame {

    // 当前登录用户对象：顶栏展示与后续页面的身份来源，由 LoginFrame 登录成功后传入
    private SysUser currentUser;

    // 药品管理面板：现有面板，直接复用，业务后续在面板内部实现
    private MedicinePanel medicinePanel = new MedicinePanel();

    // 销售管理面板：现有面板，直接复用，业务后续在面板内部实现
    private SalePanel salePanel = new SalePanel();

    // 内容区页面切换器：按注册名切换右侧显示的面板
    private CardLayout cardLayout = new CardLayout();

    // 内容区容器：所有业务页面都注册在这一个面板上
    private JPanel contentPanel = new JPanel(cardLayout);

    // 当前选中的导航按钮：切换菜单时用它把上一个按钮的边框恢复为普通样式
    private JButton selectedNav;

    // 导航按钮普通态边框：1px 浅灰圆角细线（外围补 1px 空白），未选中时一直显示，复用 UiTheme 绘制与输入框统一
    private Border navNormalBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(1, 1, 1, 1),
            UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));

    // 导航按钮选中边框：2px 加粗浅灰圆角线，选中后一直保持（颜色可修改参数：UiTheme.TEXT_GRAY）
    private Border navSelectedBorder = UiTheme.roundBorder(UiTheme.TEXT_DARK, 1);

    /**
     * 构造主窗口：保存登录用户 + 初始化窗口属性 + 组装 UI 组件
     *
     * @param currentUser 当前登录用户，由 LoginFrame 登录成功后传入
     */
    public MainFrame(SysUser currentUser) {
        // 把登录窗传过来的用户对象保存到字段上，供顶栏展示与后续页面使用
        this.currentUser = currentUser;
        // 先配窗口自身属性（标题、尺寸、关闭行为、居中）
        initFrame();
        // 再组装三块 UI 区域（顶栏 / 左导航 / 右内容区）
        initComponents();
    }

    // 设置窗口标题、尺寸、关闭行为，并让窗口显示在屏幕中央
    private void initFrame() {
        // 窗口标题栏文字（和 LoginFrame 的"- 登录"后缀区分开）
        setTitle("医药销售管理系统");
        // 点击 X 直接结束 JVM，不做其他清理（程序退出即释放所有资源）
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640); // 可修改参数：主窗口初始大小（宽 960 / 高 640）
        setMinimumSize(new Dimension(820, 560)); // 可修改参数：主窗口最小尺寸（宽 820 / 高 560），防止布局被挤坏
        // null 表示以屏幕中心为锚位，让主窗在显示器上居中
        setLocationRelativeTo(null);
        // 内容面板设为浅灰底色：和白色顶栏、导航栏底色形成层次对比（颜色可修改参数：UiTheme.BG）
        getContentPane().setBackground(UiTheme.BG);
    }

    // 组装顶部标题栏、左侧导航栏、右侧内容区三块区域
    private void initComponents() {
        // 整体用 BorderLayout：NORTH 顶栏、WEST 导航栏、CENTER 内容区
        setLayout(new BorderLayout());
        // 三块子面板按方位加到内容面板上
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createNavPanel(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    // 创建顶部标题栏：左侧系统标题，右侧当前登录用户信息，底部 1px 分割线与内容区分开
    private JPanel createHeaderPanel() {
        // 标题栏容器：BorderLayout 左右分区，标题在左、用户信息在右
        JPanel panel = new JPanel(new BorderLayout());
        // 标题栏背景纯白，和浅灰内容区形成主次对比（颜色可修改参数：UiTheme.WHITE）
        panel.setBackground(UiTheme.WHITE);
        // 外层 1px 底部分割线 + 内层四周留白：分割线把顶栏和内容区分开
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 16, 10, 16))); // 可修改参数：顶栏四周留白（上 10 / 左右 16 / 下 10）

        // 创建纯文字系统标题标签：左对齐
        JLabel titleLabel = new JLabel("医药销售管理系统", JLabel.LEFT);
        // 标题字体用主题标题字体（字体可修改参数：UiTheme.FONT_TITLE，微软雅黑 粗体 20 号）
        titleLabel.setFont(UiTheme.FONT_TITLE);
        // 标题文字深灰色（颜色可修改参数：UiTheme.TEXT_DARK）
        titleLabel.setForeground(UiTheme.TEXT_DARK);
        // 标题放在顶栏左侧
        panel.add(titleLabel, BorderLayout.WEST);

        // 右侧用户信息：真实姓名 + 用户名 + 角色（不显示密码等敏感信息），次要信息用浅灰
        JLabel userLabel = UiTheme.createLabel(
                "当前用户：" + currentUser.getRealName()
                        + "（" + currentUser.getUserName() + "）    角色：" + roleText(),
                UiTheme.TEXT_GRAY); // 颜色可修改参数：UiTheme.TEXT_GRAY
        // 用户信息放在顶栏右侧
        panel.add(userLabel, BorderLayout.EAST);

        return panel;
    }

    // 创建左侧导航栏："系统菜单"小标题 + 四个页面切换按钮 + 修改密码 + 底部退出登录
    private JPanel createNavPanel() {
        // 导航栏容器：BoxLayout 沿垂直方向从上往下排列
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // 导航栏底色浅灰，和内容区一致；选中菜单用白色卡片区分（颜色可修改参数：UiTheme.BG）
        panel.setBackground(UiTheme.BG);
        // 外层右侧 1px 分割线（把导航区和内容区分开）+ 内层四周留白
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 1, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 10, 16, 10))); // 可修改参数：导航栏内边距（上 16 / 左右 10 / 下 16）
        // 导航栏固定宽度：只限制横向，纵向随窗口变化
        panel.setPreferredSize(new Dimension(120, 0)); // 可修改参数：导航栏宽度（160）

        // "系统菜单"分组小标题：次要文字浅灰、水平居中
        JLabel menuTitle = UiTheme.createLabel("系统菜单", UiTheme.TEXT_GRAY);
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(menuTitle);
        // 小标题和第一个按钮之间的间距
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：小标题与菜单按钮的间距（8）

        // ===== 四个页面切换按钮：点击后切换右侧内容区并更新选中态 =====
        JButton medicineButton = createNavButton("药品管理");
        medicineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 注册名 "medicine" 必须与 createContentPanel 中 add 时的名字一致
                cardLayout.show(contentPanel, "medicine");
                selectNav(medicineButton);
            }
        });
        panel.add(medicineButton);
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：菜单按钮之间的间距（8）

        JButton saleButton = createNavButton("销售管理");
        saleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 注册名 "sale" 必须与 createContentPanel 中 add 时的名字一致
                cardLayout.show(contentPanel, "sale");
                selectNav(saleButton);
            }
        });
        panel.add(saleButton);
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：菜单按钮之间的间距（8）

        JButton infoButton = createNavButton("个人信息");
        infoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 个人信息页面暂无对应面板类，先切换到占位页预留位置
                cardLayout.show(contentPanel, "info");
                selectNav(infoButton);
            }
        });
        panel.add(infoButton);
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：菜单按钮之间的间距（8）

        // ===== 修改密码：弹出现有对话框，不占内容区 =====
        JButton passwordButton = createNavButton("修改密码");
        passwordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 复用现有的 PasswordDialog（表单后续在对话框内部实现）
                PasswordDialog dialog = new PasswordDialog();
                dialog.setTitle("修改密码");
                dialog.setSize(400, 260); // 可修改参数：修改密码对话框初始大小（宽 400 / 高 260）
                // 相对主窗口居中弹出
                dialog.setLocationRelativeTo(MainFrame.this);
                dialog.setVisible(true);
            }
        });
        panel.add(passwordButton);

        // 弹性空隙：把退出登录按钮推到导航栏最底部
        panel.add(Box.createVerticalGlue());

        // ===== 退出登录：放导航栏底部，文字用浅灰表示次要操作 =====
        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(UiTheme.FONT_NORMAL);
        logoutButton.setForeground(UiTheme.TEXT_DARK); // 颜色可修改参数：UiTheme.TEXT_GRAY
        logoutButton.setBackground(UiTheme.BG);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setOpaque(true);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.setPreferredSize(new Dimension(100, 40)); // 可修改参数：退出登录按钮大小（宽 140 / 高 40）
        logoutButton.setMaximumSize(new Dimension(100, 40)); // 与首选大小保持一致，防止被 BoxLayout 拉伸
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        panel.add(logoutButton);

        // 默认选中"药品管理"，和内容区默认显示的页面（见 createContentPanel）保持一致
        selectNav(medicineButton);
        return panel;
    }

    // 创建导航菜单按钮：统一样式与尺寸，底色固定白色，选中与否只切换边框粗细，由 selectNav 统一管理
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        // 按钮文字字体（字体可修改参数：UiTheme.FONT_NORMAL，微软雅黑 常规 13 号）
        button.setFont(UiTheme.FONT_NORMAL);
        // 按钮文字深灰色（颜色可修改参数：UiTheme.TEXT_DARK）
        button.setForeground(UiTheme.TEXT_DARK);
        // 底色固定白色不再变化，选中与否只切换边框粗细（颜色可修改参数：UiTheme.WHITE）
        button.setBackground(UiTheme.WHITE);
        // 默认 1px 浅灰细边框：未选中的常态外观，与选中加粗边框占位一致，切换时按钮内容不跳动
        button.setBorder(navNormalBorder);
        // 关闭默认焦点虚线框，保持扁平风格
        button.setFocusPainted(false);
        // 自定义绘制背景：配合 setOpaque(true) 让 setBackground 生效
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        // 手型光标暗示可点击
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 统一菜单按钮大小
        button.setPreferredSize(new Dimension(140, 40)); // 可修改参数：菜单按钮大小（宽 140 / 高 38）
        // BoxLayout 默认会把按钮拉伸到面板宽度，用 MaximumSize 限制住保持设置宽度
        button.setMaximumSize(new Dimension(140, 40)); // 可修改参数：与上面的菜单按钮大小保持一致
        // 垂直排列时按钮相对面板水平居中
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // 切换导航选中态：底色都保持白色，上一个按钮恢复细边框，新按钮换成加粗灰色边框一直保持
    private void selectNav(JButton button) {
        // 先把上一个选中按钮恢复为细边框
        if (selectedNav != null) {
            selectedNav.setBorder(navNormalBorder);
        }
        // 记录新选中的按钮并保持加粗灰色边框
        selectedNav = button;
        selectedNav.setBorder(navSelectedBorder);
    }

    // 创建右侧内容区：CardLayout 按注册名切换四个页面
    private JPanel createContentPanel() {
        // 空壳面板铺浅灰底色：让空区域可见，后续实现业务时再改（颜色可修改参数：UiTheme.BG）
        medicinePanel.setBackground(UiTheme.BG);
        salePanel.setBackground(UiTheme.BG);

        // 按注册名依次注册页面：导航按钮切换时用的名字必须与此处一致
        contentPanel.add("medicine", medicinePanel);
        contentPanel.add("sale", salePanel);
        // 销售记录、个人信息暂无对应面板类，先放占位页预留位置，后续实现后替换
        contentPanel.add("record", createPlaceholderPage("销售记录页面待实现"));
        contentPanel.add("info", createPlaceholderPage("个人信息页面待实现"));

        // 默认显示药品管理页，与导航栏默认选中项一致
        cardLayout.show(contentPanel, "medicine");
        return contentPanel;
    }

    // 创建占位页面：浅灰底 + 居中提示文字，用于预留未实现模块的位置
    private JPanel createPlaceholderPage(String text) {
        // GridBagLayout 不加约束时组件会自动水平垂直居中
        JPanel page = new JPanel(new GridBagLayout());
        // 占位页底色与内容区一致（颜色可修改参数：UiTheme.BG）
        page.setBackground(UiTheme.BG);
        // 居中提示文字：次要信息浅灰（颜色可修改参数：UiTheme.TEXT_GRAY）
        page.add(UiTheme.createLabel(text, UiTheme.TEXT_GRAY));
        return page;
    }

    // 把 SysUser 实体里的 role 数值（1 / 0）转换成界面上的中文名称
    // SysUser.ROLE_ADMIN = 1（管理员）、SysUser.ROLE_STAFF = 0（普通用户）
    private String roleText() {
        // 先判空再比较：避免 currentUser.getRole() 返回 null 时空指针异常
        if (currentUser.getRole() != null && currentUser.getRole() == SysUser.ROLE_ADMIN) {
            return "管理员";
        }
        // 其他所有情况都归为"普通用户"（包括 role 为 0 或 null）
        return "普通用户";
    }

    // 退出登录：弹确认框，确认后关闭主窗并回到登录窗（不直接退出 JVM）
    private void logout() {
        // 弹扁平风格"确定 / 取消"确认框：选"取消"或点标题栏 X 关闭时都视为取消
        boolean confirmed = UiTheme.showConfirmDialog(this, "确定要退出登录吗？");
        // 非"确定"都不执行退出
        if (!confirmed) {
            return;
        }
        // dispose() 只关闭主窗口，不结束 JVM；随后重新打开登录窗
        dispose();
        new LoginFrame().setVisible(true);
    }
}
