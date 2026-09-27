package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 中表单 / 底按钮 两段组织
import java.awt.BorderLayout;
// 导入 Dimension：按钮尺寸
import java.awt.Dimension;
// 导入 FlowLayout：底部按钮区居中排列
import java.awt.FlowLayout;
// 导入 GridBagConstraints：配合 GridBagLayout 控制表单行位置与对齐
import java.awt.GridBagConstraints;
// 导入 GridBagLayout：表单两列布局，左标签右输入框
import java.awt.GridBagLayout;
// 导入 Insets：表单行间距
import java.awt.Insets;
// 导入 Window：构造方法接收的父窗口类型（主窗口等顶层窗口都适用）
import java.awt.Window;
// 导入 BigDecimal：价格字段类型，与 Medicine 实体保持一致
import java.math.BigDecimal;
// 导入 LocalDate：日期字段类型，与 Medicine 实体保持一致
import java.time.LocalDate;
// 导入 DateTimeParseException：日期解析失败时给出友好提示
import java.time.format.DateTimeParseException;
// 导入 ActionEvent：按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为保存、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：表单区留白
import javax.swing.BorderFactory;
// 导入 JButton：保存与取消按钮
import javax.swing.JButton;
// 导入 JComboBox：状态下拉框
import javax.swing.JComboBox;
// 导入 JDialog：对话框基类，承载新增 / 修改药品的表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JTextField：各文本输入框
import javax.swing.JTextField;
// 导入 Medicine：表单操作的药品实体类型
import com.GM.medicine.pojo.entity.Medicine;
// 导入 MedicineService：保存统一通过它完成，本类不写 SQL
import com.GM.medicine.service.MedicineService;

/**
 * - 药品编辑对话框
 * - 同时支持新增与修改：medicine 为 null 表示新增，非 null 表示修改并回填数据
 * - 只做基础输入校验（必填、数字格式、非负、日期格式），业务规则（如有效期不早于今天）由 MedicineService 校验
 * - 保存统一通过 MedicineService.addMedicine / updateMedicine 完成
 */
public class MedicineDialog extends JDialog {

    // 药品业务对象：保存统一通过它完成
    private MedicineService medicineService = new MedicineService();

    // 待修改的药品对象：null 表示新增模式
    private Medicine medicine;

    // 药品名称输入框
    private JTextField nameField;

    // 分类输入框
    private JTextField categoryField;

    // 规格输入框
    private JTextField specificationField;

    // 生产厂家输入框
    private JTextField manufacturerField;

    // 生产批号输入框
    private JTextField batchNumberField;

    // 进货价格输入框（数字）
    private JTextField purchasePriceField;

    // 销售价格输入框（数字）
    private JTextField salePriceField;

    // 库存数量输入框（整数）
    private JTextField stockField;

    // 预警库存输入框（整数）
    private JTextField warningStockField;

    // 生产日期输入框（yyyy-MM-dd）
    private JTextField productionDateField;

    // 有效期输入框（yyyy-MM-dd）
    private JTextField expiryDateField;

    // 状态下拉框：0 号位"正常"对应 status=1，1 号位"停售"对应 status=0；扁平风格由 UiTheme 工厂统一
    private JComboBox<String> statusBox = UiTheme.createComboBox(new String[]{"正常", "停售"});

    // 保存按钮：主题绿圆角+描边，与登录按钮同款
    private JButton saveButton = UiTheme.createRoundButton("保存", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：白色圆角+描边，与登录界面"退出"按钮同款
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.WHITE, UiTheme.TEXT_DARK);

    /**
     * 构造药品编辑对话框
     *
     * @param owner    父窗口，对话框在其上居中
     * @param medicine 待修改的药品对象；传 null 表示新增
     */
    public MedicineDialog(Window owner, Medicine medicine) {
        super(owner);
        // 保存模式标记：新增 / 修改共用一个表单
        this.medicine = medicine;
        // 模态：保存或取消前不能操作主窗口
        setModal(true);
        // 按模式区分标题
        setTitle(medicine == null ? "新增药品" : "修改药品");
        // 组装表单与按钮
        initComponents();
        setSize(480, 640); // 可修改参数：对话框初始大小（宽 480 / 高 640）
        // 在父窗口上方居中
        setLocationRelativeTo(owner);
        // 修改模式时回填已有数据
        fillFields();
    }

