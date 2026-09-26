package com.GM.medicine.ui;

// 导入 BorderLayout：面板按 左分类栏 / 右列表区 组织，列表区再分工具栏与表格
import java.awt.BorderLayout;
// 导入 Color：表格选中行的浅绿底色
import java.awt.Color;
// 导入 Component：给分类按钮设置水平居中对齐、表格渲染器的方法返回类型
import java.awt.Component;
// 导入 Cursor：分类按钮手型光标
import java.awt.Cursor;
// 导入 Dimension：分类栏宽度、按钮尺寸
import java.awt.Dimension;
// 导入 FlowLayout：工具栏左侧查询控件的排列
import java.awt.FlowLayout;
// 导入 DateTimeFormatter：有效期列格式化为 yyyy-MM-dd 文本
import java.time.format.DateTimeFormatter;
// 导入 ActionEvent：按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为分类按钮、查询按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 Box：创建分类按钮之间的固定间距（Strut）
import javax.swing.Box;
// 导入 BoxLayout：分类栏沿垂直方向排列
import javax.swing.BoxLayout;
// 导入 BorderFactory：分割线、面板留白、按钮描边
import javax.swing.BorderFactory;
// 导入 AbstractCellEditor：操作列按钮编辑器的基类，提供编辑状态管理与 fireEditingStopped
import javax.swing.AbstractCellEditor;
// 导入 DefaultTableModel：表格数据模型，控制只有操作列可编辑
import javax.swing.table.DefaultTableModel;
// 导入 TableCellEditor：操作列编辑器接口
import javax.swing.table.TableCellEditor;
// 导入 JButton：分类按钮、查询按钮、表格操作按钮
import javax.swing.JButton;
// 导入 JLabel：分类栏小标题
import javax.swing.JLabel;
// 导入 JPanel：承载分类栏、工具栏、表格区的容器
import javax.swing.JPanel;
// 导入 JScrollPane：表格滚动容器，数据多时可上下滚动
import javax.swing.JScrollPane;
// 导入 JTable：药品列表表格
import javax.swing.JTable;
// 导入 JTextField：药品名称查询输入框
import javax.swing.JTextField;
// 导入 SwingUtilities：编辑器点击后延迟打开对话框；getWindowAncestor 找对话框父窗口
import javax.swing.SwingUtilities;
// 导入 TableCellRenderer：把操作列渲染成按钮外观
import javax.swing.table.TableCellRenderer;
// 导入 ArrayList / List：当前分类与名称过滤后的药品列表
import java.util.ArrayList;
import java.util.List;
// 导入 Medicine：表格行对应的药品实体类型
import com.GM.medicine.pojo.entity.Medicine;
// 导入 MedicineService：分类查询、新增后的刷新统一通过它完成，本类不写 SQL
import com.GM.medicine.service.MedicineService;

/**
 * - 药品管理面板
 * - 左侧按状态分类（全部 / 正常 / 库存预警 / 已过期），右侧表格展示药品并提供查询、新增、修改入口
 * - 数据获取全部通过 MedicineService 完成，本类不写 SQL；不提供删除功能
 * - 名称查询在当前分类结果内做包含匹配（Service 暂无按名称查询方法，不虚构）
 */
public class MedicinePanel extends JPanel {

    // 药品业务对象：分类查询、新增、修改统一通过它调用
    private MedicineService medicineService = new MedicineService();

    // 表格列名：只展示核心字段，批号 / 进价 / 预警库存 / 生产日期在编辑对话框中查看与维护
    private final String[] columnNames = {"药品名称", "分类", "规格", "生产厂家", "销售价格", "库存", "有效期至", "状态", "操作"};

