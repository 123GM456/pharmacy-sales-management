package com.GM.medicine.ui;

// 导入 BorderLayout：主窗口按顶部信息、中间内容、底部状态栏三段组织
import java.awt.BorderLayout;
// 导入 FlowLayout：状态栏内容自左向右排列
import java.awt.FlowLayout;
// 导入 Font：设置系统标题与状态栏文字的字号
import java.awt.Font;
// 导入 EmptyBorder：为顶部信息区添加留白
import javax.swing.BorderFactory;
// 导入 JFrame：主窗口的基类
import javax.swing.JFrame;
// 导入 JLabel：展示系统标题与当前登录用户信息
import javax.swing.JLabel;
// 导入 JPanel：承载顶部信息与底部状态栏的容器
import javax.swing.JPanel;
// 导入 JTabbedPane：作为后续各业务模块的基础区域，模块面板统一挂在这里
import javax.swing.JTabbedPane;
// 导入 SysUser：主窗口需要保存当前登录用户，后续操作都以该用户为操作人
import com.GM.medicine.pojo.entity.SysUser;
// 导入 MedicinePanel：药品管理模块面板，本次仅空框架
import com.GM.medicine.ui.medicine.MedicinePanel;
// 导入 SalePanel：销售管理模块面板，本次仅空框架
import com.GM.medicine.ui.sale.SalePanel;

/**
 * - 主窗口
 * - 登录成功后由 LoginFrame 创建，负责展示系统标题、当前登录用户与各业务模块的基础区域
 * - 通过构造方法接收当前登录用户，不引入额外的用户上下文容器
 * - 本次只搭建窗口框架，药品管理、销售管理等具体业务功能后续在对应面板中实现
 */
public class MainFrame extends JFrame {

    // 当前登录用户，主窗口内所有后续业务操作都以该用户为操作人
    private SysUser currentUser;

    // 药品管理面板，主窗口“药品管理”选项卡的内容区
    private MedicinePanel medicinePanel = new MedicinePanel();

    // 销售管理面板，主窗口“销售管理”选项卡的内容区
    private SalePanel salePanel = new SalePanel();

    // 内容选项卡，后续新增的业务模块面板都挂在这里
    private JTabbedPane tabbedPane = new JTabbedPane();

    /**
     * 构造主窗口
     *
     * @param currentUser 当前登录用户，由 LoginFrame 登录成功后传入
     */
    public MainFrame(SysUser currentUser) {
        this.currentUser = currentUser;
        initFrame();
        initComponents();
    }

    // 设置窗口标题、大小、关闭行为，并让窗口显示在屏幕中央
    private void initFrame() {
        setTitle("医药销售管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    // 组装顶部信息区、中间内容区与底部状态栏
    private void initComponents() {
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    // 创建顶部信息区，展示系统标题
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("医药销售管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        return panel;
    }

    // 创建中间内容区，各业务模块面板以选项卡形式承载
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        tabbedPane.addTab("药品管理", medicinePanel);
        tabbedPane.addTab("销售管理", salePanel);
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // 创建底部状态栏，显示当前登录用户及其角色
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.add(new JLabel("当前登录用户：" + currentUser.getRealName()
                + "（" + currentUser.getUserName() + "）    角色：" + roleText()));
        return panel;
    }

    // 把数据库中的角色值转换为界面上的中文名称
    private String roleText() {
        if (currentUser.getRole() != null && currentUser.getRole() == SysUser.ROLE_ADMIN) {
            return "管理员";
        }
        return "普通用户";
    }
}