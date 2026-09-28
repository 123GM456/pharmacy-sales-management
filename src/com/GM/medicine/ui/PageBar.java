package com.GM.medicine.ui;

// 导入 BorderLayout：跳页窗口内容区按 正文 / 按钮行 上下组织
import java.awt.BorderLayout;
// 导入 Dimension：固定上一页 / 页码 / 下一页按钮的尺寸
import java.awt.Dimension;
// 导入 GridLayout：跳页窗口正文里提示文字与输入框上下排列
import java.awt.GridLayout;
// 导入 FlowLayout：分页控件在底部水平居中排列，跳页窗口按钮行也用它
import java.awt.FlowLayout;
// 导入 ActionEvent：分页按钮点击事件的方法参数类型
import java.awt.event.ActionEvent;
// 导入 ActionListener：为上一页 / 页码 / 下一页 / 确定 / 取消按钮注册点击监听的接口
import java.awt.event.ActionListener;
// 导入 BorderFactory：分页栏四周留白与跳页窗口正文留白
import javax.swing.BorderFactory;
// 导入 JButton：上一页、页码跳转、下一页按钮
import javax.swing.JButton;
// 导入 JDialog：跳页窗口本体，复用系统标题栏
import javax.swing.JDialog;
// 导入 JLabel：总数据条数标签与跳页窗口提示文字
import javax.swing.JLabel;
// 导入 JPanel：本组件的容器基类与跳页窗口内容面板
import javax.swing.JPanel;
// 导入 JTextField：跳页窗口的页码输入框
import javax.swing.JTextField;

/**
 * - 表格底部分页栏
 * - 按"上一页 / 当前页数/总页数 / 下一页 / 总数据条数"四部分显示，每页条数由持有面板构造时指定
 * - 数据加载与表格渲染由持有面板完成：本类页码变化后通过回调通知持有面板重绘当前页
 * - 点击"当前页数/总页数"按钮可输入目标页码跳转；首末页自动置灰上一页 / 下一页按钮
 */
public class PageBar extends JPanel {

    // 每页显示的数据条数：构造时由持有面板指定
    private final int pageSize;

    // 页码变化回调：通知持有面板按新页码重新渲染表格当前页
    private final Runnable pageChangeListener;

    // 当前页码：从 1 开始
    private int currentPage = 1;

    // 数据总条数：持有面板每次加载数据后通过 setTotal / setTotalKeepPage 更新
    private int totalCount = 0;

    // 上一页按钮：第 1 页时置灰禁用
    private final JButton prevButton = UiTheme.createRoundButton("上一页", UiTheme.PRIMARY, UiTheme.WHITE);

    // 页码按钮：显示"当前页/总页数"，点击弹出输入框跳转指定页
    private final JButton pageButton = UiTheme.createRoundButton("1/1", UiTheme.WHITE, UiTheme.TEXT_DARK);

    // 下一页按钮：最后一页时置灰禁用
    private final JButton nextButton = UiTheme.createRoundButton("下一页", UiTheme.PRIMARY, UiTheme.WHITE);

    // 总数据条数标签：显示"共 x 条数据"
    private final JLabel totalLabel = UiTheme.createLabel("共 0 条数据", UiTheme.TEXT_GRAY);

