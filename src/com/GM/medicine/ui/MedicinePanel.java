package com.GM.medicine.ui;

// 导入 BorderLayout：面板按 顶部工具栏 / 下方列表区 组织，工具栏内含药品分类下拉框
import java.awt.BorderLayout;
// 导入 Color：表格选中行的浅绿底色
import java.awt.Color;
// 导入 Component：表格渲染器与编辑器的方法返回类型
import java.awt.Component;
// 导入 FlowLayout：工具栏查询控件的排列
import java.awt.FlowLayout;
// 导入 DateTimeFormatter：有效期列格式化为 yyyy-MM-dd 文本
import java.time.format.DateTimeFormatter;
// 导入 ActionEvent：下拉框选择、按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为药品分类下拉框、查询类型下拉框、按钮注册监听的接口
import java.awt.event.ActionListener;
// 导入 MouseEvent：表格悬停跟踪的鼠标事件参数类型
import java.awt.event.MouseEvent;
// 导入 MouseAdapter：一个类同时接收点击与移动事件，用于跟踪操作列的悬停行号
import java.awt.event.MouseAdapter;
// 导入 BorderFactory：面板留白、表格外框、按钮描边
import javax.swing.BorderFactory;
// 导入 AbstractCellEditor：操作列按钮编辑器的基类，提供编辑状态管理与 fireEditingStopped
import javax.swing.AbstractCellEditor;
// 导入 DefaultTableModel：表格数据模型，控制只有操作列可编辑
import javax.swing.table.DefaultTableModel;
// 导入 TableCellEditor：操作列编辑器接口
import javax.swing.table.TableCellEditor;
// 导入 JButton：查询按钮、表格操作按钮
import javax.swing.JButton;
// 导入 JPanel：承载工具栏、表格区的容器
import javax.swing.JPanel;
// 导入 JScrollPane：表格滚动容器，数据多时可上下滚动
import javax.swing.JScrollPane;
// 导入 JTable：药品列表表格
import javax.swing.JTable;
// 导入 JTextField：药品名称查询输入框
import javax.swing.JTextField;
// 导入 JComboBox：药品分类下拉框、查询类型下拉框
import javax.swing.JComboBox;
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
 * - 顶部工具栏用"药品分类"下拉框切换数据视角（全部 / 正常 / 预警 / 过期），选择后立即刷新表格
 * - 表格展示药品并提供查询、新增、修改入口；数据获取全部通过 MedicineService 完成，本类不写 SQL；不提供删除功能
 * - 查询按下拉框选中的类型（药品名称 / 药品类别）调用 Service 做数据库模糊查询；关键词为空时恢复当前分类视图
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

    // 药品表格：展示当前分类或查询结果的药品
    private JTable table = new JTable(tableModel);

    // 药品分类下拉框：四个固定选项（全部 / 正常 / 预警 / 过期），选择后立即刷新表格
    private JComboBox<String> categoryBox = UiTheme.createComboBox(new String[]{"全部药品", "正常药品", "预警药品", "过期药品"});

    // 查询类型下拉框：选"药品名称"按名称查、选"药品类别"按类别查，扁平风格由 UiTheme 工厂统一
    private JComboBox<String> searchTypeBox = UiTheme.createComboBox(new String[]{"药品名称", "药品类别"});

    // 查询关键词输入框：配合左侧下拉框按名称或类别模糊查询
    private JTextField searchField = UiTheme.createTextField(14); // 可修改参数：查询框推荐列数（14）

    // 查询按钮：按下拉框选中的类型执行数据库模糊查询
    private JButton searchButton = UiTheme.createRoundButton("查询", UiTheme.PRIMARY, UiTheme.WHITE);

    // 新增药品按钮：主题绿实心，强调主要操作
    private JButton addButton = UiTheme.createRoundButton("新增药品", UiTheme.PRIMARY, UiTheme.WHITE);

    // 当前分类的完整药品列表（Service 返回的原始结果）
    private List<Medicine> categoryList = new ArrayList<>();

    // 当前显示的药品列表（分类结果或查询结果，与表格行一一对应）
    private List<Medicine> displayList = new ArrayList<>();

    // 当前选中的分类标识：all / available / warning / expired
    private String currentKey = "all";

    // 操作列当前悬停的行号：-1 表示鼠标不在"修改"按钮上，用于驱动按钮悬停加深
    private int hoverRow = -1;

    /**
     * 构造药品管理面板：组装工具栏与列表区，默认加载"全部药品"
     */
    public MedicinePanel() {
        // 顶部工具栏（含药品分类下拉框）+ 下方列表区的两段布局
        setLayout(new BorderLayout());
        // 面板底色浅灰，与主窗口内容区一致（颜色可修改参数：UiTheme.BG）
        setBackground(UiTheme.BG);
        // 初始化表格外观与操作列按钮
        initTable();
        // 组装列表区：药品分类下拉框已并入工具栏，表格从导航栏旁一直排到右边
        add(createListPanel(), BorderLayout.CENTER);
        // 默认加载"全部药品"，与分类下拉框默认选中项一致
        loadCategory("all");
    }

    // 把分类下拉框的选中文字转成内部标识：all / available / warning / expired
    private String categoryTextToKey(String categoryText) {
        if ("正常药品".equals(categoryText)) {
            return "available";
        }
        if ("预警药品".equals(categoryText)) {
            return "warning";
        }
        if ("过期药品".equals(categoryText)) {
            return "expired";
        }
        // 其余情况按"全部药品"处理
        return "all";
    }

    // 按分类标识加载药品列表：key 与分类下拉框选项一一对应，分别调用对应 Service 方法
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
        // 分类结果直接作为显示列表（查询已改为数据库模糊匹配，不再在分类结果内做内存过滤）
        displayList = categoryList;
        refreshTable();
    }

    // 按下拉框选中的类型执行数据库模糊查询；关键词为空时恢复当前分类视图
    private void doSearch() {
        // 取出关键词并去掉首尾空格
        String keyword = searchField.getText().trim();
        // 关键词为空：不做模糊查询，恢复当前分类的完整列表
        if (keyword.isEmpty()) {
            loadCategory(currentKey);
            return;
        }
        // 按下拉框选中项决定查询维度，选项文字与查询字段一一对应
        if ("药品类别".equals(searchTypeBox.getSelectedItem())) {
            displayList = medicineService.findByCategory(keyword);
        } else {
            displayList = medicineService.findByName(keyword);
        }
        // 查询完成刷新表格
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
        // 列宽：长短不定的列（名称/规格/厂家/价格/库存）留余量防截断，内容固定的列（日期/状态/操作）收紧
        table.getColumnModel().getColumn(0).setPreferredWidth(140); // 可修改参数：药品名称列宽（药名长短不定，留余量）
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // 可修改参数：分类列宽（最长"解热镇痛"4 字）
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // 可修改参数：规格列宽（如"0.25g*24粒/盒"约 13 字符）
        table.getColumnModel().getColumn(3).setPreferredWidth(160); // 可修改参数：生产厂家列宽（厂名长短最悬殊，余量最大）
        table.getColumnModel().getColumn(4).setPreferredWidth(70); // 可修改参数：销售价格列宽（保证列头"销售价格"4 字完整）
        table.getColumnModel().getColumn(5).setPreferredWidth(70); // 可修改参数：库存列宽（数量长短不定，留余量）
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // 可修改参数：有效期至列宽（固定 10 字符日期 + 列头 4 字）
        table.getColumnModel().getColumn(7).setPreferredWidth(50); // 可修改参数：状态列宽（最长"正常/停售"2 字）
        table.getColumnModel().getColumn(8).setPreferredWidth(70); // 可修改参数：操作列宽（"修改"按钮宽度）

        // 操作列悬停跟踪：表格单元格里的按钮不接收鼠标事件，悬停变色由表格代为跟踪行号后交给渲染器
        MouseAdapter hoverTracker = new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                // 鼠标所在列是操作列（第 8 列）时记录行号，否则记 -1 表示不在按钮上
                int row = table.rowAtPoint(e.getPoint());
                int newHoverRow = (table.columnAtPoint(e.getPoint()) == 8) ? row : -1;
                // 悬停行变化时重绘表格，渲染器会用新的悬停状态画按钮
                if (newHoverRow != hoverRow) {
                    hoverRow = newHoverRow;
                    table.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                // 鼠标移出表格时清除悬停行，按钮恢复原色
                if (hoverRow != -1) {
                    hoverRow = -1;
                    table.repaint();
                }
            }
        };
        table.addMouseListener(hoverTracker);
        table.addMouseMotionListener(hoverTracker);

        // 操作列渲染按钮：所有行的"修改"按钮共用同一个外观实例
        JButton renderButton = createCellButton();
        table.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // 鼠标悬停在当前行的"修改"按钮上时底色加深，与"新增药品"按钮悬停效果一致；移开后恢复主题绿
                if (row == hoverRow) {
                    renderButton.setBackground(UiTheme.PRIMARY_DARK);
                } else {
                    renderButton.setBackground(UiTheme.PRIMARY);
                }
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

        // 左侧：查询控件，水平排列、垂直居中
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // 可修改参数：查询控件水平间距（8）
        left.setBackground(UiTheme.BG);
        // 药品分类下拉框：放在最左边，选中即切换数据视角（全部/正常/预警/过期）
        categoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 把选中文字转成标识后按对应 Service 方法刷新
                loadCategory(categoryTextToKey((String) categoryBox.getSelectedItem()));
            }
        });
        left.add(categoryBox);
        // 下拉框注册监听：已输入关键词时切换类型立即重新查询，空关键词时仅切换类型不动作
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