    // 表格数据模型：只有最后一列"操作"可编辑（用于触发行内修改按钮）
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        public boolean isCellEditable(int row, int column) {
            // 仅操作列可编辑；其他列点击不进入编辑状态
            return column == columnNames.length - 1;
        }
    };

    // 药品表格：展示当前分类 + 名称过滤后的药品
    private JTable table = new JTable(tableModel);

    // 名称查询输入框：查询在当前分类结果内做包含匹配
    private JTextField searchField = new JTextField(14); // 可修改参数：查询框推荐列数（14）

    // 查询按钮：按名称过滤当前列表
    private JButton searchButton = UiTheme.createFlatButton("查询", UiTheme.BG, UiTheme.TEXT_DARK);

    // 新增药品按钮：主题绿实心，强调主要操作
    private JButton addButton = UiTheme.createFlatButton("新增药品", UiTheme.PRIMARY, UiTheme.WHITE);

    // 当前分类的完整药品列表（Service 返回的原始结果）
    private List<Medicine> categoryList = new ArrayList<>();

    // 当前显示的药品列表（categoryList 经名称过滤后的结果，与表格行一一对应）
    private List<Medicine> displayList = new ArrayList<>();

    // 当前选中的分类标识：all / available / warning / expired
    private String currentKey = "all";

    // 当前选中的分类按钮：切换时恢复上一个按钮的底色
    private JButton selectedCategory;

    /**
     * 构造药品管理面板：组装分类栏与列表区，默认加载"全部药品"
     */
    public MedicinePanel() {
        // 左分类右列表的两段布局
        setLayout(new BorderLayout());
        // 面板底色浅灰，与主窗口内容区一致（颜色可修改参数：UiTheme.BG）
        setBackground(UiTheme.BG);
        // 初始化表格外观与操作列按钮
        initTable();
        // 组装分类栏、列表区
        add(createCategoryPanel(), BorderLayout.WEST);
        add(createListPanel(), BorderLayout.CENTER);
        // 默认加载"全部药品"，与分类栏默认选中项一致
        loadCategory("all");
    }

    // 按分类标识加载药品列表：key 与左侧分类按钮一一对应，分别调用对应 Service 方法
    private void loadCategory(String key) {
        // 记住当前分类：新增 / 修改成功后按它刷新，保持用户所在视角
        currentKey = key;
        // 按标识调用对应的 Service 查询方法
        if ("available".equals(key)) {
            categoryList = medicineService.findAvailableMedicines();
        } else if ("warning".equals(key)) {
            categoryList = medicineService.findWarningMedicines();
        } else if ("expired".equals(key)) {
            categoryList = medicineService.findExpiredMedicines();
        } else {
            categoryList = medicineService.findAll();
        }
        // 加载后重新应用名称过滤
        applyFilter();
    }

    // 名称过滤：在当前分类结果内做包含匹配；关键词为空时显示全部
    private void applyFilter() {
        // 取出查询词并去掉首尾空格
        String keyword = searchField.getText().trim();
        displayList = new ArrayList<>();
        for (Medicine medicine : categoryList) {
            // 名称包含关键词即保留；空关键词全部保留
            if (keyword.isEmpty() || medicine.getName().contains(keyword)) {
                displayList.add(medicine);
            }
        }
        // 过滤完成刷新表格
        refreshTable();
    }

    // 用 displayList 的数据刷新表格行；日期格式化为 yyyy-MM-dd 文本
    private void refreshTable() {
        // 清空旧行后逐行填充
        tableModel.setRowCount(0);
        // 日期显示格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Medicine medicine : displayList) {
            tableModel.addRow(new Object[]{
                    medicine.getName(),
                    medicine.getCategory(),
                    medicine.getSpecification(),
                    medicine.getManufacturer(),
                    medicine.getSalePrice(),
                    medicine.getStock(),
                    // 有效期为空时显示空串，避免空指针
                    medicine.getExpiryDate() == null ? "" : medicine.getExpiryDate().format(formatter),
                    // 状态：1 = 正常（与 Service.checkMedicineAvailable 的判断一致）
                    medicine.getStatus() != null && medicine.getStatus() == 1 ? "正常" : "停售",
                    "修改"});
        }
    }

    // 初始化表格外观与操作列按钮：渲染成按钮外观 + 点击触发修改对话框
    private void initTable() {
        table.setFont(UiTheme.FONT_NORMAL); // 可修改参数：表格正文字体
        table.setRowHeight(30); // 可修改参数：表格行高（30）
        table.setShowGrid(true);
        table.setGridColor(UiTheme.BORDER); // 可修改参数：表格网格线颜色
        table.setSelectionBackground(new Color(0xE0F2F1)); // 可修改参数：选中行底色（主题绿极浅版）
        table.setSelectionForeground(UiTheme.TEXT_DARK);
        // 表头样式：与表格正文字体一致，禁止拖动列顺序保持布局稳定
        table.getTableHeader().setFont(UiTheme.FONT_NORMAL);
        table.getTableHeader().setBackground(UiTheme.BG);
        table.getTableHeader().setReorderingAllowed(false);
        // 列宽：名称列宽一点，操作列窄一点
        table.getColumnModel().getColumn(0).setPreferredWidth(130); // 可修改参数：药品名称列宽
        table.getColumnModel().getColumn(8).setPreferredWidth(70); // 可修改参数：操作列宽

        // 操作列渲染按钮：所有行的"修改"按钮共用同一个外观实例
        JButton renderButton = createCellButton();
        table.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return renderButton;
            }
        });

        // 操作列编辑器：点击单元格时打开该行的修改对话框（DefaultCellEditor 不支持按钮，需自行实现）
        table.getColumnModel().getColumn(8).setCellEditor(new EditButtonEditor());
    }

    // 操作列按钮编辑器：单击"修改"单元格时打开对应行的药品编辑对话框
    private class EditButtonEditor extends AbstractCellEditor implements TableCellEditor {

        // 编辑器外观：与渲染按钮一致的"修改"按钮
        private JButton button = createCellButton();

        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
            // 延迟到本次点击事件结束后再处理：先结束编辑状态，避免单元格停留在编辑模式
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stopCellEditing();
                    openEditDialog(row);
                }
            });
            return button;
        }

        public Object getCellEditorValue() {
            return "修改";
        }
    }

    // 创建表格内"修改"按钮：主题绿实心，与"新增药品"按钮同风格
    private JButton createCellButton() {
        JButton button = UiTheme.createFlatButton("修改", UiTheme.PRIMARY, UiTheme.WHITE);
        // 收紧按钮内边距：工厂默认 8/18 在表格 30 行高里会显得臃肿
        button.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12)); // 可修改参数：按钮内边距（上下 3 / 左右 12）
        return button;
    }

    // 打开修改对话框：取出该行对应的药品回填；对话框关闭后刷新当前分类
    private void openEditDialog(int row) {
        // displayList 与表格行一一对应
        Medicine medicine = displayList.get(row);
        // 传入该行药品，对话框按修改模式回填
        new MedicineDialog(SwingUtilities.getWindowAncestor(this), medicine).setVisible(true);
        // 关闭后刷新，保证表格显示最新数据
        loadCategory(currentKey);
    }

    // 创建左侧分类栏：小标题 + 四个分类按钮，点击切换分类并高亮
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UiTheme.BG);
        // 外层右侧 1px 分割线（与主窗口导航栏一致）+ 内层四周留白
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 10, 16, 10))); // 可修改参数：分类栏内边距（上 16 / 左右 10 / 下 16）
        // 分类栏固定宽度：只限制横向，纵向随面板变化
        panel.setPreferredSize(new Dimension(120, 0)); // 可修改参数：分类栏宽度（150）

        // "药品分类"分组小标题
        JLabel title = UiTheme.createLabel("药品分类", UiTheme.TEXT_GRAY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：小标题与分类按钮的间距（12）

        // 四个分类按钮：key 分别对应四个 Service 查询方法
        JButton allButton = createCategoryButton("全部药品");
        allButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCategory("all");
                selectCategory(allButton);
            }
        });
        panel.add(allButton);
        panel.add(Box.createVerticalStrut(8)); // 可修改参数：分类按钮之间的间距（8）

        JButton availableButton = createCategoryButton("正常药品");
        availableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCategory("available");
                selectCategory(availableButton);
            }
        });
        panel.add(availableButton);
        panel.add(Box.createVerticalStrut(8));

        JButton warningButton = createCategoryButton("库存预警");
        warningButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCategory("warning");
                selectCategory(warningButton);
            }
        });
        panel.add(warningButton);
        panel.add(Box.createVerticalStrut(8));

        JButton expiredButton = createCategoryButton("过期药品");
        expiredButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCategory("expired");
                selectCategory(expiredButton);
            }
        });
        panel.add(expiredButton);

        // 默认选中"全部药品"，与构造方法中默认加载的分类一致
        selectCategory(allButton);
        return panel;
    }

    // 创建分类按钮：统一样式与尺寸，选中态由 selectCategory 管理（样式与主窗口导航按钮一致）
    private JButton createCategoryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.FONT_NORMAL);
        button.setForeground(UiTheme.TEXT_DARK);
        // 初始底色浅灰，选中后换成白色卡片
        button.setBackground(UiTheme.BG);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 40)); // 可修改参数：分类按钮大小（宽 140 / 高 40）
        button.setMaximumSize(new Dimension(100, 40)); // 防止被 BoxLayout 拉伸 
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // 切换分类选中态：上一个按钮恢复浅灰，新按钮换成白色卡片
    private void selectCategory(JButton button) {
        if (selectedCategory != null) {
            selectedCategory.setBackground(UiTheme.BG);
        }
        selectedCategory = button;
        selectedCategory.setBackground(UiTheme.WHITE);
    }

    // 创建右侧列表区：顶部工具栏 + 药品表格
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
        tableWrap.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER, 1)); // 可修改参数：表格外框颜色与粗细
        tableWrap.add(scrollPane, BorderLayout.CENTER);

        // 表格区外层留白：让表格不贴面板边缘
        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setBackground(UiTheme.BG);
        tableArea.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16)); // 可修改参数：表格区四周留白
        tableArea.add(tableWrap, BorderLayout.CENTER);
        panel.add(tableArea, BorderLayout.CENTER);
        return panel;
    }

    // 创建顶部工具栏：左侧名称查询框 + 查询按钮，右侧新增药品按钮
    private JPanel createToolBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // 可修改参数：工具栏四周留白（上 12 / 左右 16 / 下 12）

        // 左侧：查询框 + 查询按钮，水平排列、垂直居中
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // 可修改参数：查询控件水平间距（8）
        left.setBackground(UiTheme.BG);
        // 查询框安装静态圆角边框，风格与登录界面输入框一致
        searchField.setOpaque(false);
        searchField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        // 回车等同点击查询
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        left.add(searchField);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
        left.add(searchButton);
        panel.add(left, BorderLayout.WEST);

        // 右侧：新增药品按钮 → 打开新增对话框，关闭后刷新当前分类
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // medicine 传 null，对话框按新增模式处理
                new MedicineDialog(SwingUtilities.getWindowAncestor(MedicinePanel.this), null).setVisible(true);
                // 关闭后刷新，让新药品立即出现在列表里
                loadCategory(currentKey);
            }
        });
        panel.add(addButton, BorderLayout.EAST);
        return panel;
    }
}
