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
// 导入 BigDecimal：总价 = 数量 × 单价 需要精确十进制计算，避免浮点误差
import java.math.BigDecimal;
// 导入 ActionEvent：按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为保存、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 DocumentEvent：数量输入框内容变化的事件参数类型
import javax.swing.event.DocumentEvent;
// 导入 DocumentListener：监听数量输入变化实时重算总价
import javax.swing.event.DocumentListener;
// 导入 BorderFactory：表单区留白
import javax.swing.BorderFactory;
// 导入 JButton：保存、取消、选择药品按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，承载新增销售的表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JTextField：已选药品、数量、单价、总价、备注输入框
import javax.swing.JTextField;
// 导入 Medicine：选中药品实体（取编号、售价、名称、规格与厂家用于展示）
import com.GM.medicine.pojo.entity.Medicine;
// 导入 SysUser：当前登录用户，保存时作为销售记录的操作员
import com.GM.medicine.pojo.entity.SysUser;
// 导入 SaleDTO：新增销售的数据传输对象
import com.GM.medicine.pojo.dto.SaleDTO;
// 导入 SaleService：保存统一通过它完成，本类不写 SQL
import com.GM.medicine.service.SaleService;

/**
 * - 新增销售对话框
 * - 药品通过弹窗选择：点"选择药品"打开 MedicineSelectDialog，选完后只读文本框显示"名称 规格 厂家"并自动带出售价
 * - 输入数量后自动计算总价（不可修改）；保存时只提交药品编号，药品名称不参与保存
 * - 只做基础输入校验（必选、数量正整数），业务规则（库存、状态、总价）由 SaleService 处理
 * - 保存统一通过 SaleService.addSale 完成，操作员就是构造方法传入的当前登录用户
 */
public class SaleDialog extends JDialog {

    // 销售业务对象：保存统一通过它调用
    private SaleService saleService = new SaleService();

    // 当前登录用户：保存时作为销售记录的操作员
    private SysUser operator;

    // 当前选中的药品：由 MedicineSelectDialog 弹窗选完回填，保存时只取它的编号
    private Medicine selectedMedicine;

    // 已选药品展示框：只读，显示"名称 规格 厂家"，初始为空
    private JTextField selectedField = createReadonlyField();

    // 选择药品按钮：点击弹出药品选择对话框
    private JButton selectButton = UiTheme.createRoundButton("选择药品", UiTheme.PRIMARY, UiTheme.WHITE);

    // 数量输入框
    private JTextField quantityField = createInputField();

    // 单价显示框：选中药品后自动带出该药品售价，不可修改
    private JTextField priceField = createReadonlyField();

    // 总价显示框：数量 × 单价自动计算，不可修改
    private JTextField totalField = createReadonlyField();

    // 备注输入框
    private JTextField remarkField = createInputField();

    // 保存按钮：主题绿圆角+描边，与药品编辑对话框一致
    private JButton saveButton = UiTheme.createRoundButton("保存", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：浅灰圆角+描边，与药品编辑对话框一致
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    /**
     * 构造新增销售对话框
     *
     * @param owner    父窗口，对话框在其上居中
     * @param operator 当前登录用户，保存时作为销售记录的操作员
     */
    public SaleDialog(Window owner, SysUser operator) {
        super(owner);
        // 保存操作员身份：提交销售记录时传给 Service
        this.operator = operator;
        // 模态：保存或取消前不能操作主窗口
        setModal(true);
        setTitle("新增销售");
        // 组装表单与按钮
        initComponents();
        setSize(430, 320); // 【可修改参数】对话框初始大小（宽 430 / 高 320）
        // 在父窗口上方居中
        setLocationRelativeTo(owner);
        // 注册联动监听：数量变化实时重算总价
        registerListeners();
    }

    // 组装表单区与底部按钮区
    private void initComponents() {
        // 上下两段布局：中间表单、底部按钮
        setLayout(new BorderLayout());
        // 对话框整体白底，风格与药品编辑对话框一致
        getContentPane().setBackground(UiTheme.WHITE);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // 创建表单区：两列布局（左标签右输入框）
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiTheme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24)); // 【可修改参数】表单区四周留白

        // 约束对象：各行共用，addRow 内部按行推进
        GridBagConstraints gbc = new GridBagConstraints();
        // 第一行行号固定为 0：gridy 默认是 RELATIVE(-1)，不初始化会让前两行叠在同一行
        gbc.gridy = 0;
        // 行间距：上下各 8 像素
        gbc.insets = new Insets(8, 0, 8, 0); // 【可修改参数】表单行间距（8）
        // 标签列左对齐
        gbc.anchor = GridBagConstraints.WEST;

        // 逐行添加表单项
        addRow(panel, gbc, "药品：", createMedicineRow());
        addRow(panel, gbc, "数量：", quantityField);
        addRow(panel, gbc, "单价：", priceField);
        addRow(panel, gbc, "总价：", totalField);
        addRow(panel, gbc, "备注：", remarkField);

