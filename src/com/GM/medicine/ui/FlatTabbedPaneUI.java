package com.GM.medicine.ui;

// 导入 Insets：把选项卡内容区的默认上方边距清零，让面板紧贴选项卡底部
import java.awt.Insets;
// 导入 Rectangle：paintFocusIndicator 重写方法的参数类型，表示焦点指示器的绘制区域
import java.awt.Rectangle;
// 导入 FontMetrics：calculateTabWidth 重写方法需要用它测量选项卡文字宽度
import java.awt.FontMetrics;
// 导入 Graphics：所有绘制方法（paintTabBackground / paintContentBorder 等）的图形上下文参数
import java.awt.Graphics;
// 导入 Graphics2D：开启抗锯齿 + 画圆角矩形 + 控制线宽
import java.awt.Graphics2D;
// 导入 RenderingHints：KEY_ANTIALIASING → VALUE_ANTIALIAS_ON，让圆角边缘平滑无锯齿
import java.awt.RenderingHints;
// 导入 Color：选中项白色卡片、未选中项浅灰卡片、主题色下划线的颜色常量
import java.awt.Color;
// 导入 BasicTabbedPaneUI：JTabbedPane 的基础外观类，继承它只需要重写绘制和尺寸方法，就能改变选项卡外观
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * - 扁平化选项卡外观
 * - 继承 BasicTabbedPaneUI，把默认金属风格的立体梯形凸块形状改为圆角卡片样式
 * - 选中项：白色圆角卡片铺满单元格，底部再画一条主题色下划线标示"当前选中"
 * - 未选中项：较矮的浅灰圆角卡片，形成"被压在下层"的层级感
 * - 全部去掉默认描边、默认内容边框、默认焦点虚线框，保持扁平干净
 * - 主窗口通过 tabbedPane.setUI(new FlatTabbedPaneUI()) 换入这个外观
 */
public class FlatTabbedPaneUI extends BasicTabbedPaneUI {

    // 卡片圆角半径（像素）：决定圆角"圆"的程度，10px 对应中等圆润
    private static final int RADIUS = 10;

    // 卡片与选项卡单元格左右边缘的留白（像素）：让卡片不紧贴单元格边界
    private static final int INSET = 4;

    /**
     * 安装默认外观参数：先调父类，再把参数调整为扁平风格需要的值
     * 这是 BasicTabbedPaneUI 的生命周期方法，JTabbedPane 初始化时自动调一次
     */
    @Override
    protected void installDefaults() {
        // 先让父类完成 JTabbedPane 的基础安装（字体、颜色、各类默认值）
        super.installDefaults();
        // contentBorderInsets 默认会在选项卡内容区上方留一圈边距，清零后面板紧贴选项卡底部
        contentBorderInsets = new Insets(0, 0, 0, 0);
        // tabRunOverlay 默认 4，会让相邻两行选项卡互相重叠；清零后卡片之间独立
        tabRunOverlay = 0;
        // 选项卡上的文字统一设为深灰色，选中/未选中的主次关系靠卡片底色和下划线表达
        tabPane.setForeground(UiTheme.TEXT_DARK);
    }

    /**
     * 重写选项卡高度计算：原基础上 +10 像素，给圆角卡片的上下留白腾空间
     */
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        // 先拿父类计算出的基础高度，再额外加 10
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
    }

    /**
     * 重写选项卡宽度计算：原基础上 +24 像素，避免文字和图标贴紧边框
     */
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        // 先拿父类用 FontMetrics 算出的文字宽度，再额外加 24
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 24;
    }

    /**
     * 不绘制选项卡边框：扁平卡片靠底色区分选中状态，不需要额外描边
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        // 空实现：故意什么都不画，让默认边框消失
    }

    /**
     * 不绘制选中项上的焦点虚线框：保持扁平界面干净
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                       int tabIndex, Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {
        // 空实现：故意什么都不画，默认虚线焦点框消失
    }

    /**
     * 核心绘制方法：画卡片底色 + 选中项下划线
     * 选中项 → 白色圆角卡片铺满 + 底部主题色下划线
     * 未选中项 → 较矮的浅灰圆角卡片（形成层级感）
     *
     * 参数 x/y/w/h 是 BasicTabbedPaneUI 算好的单元格坐标，isSelected 表示当前这个选项卡是否被选中
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        // 把 Graphics 转成 Graphics2D，才能开抗锯齿 + 画圆角矩形
        Graphics2D g2 = (Graphics2D) g.create();
        // 开启抗锯齿：圆角矩形必须开，否则边缘出现锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
            // ====== 选中项 ======
            // 白色卡片：左上角 x+INSET、y+3，宽 w-INSET*2、高 h-3，圆角 RADIUS
            // 用 h-3 而不是 h：底部留 3 像素空间给下划线，视觉上更干净
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x + INSET, y + 3, w - INSET * 2, h - 3, RADIUS, RADIUS);
            // 主题色下划线：位置在卡片底部往上约 7 像素处，高度 4 像素，两端各比卡片窄 8 像素
            // 圆角 2,2 让下划线两端圆头，避免直角显得突兀
            g2.setColor(UiTheme.PRIMARY);
            g2.fillRoundRect(x + INSET + 8, y + h - 7, w - INSET * 2 - 16, 4, 2, 2);
        } else {
            // ====== 未选中项 ======
            // 浅灰卡片：高度 h-7 而不是 h-3，比选中矮 4 像素，底部留空形成"压在下层"的层级感
            g2.setColor(UiTheme.BG);
            g2.fillRoundRect(x + INSET, y + 3, w - INSET * 2, h - 7, RADIUS, RADIUS);
        }

        // 释放 Graphics2D 资源，避免内存泄漏
        g2.dispose();
    }

    /**
     * 不绘制内容区边框：默认会在选项卡内容周围画一圈边框，这里去掉
     * 白色选项卡行和浅灰内容区靠底色自然分隔就够了
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // 空实现：故意什么都不画
    }

    /**
     * 取消选中时标签的 X 轴位移抖动：默认选中项文字会向右偏 1px，让选中标签水平位置与未选中一致
     */
    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;  // 返回 0 表示"不移动"
    }

    /**
     * 取消选中时标签的 Y 轴位移抖动：默认选中项文字会向上偏 1px，让选中标签垂直位置与未选中一致
     */
    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;  // 返回 0 表示"不移动"
    }
}