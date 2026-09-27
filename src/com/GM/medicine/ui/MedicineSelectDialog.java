package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 顶部搜索区 / 中表格 / 底按钮 三段组织
import java.awt.BorderLayout;
// 导入 Color：表格选中行的浅绿底色
import java.awt.Color;
// 导入 Component：表格渲染器与编辑器的方法返回类型
import java.awt.Component;
// 导入 Dimension：按钮尺寸
import java.awt.Dimension;
// 导入 FlowLayout：搜索区与底部按钮区的控件排列
import java.awt.FlowLayout;
// 导入 ActionEvent：按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为搜索、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 MouseEvent：表格悬停跟踪的鼠标事件参数类型
import java.awt.event.MouseEvent;
// 导入 MouseAdapter：一个类同时接收点击与移动事件，用于跟踪操作列的悬停行号
import java.awt.event.MouseAdapter;
// 导入 BorderFactory：搜索区留白、表格滚动容器去立体边框
import javax.swing.BorderFactory;
// 导入 AbstractCellEditor：操作列按钮编辑器的基类，提供编辑状态管理
import javax.swing.AbstractCellEditor;
// 导入 DefaultTableModel：表格数据模型，控制只有操作列可编辑
import javax.swing.table.DefaultTableModel;
// 导入 TableCellEditor：操作列编辑器接口
import javax.swing.table.TableCellEditor;
// 导入 JButton：搜索按钮、取消按钮、表格"选择"按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，供新增销售时弹出选择药品
import javax.swing.JDialog;
// 导入 JPanel：承载搜索区、表格、按钮区的容器
import javax.swing.JPanel;
// 导入 JScrollPane：表格滚动容器，药品多时可上下滚动
import javax.swing.JScrollPane;
// 导入 JTable：药品搜索结果表格
import javax.swing.JTable;
// 导入 JTextField：药品名称搜索输入框
import javax.swing.JTextField;
// 导入 SwingUtilities：编辑器点击后延迟处理选中；getWindowAncestor 找父窗口
import javax.swing.SwingUtilities;
// 导入 TableCellRenderer：把操作列渲染成按钮外观
import javax.swing.table.TableCellRenderer;
// 导入 List：搜索结果列表类型
import java.util.List;
// 导入 Medicine：表格行对应的药品实体类型
import com.GM.medicine.pojo.entity.Medicine;
// 导入 MedicineService：搜索统一通过它完成，本类不写 SQL
import com.GM.medicine.service.MedicineService;

/**
 * - 药品选择对话框（供新增销售弹窗选择药品用）
 * - 顶部按名称模糊搜索，表格只显示可售药品（正常、有库存、未过期，由 Service 过滤）
 * - 点行内"选择"按钮把该行药品对象返回给打开方后自动关闭；"取消"直接关闭不返回
 * - 数据获取全部通过 MedicineService 完成，本类不写 SQL
 */
public class MedicineSelectDialog extends JDialog {

    // 药品业务对象：搜索统一通过它调用
    private MedicineService medicineService = new MedicineService();

    // 药品名称搜索输入框
    private JTextField searchField;

    // 搜索按钮：按下输入框关键词模糊搜索可售药品
    private JButton searchButton = UiTheme.createRoundButton("查询", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：直接关闭对话框，不返回选中结果
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    // 表格列名：编号、名称、类别、规格、厂家、售价、库存 + 操作列
    private final String[] columnNames = {"编号", "药品名称", "类别", "规格", "生产厂家", "售价", "库存", "操作"};

    // 表格数据模型：只有最后一列"操作"可编辑（用于触发行内选择按钮）
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        public boolean isCellEditable(int row, int column) {
            // 仅操作列可编辑；其他列点击不进入编辑状态
            return column == columnNames.length - 1;
        }
    };

    // 药品表格：展示当前搜索结果
    private JTable table = new JTable(tableModel);

    // 当前搜索结果列表：与表格行一一对应，点"选择"时据此取药品对象
    private List<Medicine> displayList;

    // 选中的药品对象：点"选择"后记录，供打开方通过 getSelectedMedicine 取回
    private Medicine selectedMedicine;

    // 操作列当前悬停的行号：-1 表示鼠标不在"选择"按钮上，用于驱动按钮悬停加深
    private int hoverRow = -1;

    /**
     * 构造药品选择对话框
     *
     * @param owner 父窗口，对话框在其上居中
     */
    public MedicineSelectDialog(java.awt.Window owner) {
        super(owner);
        // 模态：选完或取消前不能操作主窗口
        setModal(true);
        setTitle("选择药品");
        // 组装搜索区、表格、按钮区
        initComponents();
        setSize(620, 420); // 【可修改参数】对话框初始大小（宽 620 / 高 420）
        // 在父窗口上方居中
        setLocationRelativeTo(owner);
        // 初始加载全部可售药品
        loadMedicines(medicineService.findAvailableMedicines());
    }

    /**
     * 获取选中的药品对象
     *
     * @return 点"选择"后的药品；点"取消"关闭时为 null
     */
    public Medicine getSelectedMedicine() {
        return selectedMedicine;
    }