        return panel;
    }

    // 创建药品行：左侧已选药品只读展示框 + 右侧"选择药品"按钮，点按钮弹窗选择
    private JPanel createMedicineRow() {
        // 横向排列：展示框撑满剩余宽度，按钮固定尺寸
        JPanel row = new JPanel(new BorderLayout(8, 0)); // 【可修改参数】展示框与按钮的间距（8）
        row.setBackground(UiTheme.WHITE);
        // 已选药品展示框：初始为空，选完后显示"名称 规格 厂家"
        selectedField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        row.add(selectedField, BorderLayout.CENTER);
        // 选择药品按钮：收紧尺寸适配表单行高，点击弹出选择对话框
        selectButton.setPreferredSize(new Dimension(100, 26)); // 【可修改参数】选择药品按钮大小（宽 100 / 高 26）
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSelectMedicine();
            }
        });
        row.add(selectButton, BorderLayout.EAST);
        return row;
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

    // 创建统一风格的输入框：静态浅灰圆角边框，与药品编辑对话框一致
    private JTextField createInputField() {
        JTextField field = UiTheme.createTextField(0);
        // 静态圆角边框：浅灰 1px，复用 UiTheme 绘制
        field.setOpaque(false);
        field.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        return field;
    }

    // 创建只读展示框：外观与输入框一致但禁止编辑，用于自动带出的单价与计算出的总价
    private JTextField createReadonlyField() {
        JTextField field = createInputField();
        // 禁止编辑：只作为自动带出与计算结果的展示
        field.setEditable(false);
        return field;
    }

    // 创建底部按钮区：保存（主题绿）+ 取消（浅灰）
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10)); // 【可修改参数】按钮水平间距 16 / 上下留白 10
        panel.setBackground(UiTheme.WHITE);
        // 统一按钮尺寸
        saveButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】保存按钮大小（宽 90 / 高 32）
        cancelButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】取消按钮大小（宽 90 / 高 32）
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

    // 注册联动监听：数量变化实时重算总价（选择药品按钮的监听在 createMedicineRow 里注册）
    private void registerListeners() {
        // 数量输入变化（增删改都覆盖）：实时重算总价
        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateTotal();
            }

            public void removeUpdate(DocumentEvent e) {
                updateTotal();
            }

            public void changedUpdate(DocumentEvent e) {
                updateTotal();
            }
        });
    }

    // 弹出药品选择对话框：选完后回填展示框并带出售价、重算总价；取消时不改变当前选择
    private void doSelectMedicine() {
        // 模态弹窗：setVisible(true) 会阻塞到选择对话框关闭
        MedicineSelectDialog dialog = new MedicineSelectDialog(this);
        dialog.setVisible(true);
        // 取回选中的药品；点取消时为 null，保持原选择不变
        Medicine medicine = dialog.getSelectedMedicine();
        if (medicine == null) {
            return;
        }
        // 记录选中的药品：保存时只取它的编号
        selectedMedicine = medicine;
        // 展示框回填："名称 规格 厂家"，避免同名不同规格/厂家的药品混淆
        selectedField.setText(medicine.getName() + " " + medicine.getSpecification() + " " + medicine.getManufacturer());
        // 带出售价并重算总价
        updatePrice();
        updateTotal();
    }

    // 把选中药品的售价带出到单价框；未选药品时清空
    private void updatePrice() {
        Medicine medicine = selectedMedicine;
        priceField.setText(medicine == null ? "" : medicine.getSalePrice().toPlainString());
    }

    // 取当前选中的药品：由弹窗选择后记录
    private Medicine getSelectedMedicine() {
        return selectedMedicine;
    }

    // 总价 = 数量 × 单价，自动计算；数量非法或药品未选时清空总价框等待继续输入
    private void updateTotal() {
        Medicine medicine = selectedMedicine;
        if (medicine == null) {
            totalField.setText("");
            return;
        }
        try {
            // 数量必须是正整数才计算，输入中途（如空串、负数）不显示总价
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                totalField.setText("");
                return;
            }
            // 与 Service 相同的算法：单价 × 数量，保证显示与入库金额一致
            BigDecimal total = medicine.getSalePrice().multiply(BigDecimal.valueOf(quantity));
            totalField.setText(total.toPlainString());
        } catch (NumberFormatException e) {
            // 输入还不是合法整数：清空总价等待继续输入
            totalField.setText("");
        }
    }

    /**
     * 保存销售记录：做必选与数字格式的基础校验，业务规则（库存、状态、金额计算）交给 SaleService
     */
    private void doSave() {
        // 药品必须选中：无可售药品或未选中时无法确定销售内容
        Medicine medicine = getSelectedMedicine();
        if (medicine == null) {
            UiTheme.showMessageDialog(this, "请选择药品");
            return;
        }
        // 数量必须是正整数
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            UiTheme.showMessageDialog(this, "销售数量必须是正整数");
            return;
        }
        if (quantity <= 0) {
            UiTheme.showMessageDialog(this, "销售数量必须大于 0");
            return;
        }
        // 组装 DTO：药品编号、数量、备注；单价与总价由 Service 按药品现价统一计算并写入
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setMedicineId(medicine.getId());
        saleDTO.setQuantity(quantity);
        saleDTO.setRemark(remarkField.getText().trim());
        // 操作员就是当前登录用户，库存、状态等业务校验由 Service 完成
        boolean success = saleService.addSale(saleDTO, operator.getId());
        if (success) {
            UiTheme.showMessageDialog(this, "销售记录保存成功");
            // 关闭对话框，SalePanel 会在对话框关闭后刷新列表
            dispose();
        } else {
            // Service 校验未通过（库存不足、药品不可售等），提示统一文案
            UiTheme.showMessageDialog(this, "保存失败：库存不足或药品当前不可售");
        }
    }
}
