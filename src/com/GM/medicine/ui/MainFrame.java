package com.GM.medicine.ui;

// 导入 BorderLayout：主窗口按顶部标题、中间内容、底部状态栏三段组织
import java.awt.BorderLayout;
// 导入 FlowLayout：顶部标题栏内容左对齐、状态栏内容左对齐
import java.awt.FlowLayout;
// 导入 BorderFactory：创建底部 / 顶部 1px 细分割线，把三段区域视觉上分开
import javax.swing.BorderFactory;
// 导入 JFrame：主窗口的基类，程序运行期间一直存在的顶层窗口
import javax.swing.JFrame;
// 导入 JLabel：展示纯文字系统标题与底部当前登录用户信息
import javax.swing.JLabel;
// 导入 JPanel：承载顶部标题栏与底部状态栏的容器
import javax.swing.JPanel;
// 导入 JTabbedPane：中间内容区，后续各业务模块以"选项卡"形式挂进来
import javax.swing.JTabbedPane;
// 导入 SysUser：主窗口构造方法接收当前登录用户，所有后续业务以此为操作人
import com.GM.medicine.pojo.entity.SysUser;
// 导入 MedicinePanel：药品管理模块面板，当前仅空壳占位，后续实现药品增删改查
import com.GM.medicine.ui.medicine.MedicinePanel;
// 导入 SalePanel：销售管理模块面板，当前仅空壳占位，后续实现销售录入与记录查询
import com.GM.medicine.ui.sale.SalePanel;

/**
 * - 主窗口
 * - 登录成功后由 LoginFrame 创建，负责展示系统标题、当前登录用户、各业务模块选项卡
 * - 通过构造方法接收当前登录用户对象，不引入额外的"用户上下文容器"
 * - 当前只搭窗口骨架，药品管理、销售管理的具体业务功能后续在对应面板中实现
 */
public class MainFrame extends JFrame {

    // 当前登录用户对象：主窗口内所有业务操作（例如销售记录标记谁操作的）都以它为操作人
    private SysUser currentUser;

    // 药品管理面板：后续实现药品增删改查、库存预警、过期提醒等业务
    private MedicinePanel medicinePanel = new MedicinePanel();

    // 销售管理面板：后续实现销售录入、销售记录查询、销售统计等业务
    private SalePanel salePanel = new SalePanel();

    // 内容选项卡：后续新增的业务模块都可以 addTab 进来挂在这一个 TabbedPane 上
    private JTabbedPane tabbedPane = new JTabbedPane();

    /**
     * 构造主窗口：保存登录用户 + 初始化窗口属性 + 组装 UI 组件
     *
     * @param currentUser 当前登录用户，由 LoginFrame 登录成功后传入
     */
    public MainFrame(SysUser currentUser) {
        // 把登录窗传过来的用户对象保存到字段上，供后续 getRole() / getUserName() 等使用
        this.currentUser = currentUser;
        // 先配窗口自身属性（标题、尺寸、关闭行为、居中）
        initFrame();
        // 再组装三块 UI 区域（标题栏 / 选项卡 / 状态栏）
        initComponents();
    }

    // 设置窗口标题、尺寸、关闭行为，并让窗口显示在屏幕中央
    private void initFrame() {
        // 窗口标题栏文字（和 LoginFrame 的"- 登录"后缀区分开）
        setTitle("医药销售管理系统");
        // 点击 X 直接结束 JVM，不做其他清理（程序退出即释放所有资源）
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 窗口尺寸：宽 900、高 600 像素，足够容纳 2 列布局 + 底部状态栏
        setSize(900, 600);
        // null 表示以屏幕中心为锚位，让主窗在显示器上居中
        setLocationRelativeTo(null);
        // 内容面板设为浅灰底色：和白色标题栏 / 白色状态栏形成层次对比
        getContentPane().setBackground(UiTheme.BG);
    }

    // 组装顶部标题栏、中间选项卡区、底部状态栏三块区域
    private void initComponents() {
        // 整体用 BorderLayout：NORTH 标题栏、CENTER 选项卡、SOUTH 状态栏
        setLayout(new BorderLayout());
        // 三块子面板按方位加到内容面板上
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    // 创建顶部标题栏：左侧是系统标题粗体字，底部 1px 分割线把标题栏和内容区分开
    private JPanel createHeaderPanel() {
        // 标题栏容器：FlowLayout 左对齐，组件水平间距 12、上下间距 12
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        // 标题栏背景纯白，和下方浅灰内容区形成主次对比
        panel.setBackground(UiTheme.WHITE);
        // 在面板底部画一条 1px 浅灰细分割线（上 0 / 右 0 / 下 1 / 左 0）
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER));

        // 创建纯文字系统标题标签：左对齐
        JLabel titleLabel = new JLabel("医药销售管理系统", JLabel.LEFT);
        // 使用主题标题字体：微软雅黑、粗体、20 号
        titleLabel.setFont(UiTheme.FONT_TITLE);
        // 标题文字深灰色
        titleLabel.setForeground(UiTheme.TEXT_DARK);

        // FlowLayout 左对齐：把标题标签加到面板最左侧
        panel.add(titleLabel);
        return panel;
    }

    // 创建中间内容区：JTabbedPane 承载两个业务模块
    private JPanel createContentPanel() {
        // 外层面板用 BorderLayout，让 JTabbedPane 撑满整个中间区域
        JPanel panel = new JPanel(new BorderLayout());
        // 外层面板背景浅灰，和内容区整体底色一致
        panel.setBackground(UiTheme.BG);

        // 选项卡文字用主题普通字体：微软雅黑、常规、13 号
        tabbedPane.setFont(UiTheme.FONT_NORMAL);
        // 选项卡本体背景白色（和未选中卡片底色一致）
        tabbedPane.setBackground(UiTheme.WHITE);
        // 替换默认金属凸块外观为自绘 FlatTabbedPaneUI：圆角卡片 + 主题色下划线
        tabbedPane.setUI(new FlatTabbedPaneUI());

        // 依次添加两个选项卡：文字标题 + 具体内容面板
        tabbedPane.addTab("药品管理", medicinePanel);
        tabbedPane.addTab("销售管理", salePanel);

        // 两个空面板先铺浅灰底色：让空区域可见，后续实现业务时再改
        medicinePanel.setBackground(UiTheme.BG);
        salePanel.setBackground(UiTheme.BG);

        // 把 tabbedPane 放到外层面板中央撑满
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // 创建底部状态栏：左侧展示"当前登录用户：真名（用户名）  角色：X"，顶部 1px 分割线把状态栏和内容区分开
    private JPanel createStatusPanel() {
        // 状态栏容器：FlowLayout 左对齐，组件间距 12、上下间距 8
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        // 状态栏背景纯白，和顶部标题栏同色，上下视觉对称
        panel.setBackground(UiTheme.WHITE);
        // 在面板顶部画一条 1px 浅灰细分割线（上 1 / 右 0 / 下 0 / 左 0）
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER));

        // 用 UiTheme.createLabel 创建统一样式标签：拼好用户信息 + 角色，文字用 TEXT_GRAY 表示次要信息
        panel.add(UiTheme.createLabel(
                // 第一段："当前登录用户：" + 真名 + "（" + 用户名 + "）"
                "当前登录用户：" + currentUser.getRealName()
                        // 第二段："    角色：" + 角色中文名（由 roleText() 把数字转成"管理员"或"普通用户"）
                        + "（" + currentUser.getUserName() + "）    角色：" + roleText(),
                UiTheme.TEXT_GRAY));  // 次要信息用浅灰色

        return panel;
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
}