    /**
     * 构造分页栏：固定每页条数并注册页码变化回调
     *
     * @param pageSize           每页显示的数据条数
     * @param pageChangeListener 页码变化后的回调，持有面板用它重新渲染表格当前页
     */
    public PageBar(int pageSize, Runnable pageChangeListener) {
        this.pageSize = pageSize;
        this.pageChangeListener = pageChangeListener;
        // 分页控件水平居中排列，底色与主窗口内容区一致
        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0)); // 可修改参数：分页控件水平间距（8）
        setBackground(UiTheme.BG);
        // 四周留白：与上方表格区保持间距，不贴窗口边缘
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); // 可修改参数：分页栏四周留白（上下 8 / 左右 16）
        // 尺寸与提示弹窗的"确定"按钮一致（90×32）：加宽后"上一页/下一页"三字完整显示，页码按钮再多留 6px 容纳"999/999"
        prevButton.setPreferredSize(new Dimension(90, 32)); // 可修改参数：上一页按钮尺寸
        pageButton.setPreferredSize(new Dimension(96, 32)); // 可修改参数：页码按钮尺寸
        nextButton.setPreferredSize(new Dimension(90, 32)); // 可修改参数：下一页按钮尺寸
        // 上一页：页码减 1，越界由 goToPage 拦截
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goToPage(currentPage - 1);
            }
        });
        // 页码按钮：弹出输入框录入目标页码
        pageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showJumpDialog();
            }
        });
        // 下一页：页码加 1，越界由 goToPage 拦截
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goToPage(currentPage + 1);
            }
        });
        add(prevButton);
        add(pageButton);
        add(nextButton);
        add(totalLabel);
        // 构造后先按 0 条数据刷新一次文字与按钮状态
        updateBar();
    }

    /**
     * 更新数据总条数并回到第 1 页：切换分类、执行搜索等"重新浏览"场景使用
     *
     * @param totalCount 本次加载的数据总条数
     */
    public void setTotal(int totalCount) {
        this.totalCount = totalCount;
        currentPage = 1;
        updateBar();
    }

    /**
     * 更新数据总条数并保持当前页：行内按钮操作、对话框关闭后刷新等场景使用
     * 当前页超出新总页数时收敛到最后一页，避免停留在空页
     *
     * @param totalCount 本次加载的数据总条数
     */
    public void setTotalKeepPage(int totalCount) {
        this.totalCount = totalCount;
        currentPage = Math.min(currentPage, getTotalPages());
        updateBar();
    }

    /**
     * 获取当前页码：持有面板渲染表格时据此计算本页数据区间
     *
     * @return 当前页码，从 1 开始
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 获取每页条数：持有面板渲染表格时与当前页码配合计算数据区间
     *
     * @return 每页显示的数据条数
     */
    public int getPageSize() {
        return pageSize;
    }

    // 计算总页数：没有数据时按 1 页显示，页码按钮显示 1/1
    private int getTotalPages() {
        // 向上取整：49 条占 1 整页，51 条占 2 页
        return Math.max(1, (totalCount + pageSize - 1) / pageSize);
    }

    // 刷新分页栏文字与按钮状态：页码按钮、总条数标签、上一页 / 下一页的可用性与底色
    private void updateBar() {
        pageButton.setText(currentPage + "/" + getTotalPages());
        totalLabel.setText("共 " + totalCount + " 条数据");
        // 首页禁用上一页、末页禁用下一页；自绘按钮禁用后底色不会自动变灰，需同步换成浅灰
        prevButton.setEnabled(currentPage > 1);
        prevButton.setBackground(currentPage > 1 ? UiTheme.PRIMARY : UiTheme.BORDER);
        nextButton.setEnabled(currentPage < getTotalPages());
        nextButton.setBackground(currentPage < getTotalPages() ? UiTheme.PRIMARY : UiTheme.BORDER);
    }

    // 跳转到指定页：越界或页码无变化时不动作；合法请求更新页码并回调持有面板重绘表格
    private void goToPage(int page) {
        // 目标页超出 1~总页数范围：保持当前页不变
        if (page < 1 || page > getTotalPages()) {
            return;
        }
        // 目标页就是当前页：不重复回调，避免多余重绘
        if (page == currentPage) {
            return;
        }
        currentPage = page;
        updateBar();
        // 通知持有面板按新页码重绘表格
        pageChangeListener.run();
    }

    // 弹出扁平风格跳页窗口：白底正文 + 圆角输入框 + "确定/取消"圆角按钮，无图标装饰，与系统其他弹窗风格一致
    private void showJumpDialog() {
        // 记录用户输入：只有点"确定"或输入框回车时写入，点"取消"或关闭窗口保持 null
        final String[] input = {null};

        // 保留 Windows 系统自带标题栏（可拖动、可点 X 关闭），只有内容区按扁平风格自绘
        final JDialog dialog = new JDialog();
        dialog.setTitle("跳转页码");
        // 模态：弹窗关闭前阻塞主窗口操作
        dialog.setModal(true);
        // 不允许拉伸，避免内容区被拉变形
        dialog.setResizable(false);

        // 内容面板：纯白底，与 UiTheme 提示弹窗一致
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.WHITE);
        dialog.setContentPane(root);

        // 正文区：提示文字 + 页码输入框上下排列
        JPanel form = new JPanel(new GridLayout(2, 1, 0, 8)); // 可修改参数：文字与输入框垂直间距（8）
        form.setBackground(UiTheme.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(22, 16, 12, 16)); // 可修改参数：正文区四周留白（上 22 / 左右 16 / 下 12）
        JLabel hintLabel = new JLabel("请输入要跳转的页码（1-" + getTotalPages() + "）");
        hintLabel.setFont(UiTheme.FONT_NORMAL);
        hintLabel.setForeground(UiTheme.TEXT_DARK);
        form.add(hintLabel);
        final JTextField pageField = new JTextField();
        // 预填当前页码：改成临近页码时只需微调
        pageField.setText(String.valueOf(currentPage));
        // 输入框安装静态圆角边框，风格与登录界面、查询框输入框一致
        pageField.setBorder(UiTheme.roundBorder(UiTheme.TEXT_GRAY, 1));
        form.add(pageField);
        root.add(form, BorderLayout.CENTER);

        // 按钮行：居中放"确定"（绿底白字）和"取消"（白底深灰字）两个圆角按钮，样式与确认弹窗一致
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12)); // 可修改参数：按钮水平间距（16）
        buttonPanel.setBackground(UiTheme.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        JButton okButton = UiTheme.createRoundButton("确定", UiTheme.PRIMARY, UiTheme.WHITE);
        JButton cancelButton = UiTheme.createRoundButton("取消", UiTheme.WHITE, UiTheme.TEXT_DARK);
        okButton.setPreferredSize(new Dimension(90, 32));
        cancelButton.setPreferredSize(new Dimension(90, 32));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        // 确定：记录输入并关闭窗口，跳转校验在窗口关闭后进行，错误提示才能盖在最上层
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                input[0] = pageField.getText();
                dialog.dispose();
            }
        });
        // 输入框回车等同点击"确定"
        pageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                input[0] = pageField.getText();
                dialog.dispose();
            }
        });
        // 取消：直接关闭窗口，输入作废
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // 宽高按内容自适应，最窄不低于 320 与提示弹窗一致
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 320), dialog.getHeight());
        // 在主窗口上方居中显示
        dialog.setLocationRelativeTo(this);
        // 模态阻塞到这里，窗口关闭后继续处理输入
        dialog.setVisible(true);

        // 未点"确定"也未回车（取消 / 点标题栏 X 关闭）：不跳转
        if (input[0] == null) {
            return;
        }
        String pageText = input[0].trim();
        // 只接受 1~6 位纯数字：过滤非数字，也避免超长数字解析溢出
        if (!pageText.matches("\\d{1,6}")) {
            UiTheme.showMessageDialog(this, "请输入数字页码");
            return;
        }
        int page = Integer.parseInt(pageText);
        // 页码超出合法范围：提示后再跳
        if (page < 1 || page > getTotalPages()) {
            UiTheme.showMessageDialog(this, "页码超出范围（1-" + getTotalPages() + "）");
            return;
        }
        goToPage(page);
    }
}
