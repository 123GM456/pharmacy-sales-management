package com.GM.medicine.ui;

// 导入 BorderLayout：对话框按 中表单 / 底按钮 两段组织
import java.awt.BorderLayout;
// 导入 Dimension：确定按钮尺寸
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
// 导入 LocalDate：自定义时间范围的开始/结束日期类型
import java.time.LocalDate;
// 导入 DateTimeParseException：日期解析失败时给出友好提示
import java.time.format.DateTimeParseException;
// 导入 ActionEvent：确定按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为确定、取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 JButton：确定与取消按钮
import javax.swing.JButton;
// 导入 JDialog：对话框基类，承载自定义时间范围表单
import javax.swing.JDialog;
// 导入 JPanel：承载表单与按钮的容器
import javax.swing.JPanel;
// 导入 JTextField：开始/结束日期输入框
import javax.swing.JTextField;

/**
 * - 自定义时间范围对话框（SaleDate）：供销售管理输入自定义查询区间
 * - 开始日期必填；结束日期选填，不填按开始当天查询；格式均为 yyyy-MM-dd
 * - 点“确定”校验通过后记录区间并关闭；点“取消”直接关闭，调用方拿到的开始日期为 null
 */
public class SaleDate extends JDialog {

    // 自定义开始日期输入框：格式 yyyy-MM-dd
    private JTextField startField = UiTheme.createTextField(10); // 【可修改参数】开始日期框推荐列数（10）

    // 自定义结束日期输入框：选填，不填按开始当天查询
    private JTextField endField = UiTheme.createTextField(10); // 【可修改参数】结束日期框推荐列数（10）

    // 确定按钮：校验通过后记录区间并关闭，登录类配色（主动作）
    private JButton okButton = UiTheme.createRoundButton("确定", UiTheme.PRIMARY, UiTheme.WHITE);

    // 取消按钮：放弃输入直接关闭，退出类配色
    private JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.BG, UiTheme.TEXT_DARK);

    // 校验通过后的开始日期：未点确定前为 null
    private LocalDate startDate;

    // 校验通过后的结束日期：不填保持 null（表示按开始当天查询）
    private LocalDate endDate;

    /**
     * 构造自定义时间范围对话框
     *
     * @param owner 父窗口，对话框在其上居中
     */
    public SaleDate(Window owner) {
        super(owner);
        // 模态：确定或取消前不能操作主窗口
        setModal(true);
        // 标题与用途一致
        setTitle("自定义时间");
        // 组装表单与按钮
        initComponents();
        setSize(280, 190); // 【可修改参数】对话框大小（宽 280 / 高 190）
        // 在父窗口上居中
        setLocationRelativeTo(owner);
    }

    /**
     * 获取校验通过后的开始日期
     *
     * @return 开始日期；用户取消或未点确定时为 null
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * 获取校验通过后的结束日期
     *
     * @return 结束日期；未填写或取消时为 null（null 表示按开始当天查询）
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    // 组装中部表单与底部按钮
    private void initComponents() {
        // 中部：两行表单（开始日期 / 结束日期），白底与对话框统一
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiTheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        // 行间距：标签与输入框之间留白
        gbc.insets = new Insets(8, 0, 8, 6); // 【可修改参数】表单行间距（上下 8 / 标签与框间 6）
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("开始日期：", UiTheme.TEXT_DARK), gbc);
        // 输入框列：不拉满宽度，按列数自然宽度显示（约 100px，够输 10 字符日期）
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        startField.setOpaque(false);
        startField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(startField, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(UiTheme.createLabel("结束日期：", UiTheme.TEXT_DARK), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        endField.setOpaque(false);
        endField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        formPanel.add(endField, gbc);
        add(formPanel, BorderLayout.CENTER);

        // 底部：确定 + 取消按钮行，居中
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12)); // 【可修改参数】按钮区间距（12）
        buttonPanel.setBackground(UiTheme.WHITE);
        // 确定按钮：登录类配色（提交区间是主动作），点击校验日期
        okButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】确定按钮大小（宽 90 / 高 32）
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doConfirm();
            }
        });
        buttonPanel.add(okButton);
        // 取消按钮：退出类配色，直接关闭不返回区间
        cancelButton.setPreferredSize(new Dimension(90, 32)); // 【可修改参数】取消按钮大小（宽 90 / 高 32）
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // “确定”：校验日期输入，全部通过后记录区间并关闭；任何一步失败弹提示且不关闭
    private void doConfirm() {
        // 开始日期必填：为空直接提示
        String startText = startField.getText().trim();
        if (startText.isEmpty()) {
            UiTheme.showMessageDialog(this, "请选择开始日期");
            return;
        }
        // 解析开始日期：LocalDate.parse 默认就是 yyyy-MM-dd 格式，解析失败给友好提示
        LocalDate start;
        try {
            start = LocalDate.parse(startText);
        } catch (DateTimeParseException e) {
            UiTheme.showMessageDialog(this, "开始日期格式应为 yyyy-MM-dd");
            return;
        }
        // 结束日期选填：不填按开始当天查询（endDate 保持 null）
        String endText = endField.getText().trim();
        LocalDate end = null;
        if (!endText.isEmpty()) {
            try {
                end = LocalDate.parse(endText);
            } catch (DateTimeParseException e) {
                UiTheme.showMessageDialog(this, "结束日期格式应为 yyyy-MM-dd");
                return;
            }
            // 开始晚于结束没有意义：直接拦截
            if (start.isAfter(end)) {
                UiTheme.showMessageDialog(this, "开始日期不能晚于结束日期");
                return;
            }
        }
        // 全部校验通过：记录区间并关闭，调用方用 getStartDate / getEndDate 取值
        startDate = start;
        endDate = end;
        dispose();
    }
}
