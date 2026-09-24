package com.GM.medicine.ui;

// 导入 Color：定义并统一使用扁平化配色（主题色、底色、文字色、分割线色）
import java.awt.Color;
// 导入 Cursor：鼠标悬停在按钮上时把箭头改成小手形状，暗示可以点击
import java.awt.Cursor;
// 导入 Font：统一样式字体（微软雅黑 粗体 20 / 常规 13），所有标题和正文都复用这里定义的字体对象
import java.awt.Font;
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
     * 为输入框安装焦点边框效果：未选中时浅灰 1px，选中时黑色加粗 2px
     * 适用于 JTextField / JPasswordField 等 JComponent 子类
     *
     * @param component 要安装效果的输入框组件
     */
    public static void installFocusBorder(final javax.swing.JComponent component) {
        // 默认边框：次要文字色（TEXT_GRAY）1px 细线
        final javax.swing.border.Border normalBorder = BorderFactory.createLineBorder(TEXT_GRAY, 1);
        // 聚焦边框：主要文字色（TEXT_DARK）2px 加粗线条，与未选中态形成明显对比
        final javax.swing.border.Border focusBorder = BorderFactory.createLineBorder(TEXT_DARK, 2);
        // 先设为默认边框
        component.setBorder(normalBorder);
        // 注册焦点监听器：获得焦点换黑边，失去焦点恢复灰边
        component.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                component.setBorder(focusBorder);
            }
            public void focusLost(FocusEvent e) {
                component.setBorder(normalBorder);
            }
        });
    }
}