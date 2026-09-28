package com.GM.medicine.ui;

// 导入 BorderLayout：面板按 顶部工具栏 / 下方列表区 组织，工具栏内含时间范围下拉框
import java.awt.BorderLayout;
// 导入 Color：表格选中行的浅绿底色
import java.awt.Color;
// 导入 FlowLayout：工具栏查询控件的排列
import java.awt.FlowLayout;
// 导入 DateTimeFormatter：销售时间列格式化为 yyyy-MM-dd HH:mm:ss 文本
import java.time.format.DateTimeFormatter;
// 导入 LocalDate：自定义时间范围的开始/结束日期类型
import java.time.LocalDate;
// 导入 ActionEvent：下拉框、按钮选择与点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为时间范围下拉框、查询类型下拉框、按钮注册监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：面板留白、表格外框
import javax.swing.BorderFactory;
// 导入 DefaultTableModel：表格数据模型，控制所有列都不可编辑
import javax.swing.table.DefaultTableModel;
// 导入 JButton：查询按钮、新增销售按钮
import javax.swing.JButton;
// 导入 JComboBox：时间范围下拉框、查询类型下拉框
import javax.swing.JComboBox;
// 导入 JPanel：承载工具栏、表格区的容器
import javax.swing.JPanel;
// 导入 JScrollPane：表格滚动容器，数据多时可上下滚动
import javax.swing.JScrollPane;
// 导入 JTable：销售记录列表表格
import javax.swing.JTable;
// 导入 JTextField：查询关键词输入框
import javax.swing.JTextField;
// 导入 SwingUtilities：getWindowAncestor 找新增销售对话框的父窗口
import javax.swing.SwingUtilities;
// 导入 List：当前显示的销售记录列表类型
import java.util.List;
// 导入 Sale：表格行对应的销售记录实体类型
import com.GM.medicine.pojo.entity.Sale;
// 导入 SysUser：当前登录用户，新增销售时作为操作员传给对话框
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SaleService：时间范围查询、模糊查询、新增后的刷新统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SaleService;

/**
 * - 销售管理面板
 * - 顶部工具栏用"时间范围"下拉框切换数据视角（全部 / 今日 / 本周 / 本月 / 自定义），选择后立即刷新表格
 * - 选"自定义"弹出 SaleDate 输入开始/结束日期；查询按下拉框类型受当前时间范围过滤
 * - 右侧表格展示销售记录（含联表带出的药品信息）并提供查询、新增销售入口
 * - 数据获取全部通过 SaleService 完成，本类不写 SQL；销售记录不提供删除、修改功能
 * - 查询按下拉框选中的类型（药品名称 / 操作员）调用 Service 做数据库模糊查询；关键词为空时恢复当前范围完整列表
 * - 底部分页栏每页 50 条：滚轮翻页保留，另加上一页/下一页与页码跳转；切换时间范围或搜索回到第 1 页，新增销售保持当前页
 */
public class SalePanel extends JPanel {

    // 销售业务对象：时间范围查询、模糊查询、新增统一通过它调用
    private SaleService saleService = new SaleService();

    // 当前登录用户：新增销售时作为操作员身份传给销售对话框
    private SysUser currentUser;

    // 时间范围下拉框的五个固定选项：四个预设范围 + 自定义时间（不提供日期选择器，日期手输 yyyy-MM-dd）
    private final String[] rangeItems = {"全部销售", "今日销售", "本周销售", "本月销售", "自定义"};

    // 自定义时间区间：点"确定"校验通过后记录；结束为 null 表示查开始当天
    private LocalDate customStart;

    // 自定义时间区间右端（含当天），与 customStart 一起传给 Service 的组合查询入口
    private LocalDate customEnd;

    // 恢复下拉框选中项时置 true：让恢复动作触发的监听调用直接跳过，不当成用户选择
    private boolean restoringRange;

    // 表格列名：销售编号、药品名称、药品类别、规格、生产厂家、操作员、数量、单价、总价、销售时间、备注
    private final String[] columnNames = {"销售编号", "药品名称", "药品类别", "规格", "生产厂家", "操作员", "数量", "单价", "总价", "销售时间", "备注"};

