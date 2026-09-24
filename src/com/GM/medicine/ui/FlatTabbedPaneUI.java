package com.GM.medicine.ui;

// 导入 Insets：把内容区上方边距清零，使内容紧贴选项卡行
import java.awt.Insets;
// 导入 Rectangle：重写焦点指示器方法时需要的参数类型
import java.awt.Rectangle;
// 导入 FontMetrics：重写选项卡宽度计算方法时需要的参数类型
import java.awt.FontMetrics;
// 导入 Graphics：各绘制方法的图形上下文参数
import java.awt.Graphics;
// 导入 Graphics2D：开启抗锯齿绘制圆角卡片
import java.awt.Graphics2D;
// 导入 RenderingHints：抗锯齿渲染提示
import java.awt.RenderingHints;
// 导入 BasicTabbedPaneUI：继承基础选项卡外观，只重写绘制与尺寸相关方法
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * - 扁平化选项卡外观
 * - 继承基础选项卡外观，把默认金属风格的立体凸块形状改为圆角卡片样式
 * - 选中项为白色卡片加主题色下划线，未选中项为浅灰卡片，全部去掉描边与焦点虚线
 * - 供主窗口的 JTabbedPane 通过 setUI 使用，替换默认选项卡形状
 */
public class FlatTabbedPaneUI extends BasicTabbedPaneUI {

    // 卡片圆角半径（像素）
    private static final int RADIUS = 10;

    // 卡片与选项卡单元格左右边缘的留白（像素）
    private static final int INSET = 4;

    /**
     * 安装默认样式：先完成基础安装，再调整为扁平布局参数
     */
    @Override
    protected void installDefaults() {
        super.installDefaults();
        // 默认内容区上方有一圈边距，清零后内容与选项卡无缝衔接
        contentBorderInsets = new Insets(0, 0, 0, 0);
        // 取消相邻选项卡行之间的重叠，保证卡片之间留出间隙
        tabRunOverlay = 0;
        // 选项卡文字统一使用深灰色，主次关系由卡片底色与下划线表达
        tabPane.setForeground(UiTheme.TEXT_DARK);
    }

    /**
     * 增加选项卡高度，为圆角卡片留出上下留白
     */
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
    }

    /**
     * 增加选项卡宽度，避免文字与图标贴边
     */
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 24;
    }

    /**
     * 不绘制选项卡边框，扁平卡片依靠底色区分选中状态
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        // 空实现：去掉默认描边
    }

    /**
     * 不绘制焦点虚线框，保持界面干净
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                       int tabIndex, Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {
        // 空实现：去掉选中项上的虚线焦点框
    }

    /**
     * 绘制卡片底色：选中项为白色卡片加主题色下划线，未选中项为较矮的浅灰卡片
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        // 圆角矩形必须开启抗锯齿，否则边缘出现锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isSelected) {
            // 选中项：白色圆角卡片铺满单元格
            g2.setColor(UiTheme.WHITE);
            g2.fillRoundRect(x + INSET, y + 3, w - INSET * 2, h - 3, RADIUS, RADIUS);
            // 卡片底部绘制主题色下划线，标示当前所在模块
            g2.setColor(UiTheme.PRIMARY);
            g2.fillRoundRect(x + INSET + 8, y + h - 7, w - INSET * 2 - 16, 4, 2, 2);
        } else {
            // 未选中项：浅灰圆角卡片，高度略矮形成层级感
            g2.setColor(UiTheme.BG);
            g2.fillRoundRect(x + INSET, y + 3, w - INSET * 2, h - 7, RADIUS, RADIUS);
        }
        g2.dispose();
    }

    /**
     * 不绘制内容区边框，白色选项卡行与浅灰内容区靠底色自然分隔
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // 空实现：去掉默认内容边框
    }

    /**
     * 取消选中时标签的位移抖动，让文字与图标保持稳定
     */
    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }
}