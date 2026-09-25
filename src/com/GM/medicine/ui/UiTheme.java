package com.GM.medicine.ui;

// 导入 Color：定义并统一使用扁平化配色（主题色、底色、文字色、分割线色）
import java.awt.Color;
// 导入 Cursor：鼠标悬停在按钮上时把箭头改成小手形状，暗示可以点击
import java.awt.Cursor;
// 导入 Font：统一样式字体（微软雅黑 粗体 20 / 常规 13），所有标题和正文都复用这里定义的字体对象
import java.awt.Font;
// 导入 Graphics：圆角边框绘制的画布上下文，paintBorder 的参数类型
import java.awt.Graphics;
// 导入 Graphics2D：Graphics 的二维增强版，用它开启抗锯齿让圆角曲线平滑
import java.awt.Graphics2D;
// 导入 Insets：声明圆角边框占用的留白（上下 2 / 左右 6），保证光标和文字不贴线
import java.awt.Insets;
// 导入 RenderingHints：抗锯齿渲染提示的键值定义
import java.awt.RenderingHints;
// 导入 FocusEvent：输入框获得 / 失去焦点时的事件参数类型
import java.awt.event.FocusEvent;
// 导入 FocusListener：监听输入框焦点变化的接口，用于切换焦点边框样式
import java.awt.event.FocusListener;
// 导入 MouseEvent：鼠标进入 / 离开按钮区域时的事件参数类型
import java.awt.event.MouseEvent;
// 导入 MouseAdapter：只重写 mouseEntered 和 mouseExited 两个方法，避免实现 MouseListener 全部 5 个方法
import java.awt.event.MouseAdapter;
// 导入 BorderFactory：生成扁平按钮用的空边框（EmptyBorder），用来控制按钮内文字的上下左右留白
import javax.swing.BorderFactory;
// 导入 JButton：扁平按钮的基类组件，createFlatButton 返回的就是它（只是配置不同样式）
import javax.swing.JButton;
// 导入 JLabel：创建统一字体和颜色的标签，供标题栏、表单、状态栏复用
import javax.swing.JLabel;

/**
 * - 界面扁平化主题工具类
 * - 集中定义配色、字体等样式常量，并提供扁平按钮、统一标签的快速创建方法
 * - 登录窗口和主窗口都复用这里的样式，保证风格统一；后续新增面板也直接调用
 * - 所有方法都是 static 的，不需要 new UiTheme()；私有构造方法防止实例化
 */
public final class UiTheme {

    // 主题主色（医药绿）#26A69A：用于登录按钮实心背景、选中项下划线等需要强调的位置
    public static final Color PRIMARY = new Color(0x26A69A);

    // 主题色加深版 #1F8A80：悬停在主题色按钮上时换这个颜色，给出颜色变化的点击反馈
    public static final Color PRIMARY_DARK = new Color(0x1F8A80);

    // 纯白 #FFFFFF：窗口内容面板、标题栏、状态栏、选中的选项卡卡片底色
    public static final Color WHITE = Color.WHITE;

    // 内容区浅灰底色 #F4F6F8：比纯白柔和，用来做未选中选项卡、内容面板的底色
    public static final Color BG = new Color(0xF4F6F8);

    // 主要文字色（接近黑的深灰）#2C3E50：避免纯黑过于生硬，用于标题、按钮文字、输入框内文字
    public static final Color TEXT_DARK = new Color(0x2C3E50);

    // 次要文字色（浅灰）#8A98A8：用于状态栏、placeholder 等提示性文字，不抢主要内容注意力
    public static final Color TEXT_GRAY = new Color(0x8A98A8);

    // 分割线色（更浅的灰）#E3E8EF：用于标题栏底部 1px 细分割线、状态栏顶部 1px 细分割线、浅色按钮悬停底色
    public static final Color BORDER = new Color(0xE3E8EF);

