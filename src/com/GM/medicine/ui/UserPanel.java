package com.GM.medicine.ui;

// 导入 BorderLayout：面板按 顶部工具栏 / 下方列表区 组织
import java.awt.BorderLayout;
// 导入 Color：表格选中行的浅绿底色与操作按钮悬停底色
import java.awt.Color;
// 导入 Component：操作列渲染器与编辑器方法返回的组件类型
import java.awt.Component;
// 导入 FlowLayout：工具栏查询控件与操作列按钮面板的排列
import java.awt.FlowLayout;
// 导入 DateTimeFormatter：创建时间列格式化为 yyyy-MM-dd HH:mm 文本
import java.time.format.DateTimeFormatter;
// 导入 ActionEvent：下拉框、按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为下拉框与按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 MouseAdapter：一个类同时接收点击与移动事件，用于跟踪操作列的悬停行号
import java.awt.event.MouseAdapter;
// 导入 MouseEvent：表格悬停跟踪的鼠标事件参数类型
import java.awt.event.MouseEvent;
// 导入 BorderFactory：表格区留白、操作按钮内边距
import javax.swing.BorderFactory;
// 导入 AbstractCellEditor：操作列按钮编辑器的基类，提供编辑状态管理与 fireEditingStopped
import javax.swing.AbstractCellEditor;
// 导入 JButton：查询、新增用户按钮与操作列内联按钮
import javax.swing.JButton;
// 导入 JComboBox：分类下拉框、查询类型下拉框
import javax.swing.JComboBox;
// 导入 JPanel：承载工具栏、表格区、操作列按钮面板的容器
import javax.swing.JPanel;
// 导入 JScrollPane：表格滚动容器，数据多时可上下滚动
import javax.swing.JScrollPane;
// 导入 JTable：用户列表表格
import javax.swing.JTable;
// 导入 JTextField：查询关键词输入框
import javax.swing.JTextField;
// 导入 SwingUtilities：编辑器点击后延迟执行操作；getWindowAncestor 找对话框父窗口
import javax.swing.SwingUtilities;
// 导入 TableCellEditor：操作列编辑器接口
import javax.swing.table.TableCellEditor;
// 导入 TableCellRenderer：把操作列渲染成两个按钮外观
import javax.swing.table.TableCellRenderer;
// 导入 DefaultTableModel：表格数据模型，控制只有操作列可编辑
import javax.swing.table.DefaultTableModel;
// 导入 List：当前显示的用户列表类型
import java.util.List;
// 导入 SysUser：表格行对应的用户实体类型（含角色 / 状态常量）
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SysUserService：分类查询、模糊查询、启用禁用、重置密码统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SysUserService;

/**
 * - 用户管理面板（仅管理员使用）
 * - 顶部工具栏用"用户分类"下拉框切换数据视角（全部 / 管理员 / 员工 / 禁用），选择后立即刷新表格
 * - 工具栏提供用户名 / 姓名 / 手机号三种模糊查询与新增用户入口
 * - 表格行尾"启用/禁用"与"重置密码"两列操作按钮：分别切换该行用户状态、确认后重置为默认密码
 * - 数据获取全部通过 SysUserService 完成，本类不写 SQL；不提供删除、修改功能
 * - 底部分页栏每页 50 条：滚轮翻页保留，另加上一页/下一页与页码跳转；切换分类或搜索回到第 1 页，行内操作保持当前页
 */
public class UserPanel extends JPanel {

    // 用户业务对象：查询、启用禁用、重置密码统一通过它调用
    private SysUserService userService = new SysUserService();

    // 当前登录用户：用于"管理员不能禁用自己"校验（Service 同样兜底校验）
    private SysUser currentUser;

    // 分类下拉框的四个固定选项：与左侧下拉框选项一一对应
    private final String[] categoryItems = {"全部用户", "管理员", "员工", "禁用用户"};