    // 表格数据模型：销售记录只读，所有列都不可编辑
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        public boolean isCellEditable(int row, int column) {
            // 销售记录不允许在表格里修改，任何单元格点击都不进入编辑状态
            return false;
        }
    };

    // 销售记录表格：展示当前时间范围或查询结果的销售记录
    private JTable table = new JTable(tableModel);

    // 时间范围下拉框：选择后立即按对应 Service 方法刷新表格
    private JComboBox<String> rangeBox = UiTheme.createComboBox(rangeItems);

    // 查询类型下拉框：选"编号"按销售编号前缀查、选"药品名称"按药品名查、选"操作员"按操作员姓名查
    private JComboBox<String> searchTypeBox = UiTheme.createComboBox(new String[]{"药品名称","编号",  "操作员"});

    // 查询关键词输入框：配合右侧下拉框按药品名称或操作员姓名模糊查询
    private JTextField searchField = UiTheme.createTextField(14); // 【可修改参数】查询框推荐列数（14）

    // 查询按钮：按下拉框选中的类型执行数据库模糊查询
    private JButton searchButton = UiTheme.createRoundButton("查询", UiTheme.PRIMARY, UiTheme.WHITE);

    // 新增销售按钮：主题绿实心，强调主要操作
    private JButton addButton = UiTheme.createRoundButton("新增销售", UiTheme.PRIMARY, UiTheme.WHITE);

    // 当前显示的销售记录列表（时间范围结果或查询结果，与表格行一一对应）
    private List<Sale> displayList;

    // 当前选中的时间范围标识：all / today / week / month / custom，新增销售成功后按它刷新
    private String currentKey = "all";

    // 底部分页栏：每页显示 50 条（可修改参数：每页条数 50），页码变化时回调 refreshTable 重绘当前页
    private PageBar pageBar = new PageBar(50, new Runnable() {
        public void run() {
            refreshTable();
        }
    });

    /**
     * 构造销售管理面板：保存登录用户 + 组装时间范围栏与列表区，默认加载"全部销售"
     *
     * @param currentUser 当前登录用户，新增销售时作为操作员身份使用
     */
    public SalePanel(SysUser currentUser) {
        // 保存当前登录用户：打开销售对话框时传给它，由它决定销售记录的操作员
        this.currentUser = currentUser;
        // 顶部工具栏（含时间范围下拉框）+ 下方列表区的两段布局
        setLayout(new BorderLayout());
        // 面板底色浅灰，与主窗口内容区一致（颜色可修改参数：UiTheme.BG）
        setBackground(UiTheme.BG);
        // 初始化表格外观
        initTable();
        // 组装列表区：时间范围下拉框已并入工具栏，表格从导航栏旁一直排到右边
        add(createListPanel(), BorderLayout.CENTER);
        // 默认加载"全部销售"，与下拉框默认选中项一致
        loadCategory("all");
    }

    // 把时间范围下拉框的选中文字转成内部标识：all / today / week / month / custom
    private String rangeTextToKey(String rangeText) {
        if ("今日销售".equals(rangeText)) {
            return "today";
        }
        if ("本周销售".equals(rangeText)) {
            return "week";
        }
        if ("本月销售".equals(rangeText)) {
            return "month";
        }
        if ("自定义".equals(rangeText)) {
            return "custom";
        }
        // 其余情况按"全部销售"处理
        return "all";
    }

    // 把查询类型下拉框的选中文字转成 Service 组合查询的类型标识：id / operator / name
    private String typeKey() {
        if ("操作员".equals(searchTypeBox.getSelectedItem())) {
            return "operator";
        }
        if ("编号".equals(searchTypeBox.getSelectedItem())) {
            return "id";
        }
        // 其余情况按"药品名称"处理
        return "name";
    }

    // 按时间范围标识加载销售记录：统一走 Service 的"范围 + 类型"组合查询入口（不按关键词）
    private void loadCategory(String key) {
        // 记住当前时间范围：新增销售成功后按它刷新，保持用户所在视角（custom 时区间已由确定按钮记录）
        currentKey = key;
        // 无关键词：只按时间范围过滤
        displayList = saleService.searchSales(key, "none", "", customStart, customEnd);
        // 数据变化后回到第 1 页：切换时间范围视为重新浏览
        pageBar.setTotal(displayList.size());
        // 加载完成刷新表格
        refreshTable();
    }

    // 重新装载当前时间范围数据并保持当前页码：新增销售成功后调用，避免页码跳回第 1 页
    private void reloadKeepPage() {
        // 装载当前时间范围的数据（自定义区间已记录在 customStart / customEnd）
        displayList = saleService.searchSales(currentKey, "none", "", customStart, customEnd);
        // 更新总条数并保持当前页（当前页越界时收敛到最后一页）
        pageBar.setTotalKeepPage(displayList.size());
        refreshTable();
    }

    // 按下拉框选中的类型执行查询，查询限制在当前时间范围内；关键词为空时恢复当前范围完整列表
    private void doSearch() {
        // 取出关键词并去掉首尾空格
        String keyword = searchField.getText().trim();
        // 查询为空时不执行查询，恢复当前时间范围的完整列表
        if (keyword.isEmpty()) {
            loadCategory(currentKey);
            return;
        }
        // 按当前时间范围 + 查询类型组合查询（如"今日销售"+药品名称 = 只查今日内名称匹配的记录）
        displayList = saleService.searchSales(currentKey, typeKey(), keyword, customStart, customEnd);
        // 搜索结果视为重新浏览：更新总条数并回到第 1 页
        pageBar.setTotal(displayList.size());
        // 查询完成刷新表格
        refreshTable();
    }

    // 选"自定义"时弹出日期输入弹窗：点确定校验并按区间查询，点取消恢复下拉框到原范围
    private void doCustomRange() {
        // 模态弹窗：阻塞到确定或取消
        SaleDate dialog = new SaleDate(SwingUtilities.getWindowAncestor(SalePanel.this));
        dialog.setVisible(true);
        // 取消（开始日期为 null）：没有新区间，把下拉框恢复到原来的范围，保持显示与实际一致
        if (dialog.getStartDate() == null) {
            // 用标志恢复：避免恢复动作再次触发监听器（当前已是自定义视角时会无限重新弹窗）
            restoringRange = true;
            rangeBox.setSelectedItem(keyToRangeText(currentKey));
            return;
        }
        // 确定：记住区间并把当前范围切到自定义，之后的关键词查询与刷新都按它过滤
        customStart = dialog.getStartDate();
        customEnd = dialog.getEndDate();
        currentKey = "custom";
        // 按自定义区间 + 当前查询类型/关键词立即查询
        displayList = saleService.searchSales("custom", typeKey(), searchField.getText().trim(), customStart, customEnd);
        // 自定义区间视为重新浏览：更新总条数并回到第 1 页
        pageBar.setTotal(displayList.size());
        refreshTable();
    }

    // 把时间范围标识转回下拉框选项文字：取消自定义后恢复选中项用
    private String keyToRangeText(String key) {
        if ("today".equals(key)) {
            return "今日销售";
        }
        if ("week".equals(key)) {
            return "本周销售";
        }
        if ("month".equals(key)) {
            return "本月销售";
        }
        if ("custom".equals(key)) {
            return "自定义";
        }
        // 其余情况对应"全部销售"
        return "全部销售";
    }

    // 用 displayList 当前页的数据刷新表格行；销售时间格式化为 yyyy-MM-dd HH:mm:ss 文本
    private void refreshTable() {
        // 清空旧行后逐行填充
        tableModel.setRowCount(0);
        // 销售时间显示格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // displayList 在构造方法 loadCategory 后一定有值，判空只是防御极端时序
        if (displayList == null) {
            return;
        }
        // 计算当前页的数据区间：[页首下标, min(页尾下标, 总条数))
        int from = (pageBar.getCurrentPage() - 1) * pageBar.getPageSize();
        int to = Math.min(from + pageBar.getPageSize(), displayList.size());
        // 只填充当前页区间内的销售记录
        for (int i = from; i < to; i++) {
            Sale sale = displayList.get(i);
            tableModel.addRow(new Object[]{
                    sale.getId(),
                    // 药品名称、类别、规格、厂家、操作员姓名由 DAO 联表直接带出，无需在界面二次查询
                    sale.getMedicineName(),
                    sale.getCategory(),
                    sale.getSpecification(),
                    sale.getManufacturer(),
                    sale.getOperatorName(),
                    sale.getQuantity(),
                    sale.getSalePrice(),
                    sale.getTotalAmount(),
                    sale.getSaleTime() == null ? "" : sale.getSaleTime().format(formatter),
                    sale.getRemark()});
        }
    }

    // 初始化表格外观：行高、网格线、选中底色与表头样式，与药品管理页保持一致
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
        // 列宽：编号/数量/金额列内容固定收紧，名称/规格/厂家/时间/备注列留余量
        table.getColumnModel().getColumn(0).setPreferredWidth(75); // 【可修改参数】销售编号列宽
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // 【可修改参数】药品名称列宽（药名长短不定，留余量）
        table.getColumnModel().getColumn(2).setPreferredWidth(85); // 【可修改参数】药品类别列宽
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // 【可修改参数】规格列宽（如"0.25g*24粒/盒"）
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // 【可修改参数】生产厂家列宽（厂名长短不定，留余量）
        table.getColumnModel().getColumn(5).setPreferredWidth(85); // 【可修改参数】操作员列宽
        table.getColumnModel().getColumn(6).setPreferredWidth(55); // 【可修改参数】数量列宽
        table.getColumnModel().getColumn(7).setPreferredWidth(70); // 【可修改参数】单价列宽
        table.getColumnModel().getColumn(8).setPreferredWidth(80); // 【可修改参数】总价列宽
        table.getColumnModel().getColumn(9).setPreferredWidth(145); // 【可修改参数】销售时间列宽（固定 19 字符时间文本）
        table.getColumnModel().getColumn(10).setPreferredWidth(110); // 【可修改参数】备注列宽（内容长短不定，留余量）
    }

    // 创建右侧列表区：顶部工具栏 + 销售记录表格
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG);
        // 顶部工具栏
        panel.add(createToolBar(), BorderLayout.NORTH);

        // 表格滚动容器：数据多时可上下滚动，列多时可横向滚动
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

    // 创建顶部工具栏：左侧查询类型下拉框 + 关键词输入框 + 查询按钮，右侧新增销售按钮
    private JPanel createToolBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // 【可修改参数】工具栏四周留白（上 12 / 左右 16 / 下 12）

        // 左侧：查询控件，水平排列、垂直居中
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // 【可修改参数】查询控件水平间距（8）
        left.setBackground(UiTheme.BG);
        // 时间范围下拉框注册监听：选"自定义"弹出日期输入弹窗，选其他范围立即刷新
        rangeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 恢复动作触发的监听调用：跳过，不当成用户选择
                if (restoringRange) {
                    restoringRange = false;
                    return;
                }
                String rangeText = (String) rangeBox.getSelectedItem();
                // 选"自定义"：弹出开始/结束日期输入弹窗，点确定后才按区间查询
                if ("自定义".equals(rangeText)) {
                    doCustomRange();
                    return;
                }
                // 选其他范围：立即按对应范围刷新
                loadCategory(rangeTextToKey(rangeText));
            }
        });
        left.add(rangeBox);
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

        // 右侧：新增销售按钮 → 打开新增销售对话框，关闭后刷新当前时间范围
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 传入当前登录用户：对话框保存时把它作为销售记录的操作员
                new SaleDialog(SwingUtilities.getWindowAncestor(SalePanel.this), currentUser).setVisible(true);
                // 关闭后保持当前页刷新：新销售记录按编号排在末页，可通过页码按钮跳转查看
                reloadKeepPage();
            }
        });
        panel.add(addButton, BorderLayout.EAST);
        return panel;
    }
}