    // 标题字体：微软雅黑、粗体、20 号；用 new Font("微软雅黑", Font.BOLD, 20) 创建
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 20);

    // 普通文字字体：微软雅黑、常规、13 号；按钮 / 标签 / 输入框都用这个
    public static final Font FONT_NORMAL = new Font("微软雅黑", Font.PLAIN, 13);

    // 私有构造方法：防止外部 new UiTheme()，本类只提供静态常量和静态工厂方法
    private UiTheme() {
    }

    /**
     * 创建扁平化按钮：去掉默认立体边框，悬停时背景色变化给出可点击反馈
     *
     * @param text       按钮上显示的文字
     * @param background 按钮初始背景色：实心按钮传 PRIMARY；幽灵按钮传 BG 或 WHITE
     * @param foreground 按钮文字颜色：实心按钮传 WHITE；幽灵按钮传 TEXT_DARK
     * @return 设置好扁平样式的 JButton 实例，外部直接 add 到面板即可
     */
    public static JButton createFlatButton(String text, Color background, Color foreground) {
        // 创建按钮对象（纯文字）
        JButton button = new JButton(text);
        // 使用主题普通字体
        button.setFont(FONT_NORMAL);
        // 按钮文字颜色（前景色）
        button.setForeground(foreground);
        // 按钮初始背景色
        button.setBackground(background);
        // 关闭默认焦点虚线框，保持扁平界面干净
        button.setFocusPainted(false);
        // 让按钮自定义绘制背景：配合 setOpaque(true) 实现扁平样式
        button.setContentAreaFilled(false);
        // 把按钮设为不透明，这样 setBackground 设置的颜色才能生效
        button.setOpaque(true);
        // 用空边框控制按钮内边距：上 8 / 右 18 / 下 8 / 左 18
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        // 鼠标移动到按钮上时显示手型光标，暗示这个区域可以点击
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 注册匿名 MouseAdapter：鼠标进入按钮区域时换悬停色，离开时恢复原色
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor(background));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
        return button;
    }

    // 计算悬停时应该用的背景色：主题绿 → 更深的绿；白色 → 浅灰；其他情况 → 略深浅灰
    private static Color hoverColor(Color background) {
        // 如果初始就是主题绿，悬停换更深的 PRIMARY_DARK
        if (background.equals(PRIMARY)) {
            return PRIMARY_DARK;
        }
        // 如果初始是纯白，悬停换成内容区浅灰 BG
        if (background.equals(WHITE)) {
            return BG;
        }
        // 其他情况（例如退出按钮传的 BG 本身）换成略深的 #E8ECF0，保持颜色变化
        return new Color(0xE8ECF0);
    }

    /**
     * 创建统一样式的标签：统一字体、统一颜色、左对齐，不带图标
     *
     * @param text  标签文字
     * @param color 文字颜色：次要信息用 TEXT_GRAY；主要信息用 TEXT_DARK
     * @return 设置好样式的 JLabel 实例
     */
    public static JLabel createLabel(String text, Color color) {
        // 创建左对齐纯文字标签
        JLabel label = new JLabel(text, JLabel.LEFT);
        // 使用主题普通字体
        label.setFont(FONT_NORMAL);
        // 设置文字颜色
        label.setForeground(color);
        return label;
    }

    /**
     * 为输入框安装焦点边框效果：未选中时浅灰 1px 圆角线，选中时深色 1px 圆角线
     * 圆角线用匿名 Border 内部类自绘（Swing 没有现成的圆角边框），留白直接并入边框 insets
     * 关键前提：必须关闭组件的不透明填充——JTextField 默认的直角白底会盖住圆角外的三角区域，
     * setOpaque(false) 后透出父面板的白底，视觉上才是真正的圆角
     *
     * @param component 要安装效果的组件（JTextField / JPasswordField 等）
     */
    public static void installFocusBorder(final javax.swing.JComponent component) {
        // 圆角半径（像素）：想调整弧度只改这一个数
        final int arc = 10;
        // 关闭直角白色填充：否则圆角线画在直角白底上，四个角会被白底截断
        component.setOpaque(false);

        // 未选中边框：TEXT_GRAY 浅灰圆角线；聚焦边框：TEXT_DARK 深色圆角线
        // 两者 insets 完全一致（上下 2 / 左右 6），切换时组件尺寸不变，不会引起布局跳动
        final javax.swing.border.Border normalBorder = roundBorder(TEXT_GRAY, arc);
        final javax.swing.border.Border focusBorder = roundBorder(TEXT_DARK, arc);
        // 先设为默认边框
        component.setBorder(normalBorder);
        // 注册焦点监听器：获得焦点换深色圆角线，失去焦点恢复浅灰圆角线
        component.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                component.setBorder(focusBorder);
            }
            public void focusLost(FocusEvent e) {
                component.setBorder(normalBorder);
            }
        });
    }

    // 创建 1px 圆角描边：匿名 Border 内部类自绘圆角矩形
    // insets 取上下 2 / 左右 6（线 1 + 留白 5），与旧版 LineBorder(1) + EmptyBorder(1,5,1,5) 的总留白一致
    private static javax.swing.border.Border roundBorder(final Color color, final int arc) {
        return new javax.swing.border.Border() {
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                // g.create() 克隆画布，改变抗锯齿等状态后用 dispose 恢复，不污染组件后续绘制
                Graphics2D g2 = (Graphics2D) g.create();
                // 开抗锯齿：不开的话圆角曲线会有明显锯齿
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                // 画圆角描边：宽高各减 1 让 1px 线完整落在组件边界内
                g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
                g2.dispose();
            }
            public Insets getBorderInsets(java.awt.Component c) {
                // 线宽 1 + 留白 5，保证光标和文字不贴着圆角线
                return new Insets(2, 6, 2, 6);
            }
            // 返回 false：边框是透明绘制的圆角线，Swing 不会先拿它填充整块矩形底色
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }

    /**
     * 弹出与系统扁平风格一致的提示对话框：Windows 系统标题栏 + 白底正文 + 扁平按钮
     * 替代系统自带的 JOptionPane 弹窗（灰色 Metal 底色，与扁平界面不协调）
     * 配色、字体、按钮全部复用本类既有资源，不引入新的样式定义
     *
     * @param parent  父组件，弹窗在其上方居中显示；传 null 则屏幕居中
     * @param message 提示文字内容
     */
    public static void showMessageDialog(final java.awt.Component parent, String message) {
        // 保留 Windows 系统自带标题栏（可拖动、可点 X 关闭），只有内容区按扁平风格自绘
        final javax.swing.JDialog dialog = new javax.swing.JDialog();
        // 系统标题栏上显示的文字
        dialog.setTitle("提示");
        // 模态：弹窗关闭前阻塞父窗口操作
        dialog.setModal(true);
        // 提示弹窗不允许拉伸，避免内容区被拉变形
        dialog.setResizable(false);

        // 内容面板：纯白底。顶部不再自绘绿色标题条（交给系统标题栏），也不画描边（系统窗口自带边框）
        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(WHITE);
        dialog.setContentPane(root);

        // 正文：深灰文字 + 主题普通字体，四周留白让文字不贴窗口边缘
        javax.swing.JLabel messageLabel = new javax.swing.JLabel(message);
        messageLabel.setFont(FONT_NORMAL);
        messageLabel.setForeground(TEXT_DARK);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(22, 16, 22, 16));
        root.add(messageLabel, java.awt.BorderLayout.CENTER);

        // 按钮行：居中放一个扁平“确定”按钮，直接复用 createFlatButton 工厂，尺寸与登录按钮一致
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 12));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        javax.swing.JButton okButton = createFlatButton("确定", PRIMARY, WHITE);
        okButton.setPreferredSize(new java.awt.Dimension(90, 32));
        buttonPanel.add(okButton);
        root.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // 点击“确定”关闭弹窗；模态弹窗关闭后本方法才返回，调用方不用关心结果
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        // 宽高按内容自适应，最窄不低于 320 保证长提示不至于拥挤换行
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 320), dialog.getHeight());
        // 在父窗口上方居中显示；parent 为 null 时自动改为屏幕居中
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}