    // 表格列名：数据的各个字段 + 行尾两列操作列，字段列均只读展示
    private final String[] columnNames = {"用户名", "姓名", "手机号", "角色", "状态", "创建时间", "修改时间", "状态", "重置密码"};

    // 表格数据模型：只有行尾两列操作列可编辑（用于触发行内按钮）
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        public boolean isCellEditable(int row, int column) {
            // 仅行尾两列操作列可编辑（用于触发行内按钮）；其他列点击不进入编辑状态
            return column >= columnNames.length - 2;
        }
    };

    // 用户列表表格：展示当前分类或查询结果的用户
    private JTable table = new JTable(tableModel);

    // 分类下拉框：选择后立即按对应 Service 方法刷新表格
    private JComboBox<String> categoryBox = UiTheme.createComboBox(categoryItems);

    // 查询类型下拉框：选用户名 / 姓名 / 手机号决定模糊查询的匹配列
    private JComboBox<String> searchTypeBox = UiTheme.createComboBox(new String[]{"用户名", "姓名", "手机号"});

    // 查询关键词输入框：配合右侧下拉框按对应列模糊查询
    private JTextField searchField = new JTextField(14); // 【可修改参数】查询框推荐列数（14）

    // 查询按钮：按下拉框选中的类型执行数据库模糊查询
    private JButton searchButton = UiTheme.createRoundButton("查询", UiTheme.PRIMARY, UiTheme.WHITE);

    // 新增用户按钮：主题绿实心，强调主要操作
    private JButton addButton = UiTheme.createRoundButton("新增用户", UiTheme.PRIMARY, UiTheme.WHITE);

    // 操作列当前悬停的行号：-1 表示鼠标不在操作按钮上，用于驱动按钮悬停加深
    private int hoverRow = -1;

    // 操作列当前悬停的列号：7 是"启用/禁用"列，8 是"重置密码"列，-1 表示不在操作列上
    private int hoverCol = -1;

    // 当前显示的用户列表（分类结果或查询结果，与表格行一一对应）
    private List<SysUser> displayList;

    // 当前选中的分类标识：all / admin / staff / disabled，新增或操作成功后按它刷新
    private String currentKey = "all";

    // 底部分页栏：每页显示 50 条（可修改参数：每页条数 50），页码变化时回调 refreshTable 重绘当前页
    private PageBar pageBar = new PageBar(50, new Runnable() {
        public void run() {
            refreshTable();
        }
    });

    /**
     * 构造用户管理面板：保存登录用户 + 组装分类栏与列表区，默认加载"全部用户"
     *
     * @param currentUser 当前登录用户，用于禁用自己校验
     */
    public UserPanel(SysUser currentUser) {
        // 保存当前登录用户：启用/禁用时据此拦截"禁用自己"
        this.currentUser = currentUser;
        // 顶部工具栏（含用户分类下拉框）+ 下方列表区的两段布局
        setLayout(new BorderLayout());
        // 面板底色浅灰，与主窗口内容区一致（颜色可修改参数：UiTheme.BG）
        setBackground(UiTheme.BG);
        // 初始化表格外观与操作列按钮
        initTable();
        // 组装列表区：用户分类下拉框已并入工具栏，表格从导航栏旁一直排到右边
        add(createListPanel(), BorderLayout.CENTER);
        // 默认加载"全部用户"，与下拉框默认选中项一致
        loadCategory("all");
    }

    // 把分类下拉框的选中文字转成内部标识：all / admin / staff / disabled
    private String categoryTextToKey(String categoryText) {
        if ("管理员".equals(categoryText)) {
            return "admin";
        }
        if ("员工".equals(categoryText)) {
            return "staff";
        }
        if ("禁用用户".equals(categoryText)) {
            return "disabled";
        }
        // 其余情况按"全部用户"处理
        return "all";
    }

    // 按分类标识加载用户并回到第 1 页：key 与下拉框选项一一对应
    private void loadCategory(String key) {
        // 记住当前分类：新增或操作成功后按它刷新，保持用户所在视角
        currentKey = key;
        // 装载当前分类的数据
        loadCategoryData();
        // 数据变化后回到第 1 页：切换分类视为重新浏览
        pageBar.setTotal(displayList.size());
        // 加载完成刷新表格
        refreshTable();
    }

    // 按当前分类标识调用对应 Service 方法装载数据（不刷新表格、不动页码）
    private void loadCategoryData() {
        if ("admin".equals(currentKey)) {
            displayList = userService.findByRole(currentUser, SysUser.ROLE_ADMIN);
        } else if ("staff".equals(currentKey)) {
            displayList = userService.findByRole(currentUser, SysUser.ROLE_STAFF);
        } else if ("disabled".equals(currentKey)) {
            displayList = userService.findByStatus(currentUser, SysUser.STATUS_DISABLED);
        } else {
            displayList = userService.findAll(currentUser);
        }
    }

    // 重新装载当前分类数据并保持当前页码：行内按钮操作、对话框关闭后调用，避免页码跳回第 1 页
    private void reloadKeepPage() {
        // 装载当前分类的数据
        loadCategoryData();
        // 更新总条数并保持当前页（当前页越界时收敛到最后一页）
        pageBar.setTotalKeepPage(displayList.size());
        refreshTable();
    }

    // 把表格行号换算成 displayList 下标：表格只显示当前页数据，需加上页首偏移
    private int toListIndex(int row) {
        return (pageBar.getCurrentPage() - 1) * pageBar.getPageSize() + row;
    }

    // 按下拉框选中的类型执行数据库模糊查询；关键词为空时弹提示要求输入
    private void doSearch() {
        // 取出关键词并去掉首尾空格
        String keyword = searchField.getText().trim();
        // 查询为空时不执行查询，弹提示要求先输入关键词
        if (keyword.isEmpty()) {
            UiTheme.showMessageDialog(this, "请输入查询关键词");
            return;
        }
        // 按下拉框选中项决定查询列，选项文字与 DAO 白名单字段一一对应
        String field;
        if ("用户名".equals(searchTypeBox.getSelectedItem())) {
            field = "username";
        } else if ("姓名".equals(searchTypeBox.getSelectedItem())) {
            field = "real_name";
        } else {
            field = "phone";
        }
        displayList = userService.findUsersByKeyword(currentUser, keyword, field);
        // 搜索结果视为重新浏览：更新总条数并回到第 1 页
        pageBar.setTotal(displayList.size());
        // 查询完成刷新表格
        refreshTable();
    }

    // 用 displayList 当前页的数据刷新表格行；创建时间格式化为 yyyy-MM-dd HH:mm 文本
    private void refreshTable() {
        // 清空旧行后逐行填充
        tableModel.setRowCount(0);
        // 创建时间显示格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // displayList 在构造方法 loadCategory 后一定有值，判空只是防御极端时序
        if (displayList == null) {
            return;
        }
        // 计算当前页的数据区间：[页首下标, min(页尾下标, 总条数))
        int from = (pageBar.getCurrentPage() - 1) * pageBar.getPageSize();
        int to = Math.min(from + pageBar.getPageSize(), displayList.size());
        // 只填充当前页区间内的用户
        for (int i = from; i < to; i++) {
            SysUser user = displayList.get(i);
            tableModel.addRow(new Object[]{
                    user.getUserName(),
                    user.getRealName(),
                    user.getPhone(),
                    // 角色、状态用文字展示，与系统其他页面口径一致
                    user.getRole() != null && user.getRole() == SysUser.ROLE_ADMIN ? "管理员" : "员工",
                    user.getStatus() != null && user.getStatus() == SysUser.STATUS_ENABLED ? "正常" : "禁用",
                    user.getCreatedTime() == null ? "" : user.getCreatedTime().format(formatter),
                    // 修改时间为空（新用户未做过修改）时显示空串，避免空指针
                    user.getUpdatedTime() == null ? "" : user.getUpdatedTime().format(formatter),
                    // 两列操作按钮由各自渲染器绘制，单元格值仅作占位
                    "", ""});
        }
    }

    // 初始化表格外观：行高、网格线、选中底色、表头样式、列宽与操作列按钮（渲染器 + 编辑器 + 悬停跟踪）
    private void initTable() {
        table.setFont(UiTheme.FONT_NORMAL); // 【可修改参数】表格正文字体
        table.setRowHeight(30); // 【可修改参数】表格行高（30）
        table.setShowGrid(true);
        table.setGridColor(UiTheme.BORDER); // 【可修改参数】表格网格线颜色
        table.setSelectionBackground(new Color(0xE0F2F1)); // 【可修改参数】选中行底色（主题绿极浅版）
        table.setSelectionForeground(UiTheme.TEXT_DARK);
        // 表头样式：与表格正文字体一致，禁止拖动列顺序保持布局稳定
        table.getTableHeader().setFont(UiTheme.FONT_NORMAL);
        table.getTableHeader().setBackground(UiTheme.BG);
        table.getTableHeader().setReorderingAllowed(false);
        // 列宽：内容固定的列收紧，用户名/姓名列留余量防截断
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // 【可修改参数】用户名列宽（长短不定，留余量）
        table.getColumnModel().getColumn(1).setPreferredWidth(90); // 【可修改参数】姓名列宽
        table.getColumnModel().getColumn(2).setPreferredWidth(115); // 【可修改参数】手机号列宽（固定 11 位 + 列头 3 字）
        table.getColumnModel().getColumn(3).setPreferredWidth(65); // 【可修改参数】角色列宽（最长"管理员"3 字）
        table.getColumnModel().getColumn(4).setPreferredWidth(60); // 【可修改参数】状态列宽（最长"正常/禁用"2 字）
        table.getColumnModel().getColumn(5).setPreferredWidth(145); // 【可修改参数】创建时间列宽（固定 16 字符）
        table.getColumnModel().getColumn(6).setPreferredWidth(145); // 【可修改参数】修改时间列宽（固定 16 字符）
        table.getColumnModel().getColumn(7).setPreferredWidth(95); // 【可修改参数】启用/禁用列宽（列头 5 字 + 按钮 2 字）
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // 【可修改参数】重置密码列宽（列头与按钮均 4 字）

        // 操作列悬停跟踪：表格单元格里的按钮不接收鼠标事件，悬停变色由表格代为跟踪行列号后交给渲染器
        MouseAdapter hoverTracker = new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                // 鼠标所在列是行尾两列操作列（第 7、8 列）时记录行列号，否则记 -1 表示不在按钮上
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                int newHoverRow = (col >= 7) ? row : -1;
                int newHoverCol = (col >= 7) ? col : -1;
                // 悬停位置变化时重绘表格，渲染器会用新的悬停状态画按钮
                if (newHoverRow != hoverRow || newHoverCol != hoverCol) {
                    hoverRow = newHoverRow;
                    hoverCol = newHoverCol;
                    table.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                // 鼠标移出表格时清除悬停位置，按钮恢复原色
                if (hoverRow != -1 || hoverCol != -1) {
                    hoverRow = -1;
                    hoverCol = -1;
                    table.repaint();
                }
            }
        };
        table.addMouseListener(hoverTracker);
        table.addMouseMotionListener(hoverTracker);

        // "启用/禁用"列渲染按钮：所有行共用同一个外观实例，文字随该行用户状态变化
        JButton renderStatusButton = createCellButton("禁用");
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // 按钮文字随该行用户状态变化：正常 → "禁用"，已禁用 → "启用"
                if (displayList != null && row >= 0 && toListIndex(row) < displayList.size()) {
                    // 表格行号加上页首偏移才是列表下标
                    SysUser target = displayList.get(toListIndex(row));
                    renderStatusButton.setText(target.getStatus() != null && target.getStatus() == SysUser.STATUS_ENABLED ? "禁用" : "启用");
                }
                // 鼠标悬停在当前行该按钮上时底色加深，与"新增药品"按钮悬停效果一致；移开后恢复主题绿
                renderStatusButton.setBackground(row == hoverRow && hoverCol == 7 ? UiTheme.PRIMARY_DARK : UiTheme.PRIMARY);
                return renderStatusButton;
            }
        });

        // "重置密码"列渲染按钮：所有行共用同一个外观实例
        JButton renderResetButton = createCellButton("重置密码");
        table.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // 鼠标悬停在当前行该按钮上时底色加深；移开后恢复主题绿
                renderResetButton.setBackground(row == hoverRow && hoverCol == 8 ? UiTheme.PRIMARY_DARK : UiTheme.PRIMARY);
                return renderResetButton;
            }
        });

        // 两列各自独立编辑器：单击按钮即触发对应操作
        table.getColumnModel().getColumn(7).setCellEditor(new StatusButtonEditor());
        table.getColumnModel().getColumn(8).setCellEditor(new ResetButtonEditor());
    }

    // 启用/禁用指定行用户：点击操作列按钮后把 status 切换为 0 或 1，按钮文字随新状态变化
    private void toggleStatus(int row) {
        // 表格行号加上页首偏移才是列表下标
        SysUser target = displayList.get(toListIndex(row));
        // 管理员不能禁用自己（Service 同样兜底校验）
        if (target.getId() != null && target.getId().equals(currentUser.getId())) {
            UiTheme.showMessageDialog(this, "不能禁用当前登录账号");
            return;
        }
        // 判断本次操作方向：正常用户 → 禁用（status=0）；已禁用 → 启用（status=1）
        boolean disabling = target.getStatus() != null && target.getStatus() == SysUser.STATUS_ENABLED;
        int newStatus = disabling ? SysUser.STATUS_DISABLED : SysUser.STATUS_ENABLED;
        if (userService.updateStatus(currentUser, target.getId(), newStatus)) {
            // 成功后保持当前页刷新，该行操作按钮的文字随新状态变化
            reloadKeepPage();
        } else {
            // 失败统一提示并保持当前页刷新，让显示回到数据库的真实状态
            UiTheme.showMessageDialog(this, "操作失败，请重试");
            reloadKeepPage();
        }
    }

    // 重置密码：弹确认框（确定 / 取消），确定后把指定行用户密码重置为系统默认密码 123456
    private void resetPassword(int row) {
        // 表格行号加上页首偏移才是列表下标
        SysUser target = displayList.get(toListIndex(row));
        // 确认框：确定返回 true，取消返回 false
        if (!UiTheme.showConfirmDialog(this, "确认要重置密码吗")) {
            return;
        }
        // 重置为系统默认密码，BCrypt 哈希与入库都在 Service 完成
        if (userService.resetPassword(currentUser, target.getId(), "123456")) {
            UiTheme.showMessageDialog(this, "密码重置成功，该用户可使用默认密码 123456 登录");
        } else {
            UiTheme.showMessageDialog(this, "重置失败，请重试");
        }
    }

    // 创建右侧列表区：顶部工具栏 + 用户表格
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG);
        // 顶部工具栏
        panel.add(createToolBar(), BorderLayout.NORTH);

        // 表格滚动容器：数据多时可上下滚动
        JScrollPane scrollPane = new JScrollPane(table);
        // 去掉滚动面板自带立体边框，保持扁平风格
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UiTheme.WHITE);

        // 表格外框：1px 浅灰描边，和输入框风格呼应
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(UiTheme.WHITE);
        tableWrap.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 1)); // 【可修改参数】表格外框颜色与粗细
        tableWrap.add(scrollPane, BorderLayout.CENTER);

        // 表格区外层留白：让表格不贴面板边缘
        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setBackground(UiTheme.BG);
        tableArea.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16)); // 【可修改参数】表格区四周留白
        tableArea.add(tableWrap, BorderLayout.CENTER);
        panel.add(tableArea, BorderLayout.CENTER);
        // 底部分页栏：上一页 / 当前页数/总页数 / 下一页 / 总数据条数
        panel.add(pageBar, BorderLayout.SOUTH);
        return panel;
    }

    // 创建表格内操作列按钮：主题绿实心，与"新增用户"按钮同风格
    private JButton createCellButton(String text) {
        JButton button = UiTheme.createFlatButton(text, UiTheme.PRIMARY, UiTheme.WHITE);
        // 收紧按钮内边距：工厂默认 8/18 在表格 30 行高里会显得臃肿
        button.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10)); // 【可修改参数】按钮内边距（上下 3 / 左右 10）
        return button;
    }

    // "启用/禁用"列按钮编辑器：单击时切换该行用户状态
    private class StatusButtonEditor extends AbstractCellEditor implements TableCellEditor {

        // 编辑器外观：与渲染按钮一致的按钮
        private JButton button = createCellButton("禁用");

        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
            // 激活时同步按钮文字：正常 → "禁用"，已禁用 → "启用"
            if (displayList != null && row >= 0 && toListIndex(row) < displayList.size()) {
                // 表格行号加上页首偏移才是列表下标
                SysUser target = displayList.get(toListIndex(row));
                button.setText(target.getStatus() != null && target.getStatus() == SysUser.STATUS_ENABLED ? "禁用" : "启用");
            }
            // 延迟到本次点击事件结束后再处理：先结束编辑状态，避免单元格停留在编辑模式
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stopCellEditing();
                    toggleStatus(row);
                }
            });
            return button;
        }

        public Object getCellEditorValue() {
            return "禁用";
        }
    }

    // "重置密码"列按钮编辑器：单击时弹确认框，确定后重置该行用户密码
    private class ResetButtonEditor extends AbstractCellEditor implements TableCellEditor {

        // 编辑器外观：与渲染按钮一致的"重置密码"按钮
        private JButton button = createCellButton("重置密码");

        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
            // 延迟到本次点击事件结束后再处理：先结束编辑状态，避免单元格停留在编辑模式
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stopCellEditing();
                    resetPassword(row);
                }
            });
            return button;
        }

        public Object getCellEditorValue() {
            return "重置密码";
        }
    }

    // 创建顶部工具栏：左侧分类下拉框 + 查询类型下拉框 + 输入框 + 查询按钮，右侧新增用户按钮
    private JPanel createToolBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // 【可修改参数】工具栏四周留白（上 12 / 左右 16 / 下 12）

        // 左侧：控件组，水平排列、垂直居中
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // 【可修改参数】查询控件水平间距（8）
        left.setBackground(UiTheme.BG);
        // 分类下拉框放最左：与药品管理、销售管理页的分类下拉框位置一致
        left.add(categoryBox);
        // 查询类型下拉框注册监听：已输入关键词时切换类型立即重新查询，空关键词时仅切换类型不动作
        searchTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!searchField.getText().trim().isEmpty()) {
                    doSearch();
                }
            }
        });
        left.add(searchTypeBox);
        // 查询框安装静态圆角边框，风格与登录界面输入框一致
        searchField.setOpaque(false);
        searchField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        // 回车等同点击查询
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        left.add(searchField);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        left.add(searchButton);
        panel.add(left, BorderLayout.WEST);

        // 右侧：新增用户按钮 → 打开新增用户对话框，关闭后刷新当前分类
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new UserDialog(SwingUtilities.getWindowAncestor(UserPanel.this), currentUser).setVisible(true);
                // 关闭后保持当前页刷新：新用户按编号排在末页，可通过页码按钮跳转查看
                reloadKeepPage();
            }
        });
        panel.add(addButton, BorderLayout.EAST);
        return panel;
    }
}
