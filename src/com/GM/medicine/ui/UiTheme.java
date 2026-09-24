package com.GM.medicine.ui;

// 导入 Color：定义并统一使用扁平化配色
import java.awt.Color;
// 导入 Cursor：鼠标悬停在按钮上时显示手型光标
import java.awt.Cursor;
// 导入 Font：统一样式字体
import java.awt.Font;
// 导入 MouseEvent：实现按钮悬停变色效果
import java.awt.event.MouseEvent;
// 导入 MouseAdapter：只重写进入与离开两个方法，避免实现全部接口
import java.awt.event.MouseAdapter;
// 导入 BorderFactory：生成扁平边框
import javax.swing.BorderFactory;
// 导入 JButton：扁平按钮的基础组件
import javax.swing.JButton;
// 导入 Icon：按钮与标签的图标参数类型
import javax.swing.Icon;
// 导入 JLabel：创建统一字体的标签
import javax.swing.JLabel;

/**
 * - 界面扁平化主题工具类
 * - 集中定义配色、字体等样式常量，并提供扁平按钮、统一标签的快速创建方法
 * - 保证登录窗口与主窗口风格统一，后续新增的面板也复用这里定义的样式
 */
public final class UiTheme {

    // 主题色：医药绿，用于登录按钮、徽标等需要强调的位置
    public static final Color PRIMARY = new Color(0x26A69A);

    // 主题色加深版本，主题色按钮悬停时使用
    public static final Color PRIMARY_DARK = new Color(0x1F8A80);

    // 内容区浅灰底色，比纯白柔和，营造简洁感
    public static final Color BG = new Color(0xF4F6F8);

    // 纯白色，用于窗口与面板背景
    public static final Color WHITE = Color.WHITE;

    // 主要文字颜色，接近黑色的深灰，避免纯黑过于生硬
    public static final Color TEXT_DARK = new Color(0x2C3E50);

    // 次要文字颜色，用于状态栏等提示性文字
    public static final Color TEXT_GRAY = new Color(0x8A98A8);

    // 分割线与浅色按钮悬停时的描边颜色
    public static final Color BORDER = new Color(0xE3E8EF);

    // 标题字体
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 20);

    // 普通文字字体
    public static final Font FONT_NORMAL = new Font("微软雅黑", Font.PLAIN, 13);

    // 私有构造方法，本类只对外提供静态常量与方法
    private UiTheme() {
    }

    /**
     * 创建扁平化按钮：去除立体边框，悬停时背景色变化给出可点击反馈
     *
     * @param text       按钮文字
     * @param icon       按钮左侧图标，可为 null
     * @param background 背景色；实心按钮传主题色，幽灵按钮传浅灰或白色
     * @param foreground 文字颜色；实心按钮传白色，幽灵按钮传深灰
     * @return 设置好扁平样式的按钮
     */
    public static JButton createFlatButton(String text, Icon icon, Color background, Color foreground) {
        JButton button = new JButton(text, icon);
        button.setFont(FONT_NORMAL);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setIconTextGap(6);
        // 悬停时加深背景色，离开后恢复，两种按钮都需要这个反馈
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

    // 计算悬停时的背景色：主题色按钮加深，浅色按钮换更深的浅灰
    private static Color hoverColor(Color background) {
        if (background.equals(PRIMARY)) {
            return PRIMARY_DARK;
        }
        if (background.equals(WHITE)) {
            return BG;
        }
        return new Color(0xE8ECF0);
    }

    /**
     * 创建使用主题字体与颜色的标签
     *
     * @param text  标签文字
     * @param icon  标签左侧图标，可为 null
     * @param color 文字颜色
     * @return 设置好样式的标签
     */
    public static JLabel createLabel(String text, Icon icon, Color color) {
        JLabel label = new JLabel(text, icon, JLabel.LEFT);
        label.setFont(FONT_NORMAL);
        label.setForeground(color);
        label.setIconTextGap(6);
        return label;
    }
}