    // 组装表单区与底部按钮区
    private void initComponents() {
        // 上下两段布局：中间表单、底部按钮
        setLayout(new BorderLayout());
        // 对话框整体白底，风格与登录、主窗口一致
        getContentPane().setBackground(UiTheme.WHITE);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // 创建表单区：两列布局（左标签右输入框），字段顺序与 Medicine 实体一致
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiTheme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24)); // 可修改参数：表单区四周留白

        // 各输入框统一创建：圆角焦点边框与登录界面一致
        nameField = createInputField();
        categoryField = createInputField();
        specificationField = createInputField();
        manufacturerField = createInputField();
        batchNumberField = createInputField();
        purchasePriceField = createInputField();
        salePriceField = createInputField();
        stockField = createInputField();
        warningStockField = createInputField();
        productionDateField = createInputField();
        expiryDateField = createInputField();

        // 约束对象：两行共用，addRow 内部按行推进
        GridBagConstraints gbc = new GridBagConstraints();
        // 第一行行号固定为 0：gridy 默认是 RELATIVE(-1)，不初始化会让前两行叠在同一行
        gbc.gridy = 0;
        // 行间距：上下各 8 像素
        gbc.insets = new Insets(8, 0, 8, 0); // 可修改参数：表单行间距（8）
        // 标签列左对齐
        gbc.anchor = GridBagConstraints.WEST;

        // 逐行添加表单项
        addRow(panel, gbc, "药品名称：", nameField);
        addRow(panel, gbc, "药品分类：", categoryField);
        addRow(panel, gbc, "规格：", specificationField);
        addRow(panel, gbc, "生产厂家：", manufacturerField);
        addRow(panel, gbc, "生产批号：", batchNumberField);
        addRow(panel, gbc, "进货价格：", purchasePriceField);
        addRow(panel, gbc, "销售价格：", salePriceField);
        addRow(panel, gbc, "库存数量：", stockField);
        addRow(panel, gbc, "预警库存：", warningStockField);
        addRow(panel, gbc, "生产日期：", productionDateField);
        addRow(panel, gbc, "有效期至：", expiryDateField);
        addRow(panel, gbc, "药品状态：", statusBox);

        return panel;
    }

    // 添加一行表单项：第 0 列放标签，第 1 列放输入组件（横向撑满），行号自动下移
    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        // 左列：字段标签，宽度由内容决定
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(UiTheme.createLabel(labelText, UiTheme.TEXT_DARK), gbc);
        // 右列：输入组件横向撑满剩余宽度
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        // 行号下移一行
        gbc.gridy++;
    }

    // 创建统一风格的输入框：静态浅灰圆角边框，与登录界面一致
    private JTextField createInputField() {
        JTextField field = UiTheme.createTextField(0);
        // 静态圆角边框：浅灰 1px，复用 UiTheme 绘制（焦点变色效果已取消）
        field.setOpaque(false);
        field.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        return field;
    }

    // 创建底部按钮区：保存（主题绿）+ 取消（浅灰）
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10)); // 可修改参数：按钮水平间距 16 / 上下留白 10
        panel.setBackground(UiTheme.WHITE);
        // 统一按钮尺寸
        saveButton.setPreferredSize(new Dimension(90, 32)); // 可修改参数：保存按钮大小（宽 90 / 高 32）
        cancelButton.setPreferredSize(new Dimension(90, 32)); // 可修改参数：取消按钮大小（宽 90 / 高 32）
        panel.add(saveButton);
        panel.add(cancelButton);

        // 保存：校验并提交
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        // 取消：直接关闭对话框，不做保存
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        return panel;
    }

    // 修改模式回填：把药品已有数据填入表单；新增模式不做任何事
    private void fillFields() {
        if (medicine == null) {
            return;
        }
        nameField.setText(medicine.getName());
        categoryField.setText(medicine.getCategory());
        specificationField.setText(medicine.getSpecification());
        manufacturerField.setText(medicine.getManufacturer());
        batchNumberField.setText(medicine.getBatchNumber());
        purchasePriceField.setText(medicine.getPurchasePrice() == null ? "" : medicine.getPurchasePrice().toPlainString());
        salePriceField.setText(medicine.getSalePrice() == null ? "" : medicine.getSalePrice().toPlainString());
        stockField.setText(medicine.getStock() == null ? "" : String.valueOf(medicine.getStock()));
        warningStockField.setText(medicine.getWarningStock() == null ? "" : String.valueOf(medicine.getWarningStock()));
        // LocalDate.toString() 本身就是 yyyy-MM-dd 格式，可直接回填
        productionDateField.setText(medicine.getProductionDate() == null ? "" : medicine.getProductionDate().toString());
        expiryDateField.setText(medicine.getExpiryDate() == null ? "" : medicine.getExpiryDate().toString());
        // 状态映射：1 = 正常（下拉框 0 号位）
        statusBox.setSelectedIndex(medicine.getStatus() != null && medicine.getStatus() == 1 ? 0 : 1);
    }

    /**
     * 保存：基础输入校验 → 组装 Medicine → 交给 Service 新增 / 更新
     * 校验失败弹提示并留在对话框；成功后关闭对话框，由面板刷新列表
     */
    private void doSave() {
        // 逐项取出输入并去掉首尾空格
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String specification = specificationField.getText().trim();
        String manufacturer = manufacturerField.getText().trim();
        String batchNumber = batchNumberField.getText().trim();
        String purchasePriceText = purchasePriceField.getText().trim();
        String salePriceText = salePriceField.getText().trim();
        String stockText = stockField.getText().trim();
        String warningStockText = warningStockField.getText().trim();
        String productionDateText = productionDateField.getText().trim();
        String expiryDateText = expiryDateField.getText().trim();

        // ===== 基础校验：必填项 =====
        if (name.isEmpty()) {
            showMessage("药品名称不能为空");
            return;
        }
        if (category.isEmpty()) {
            showMessage("药品分类不能为空");
            return;
        }
        if (specification.isEmpty()) {
            showMessage("规格不能为空");
            return;
        }
        if (manufacturer.isEmpty()) {
            showMessage("生产厂家不能为空");
            return;
        }
        if (batchNumber.isEmpty()) {
            showMessage("生产批号不能为空");
            return;
        }

        // ===== 基础校验：价格必须是大于 0 的数字 =====
        BigDecimal purchasePrice;
        try {
            purchasePrice = new BigDecimal(purchasePriceText);
        } catch (NumberFormatException e) {
            showMessage("进货价格必须为数字");
            return;
        }
        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            showMessage("进货价格必须大于 0");
            return;
        }
        BigDecimal salePrice;
        try {
            salePrice = new BigDecimal(salePriceText);
        } catch (NumberFormatException e) {
            showMessage("销售价格必须为数字");
            return;
        }
        if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            showMessage("销售价格必须大于 0");
            return;
        }

        // ===== 基础校验：库存必须是不为负的整数（Service 里会直接拆箱，空值会导致空指针） =====
        int stock;
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            showMessage("库存数量必须为整数");
            return;
        }
        if (stock < 0) {
            showMessage("库存数量不能为负数");
            return;
        }
        int warningStock;
        try {
            warningStock = Integer.parseInt(warningStockText);
        } catch (NumberFormatException e) {
            showMessage("预警库存必须为整数");
            return;
        }
        if (warningStock < 0) {
            showMessage("预警库存不能为负数");
            return;
        }

        // ===== 基础校验：日期必须是 yyyy-MM-dd 格式 =====
        LocalDate productionDate;
        try {
            productionDate = LocalDate.parse(productionDateText);
        } catch (DateTimeParseException e) {
            showMessage("生产日期格式应为 yyyy-MM-dd，例如 2026-01-01");
            return;
        }
        LocalDate expiryDate;
        try {
            expiryDate = LocalDate.parse(expiryDateText);
        } catch (DateTimeParseException e) {
            showMessage("有效期格式应为 yyyy-MM-dd，例如 2027-01-01");
            return;
        }

        // ===== 组装药品对象：修改模式复用原对象（保留 id），新增模式创建新对象 =====
        Medicine target = (medicine == null) ? new Medicine() : medicine;
        target.setName(name);
        target.setCategory(category);
        target.setSpecification(specification);
        target.setManufacturer(manufacturer);
        target.setBatchNumber(batchNumber);
        target.setPurchasePrice(purchasePrice);
        target.setSalePrice(salePrice);
        target.setStock(stock);
        target.setWarningStock(warningStock);
        target.setProductionDate(productionDate);
        target.setExpiryDate(expiryDate);
        // 下拉框 0 号位"正常"对应 status=1
        target.setStatus(statusBox.getSelectedIndex() == 0 ? 1 : 0);

        // ===== 交给 Service：新增或更新，业务规则由 Service 校验 =====
        boolean success = (medicine == null)
                ? medicineService.addMedicine(target)
                : medicineService.updateMedicine(target);
        if (success) {
            showMessage(medicine == null ? "新增药品成功" : "修改药品成功");
            // 关闭对话框，由面板刷新列表
            dispose();
        } else {
            // Service 只返回成功与否；失败常见原因是有效期早于今天等业务规则
            showMessage("保存失败：请检查有效期是否早于今天等输入项");
        }
    }

    // 统一弹提示：复用主题风格的提示框
    private void showMessage(String message) {
        UiTheme.showMessageDialog(this, message);
    }
}