    // 组装搜索区、表格区、底部按钮区
    private void initComponents() {
        // 上中下三段布局
        setLayout(new BorderLayout());
        // 对话框整体白底，风格与药品编辑对话框一致
        getContentPane().setBackground(UiTheme.WHITE);
        add(createSearchPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        // 初始化表格外观与操作列按钮
        initTable();
    }

    // 创建顶部搜索区：名称输入框 + 查询按钮
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12)); // 【可修改参数】搜索控件水平间距 8 / 上下留白 12
        panel.setBackground(UiTheme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16)); // 【可修改参数】搜索区左右留白（16）

        // 搜索输入框：静态浅灰圆角边框，与编辑对话框输入框一致
        searchField = UiTheme.createTextField(16); // 【可修改参数】搜索框推荐列数（16）
        searchField.setOpaque(false);
        searchField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        // 回车等同点击查询
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        panel.add(searchField);
        // 查询按钮：主题绿圆角，与主界面查询按钮同款
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        panel.add(searchButton);
        return panel;
    }

    // 创建中部表格区：滚动容器包裹表格，外框与主界面表格一致
    private JPanel createTablePanel() {
        // 表格滚动容器：药品多时可上下滚动
        JScrollPane scrollPane = new JScrollPane(table);
        // 去掉滚动面板自带立体边框，保持扁平风格
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UiTheme.WHITE);

        // 表格外层留白：让表格不贴对话框边缘
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16)); // 【可修改参数】表格区左右留白（16）
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // 创建底部按钮区：居中一个取消按钮
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10)); // 【可修改参数】按钮区上下留白（10）
        panel.setBackground(UiTheme.WHITE);
        cancelButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】取消按钮大小（宽 90 / 高 32）
        panel.add(cancelButton);
        // 取消：直接关闭对话框，不返回选中结果
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        return panel;
    }

    // 初始化表格外观：行高、网格线、选中底色、列宽与操作列按钮，与药品管理页一致
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
        // 列宽：编号/售价/库存收紧，名称/厂家留余量
        table.getColumnModel().getColumn(0).setPreferredWidth(55); // 【可修改参数】编号列宽
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // 【可修改参数】药品名称列宽（药名长短不定，留余量）
        table.getColumnModel().getColumn(2).setPreferredWidth(80); // 【可修改参数】类别列宽
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // 【可修改参数】规格列宽（如"0.25g*24粒/盒"）
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // 【可修改参数】生产厂家列宽（厂名长短不定，留余量）
        table.getColumnModel().getColumn(5).setPreferredWidth(65); // 【可修改参数】售价列宽
        table.getColumnModel().getColumn(6).setPreferredWidth(55); // 【可修改参数】库存列宽
        table.getColumnModel().getColumn(7).setPreferredWidth(70); // 【可修改参数】操作列宽（"选择"按钮宽度）

        // 操作列悬停跟踪：表格单元格里的按钮不接收鼠标事件，悬停变色由表格代为跟踪行号后交给渲染器
        MouseAdapter hoverTracker = new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                // 鼠标所在列是操作列（第 7 列）时记录行号，否则记 -1 表示不在按钮上
                int row = table.rowAtPoint(e.getPoint());
                int newHoverRow = (table.columnAtPoint(e.getPoint()) == 7) ? row : -1;
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

        // 操作列渲染按钮：所有行的"选择"按钮共用同一个外观实例
        JButton renderButton = createCellButton();
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // 鼠标悬停在当前行的"选择"按钮上时底色加深；移开后恢复主题绿
                if (row == hoverRow) {
                    renderButton.setBackground(UiTheme.PRIMARY_DARK);
                } else {
                    renderButton.setBackground(UiTheme.PRIMARY);
                }
                return renderButton;
            }
        });

        // 操作列编辑器：点击"选择"单元格时记录该行药品并关闭对话框
        table.getColumnModel().getColumn(7).setCellEditor(new SelectButtonEditor());
    }

    // 操作列按钮编辑器：单击"选择"单元格时记录对应行的药品对象并关闭对话框
    private class SelectButtonEditor extends AbstractCellEditor implements TableCellEditor {

        // 编辑器外观：与渲染按钮一致的"选择"按钮
        private JButton button = createCellButton();

        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
            // 延迟到本次点击事件结束后再处理：先结束编辑状态，避免单元格停留在编辑模式
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stopCellEditing();
                    // 按当前行号从搜索结果里取药品对象，记录后关闭对话框交还打开方
                    selectedMedicine = displayList.get(row);
                    dispose();
                }
            });
            return button;
        }

        public Object getCellEditorValue() {
            return "选择";
        }
    }

    // 创建表格内"选择"按钮：主题绿实心，与药品管理页"修改"按钮同风格
    private JButton createCellButton() {
        JButton button = UiTheme.createFlatButton("选择", UiTheme.PRIMARY, UiTheme.WHITE);
        // 收紧按钮内边距：工厂默认 8/18 在表格 30 行高里会显得臃肿
        button.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12)); // 【可修改参数】按钮内边距（上下 3 / 左右 12）
        return button;
    }

    // 按输入框关键词搜索：空关键词恢复全部可售药品，否则按名称模糊搜索可售药品
    private void doSearch() {
        // 取出关键词并去掉首尾空格
        String keyword = searchField.getText().trim();
        // 空关键词恢复全部可售药品，与对话框初始状态一致
        if (keyword.isEmpty()) {
            loadMedicines(medicineService.findAvailableMedicines());
            return;
        }
        // 名称模糊搜索，Service 只返回可售药品
        loadMedicines(medicineService.findAvailableByName(keyword));
    }

    // 用传入的药品列表刷新表格行：displayList 与表格行一一对应
    private void loadMedicines(List<Medicine> medicines) {
        // 记住当前结果列表：点"选择"时按行号取药品对象
        displayList = medicines;
        // 清空旧行后逐行填充
        tableModel.setRowCount(0);
        for (Medicine medicine : medicines) {
            tableModel.addRow(new Object[]{
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getCategory(),
                    medicine.getSpecification(),
                    medicine.getManufacturer(),
                    medicine.getSalePrice(),
                    medicine.getStock(),
                    "选择"});
        }
    }
}
