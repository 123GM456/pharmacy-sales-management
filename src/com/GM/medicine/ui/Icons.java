package com.GM.medicine.ui;

// 导入 BasicStroke：设置线条粗细与圆头端点，锁环、电源环等线条类图标需要
import java.awt.BasicStroke;
// 导入 Color：绘制白色十字等固定色图形
import java.awt.Color;
// 导入 Component：Icon 接口绘制方法的参数类型
import java.awt.Component;
// 导入 Graphics：图形上下文基类，paintIcon 方法签名需要
import java.awt.Graphics;
// 导入 Graphics2D：提供抗锯齿、线宽控制等二维绘图增强能力
import java.awt.Graphics2D;
// 导入 RenderingHints：开启抗锯齿渲染提示，让圆弧与斜线平滑
import java.awt.RenderingHints;
// 导入 Icon：Swing 图标接口，实现 paintIcon 完成自绘图标
import javax.swing.Icon;

/**
 * - 扁平化图标库
 * - 全部图标用 Java2D 程序绘制，不依赖任何外部图片资源，任意缩放不失真
 * - 图标配色取自 UiTheme 主题色板，供登录窗口与主窗口统一使用
 */
public final class Icons {

    // 图标种类，内部按种类选择对应的绘制逻辑
    private enum Kind { LOGO, USER, LOCK, PILL, CHART, POWER }

    // 私有构造方法，本类只对外提供静态工厂方法
    private Icons() {
    }

    /**
     * 创建系统徽标：主题色圆角方块内嵌白色十字
     *
     * @param size 图标边长（像素）
     * @return 徽标图标
     */
    public static Icon logo(int size) {
        return create(Kind.LOGO, size);
    }

    /**
     * 创建用户图标：圆头像加肩膀半圆，用于用户名输入项
     *
     * @param size 图标边长（像素）
     * @return 用户图标
     */
    public static Icon user(int size) {
        return create(Kind.USER, size);
    }

    /**
     * 创建锁图标：锁环加锁体，用于密码输入项
     *
     * @param size 图标边长（像素）
     * @return 锁图标
     */
    public static Icon lock(int size) {
        return create(Kind.LOCK, size);
    }

    /**
     * 创建胶囊图标：横向胶囊加中线分割，用于药品管理选项卡
     *
     * @param size 图标边长（像素）
     * @return 胶囊图标
     */
    public static Icon pill(int size) {
        return create(Kind.PILL, size);
    }

    /**
     * 创建柱状图图标：三根渐高的圆角柱，用于销售管理选项卡
     *
     * @param size 图标边长（像素）
     * @return 柱状图图标
     */
    public static Icon chart(int size) {
        return create(Kind.CHART, size);
    }

    /**
     * 创建电源图标：缺口圆环加竖线，用于退出按钮
     *
     * @param size 图标边长（像素）
     * @return 电源图标
     */
    public static Icon power(int size) {
        return create(Kind.POWER, size);
    }

    /**
     * 图标统一入口：构造一个按种类绘制几何图形的 Icon 实现
     *
     * @param kind 图标种类
     * @param size 图标边长（像素）
     * @return 自绘的扁平图标
     */
    private static Icon create(final Kind kind, final int size) {
        return new Icon() {
            public int getIconWidth() {
                return size;
            }

            public int getIconHeight() {
                return size;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                // 平移到图标位置并开启抗锯齿，保证圆弧与斜线平滑
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                draw(g2, kind, size);
                g2.dispose();
            }
        };
    }

    // 按种类分发到对应的绘制方法，保持 paintIcon 简洁
    private static void draw(Graphics2D g2, Kind kind, int s) {
        if (kind == Kind.LOGO) {
            drawLogo(g2, s);
        } else if (kind == Kind.USER) {
            drawUser(g2, s);
        } else if (kind == Kind.LOCK) {
            drawLock(g2, s);
        } else if (kind == Kind.PILL) {
            drawPill(g2, s);
        } else if (kind == Kind.CHART) {
            drawChart(g2, s);
        } else {
            drawPower(g2, s);
        }
    }

    // 徽标：主题色圆角方块加白色十字
    private static void drawLogo(Graphics2D g2, int s) {
        g2.setColor(UiTheme.PRIMARY);
        int radius = s / 5;
        g2.fillRoundRect(0, 0, s, s, radius, radius);
        g2.setColor(Color.WHITE);
        // 十字横竖两臂厚度均为边长的五分之一，居中放置
        int arm = s / 5;
        g2.fillRect((s - arm) / 2, s / 5, arm, s * 3 / 5);
        g2.fillRect(s / 5, (s - arm) / 2, s * 3 / 5, arm);
    }

    // 用户：灰色圆头加肩膀半圆
    private static void drawUser(Graphics2D g2, int s) {
        g2.setColor(UiTheme.TEXT_GRAY);
        // 头部：位于中上部的圆
        int head = s * 36 / 100;
        g2.fillOval((s - head) / 2, s * 12 / 100, head, head);
        // 肩膀：取大椭圆的上半部，底部收在图标内不越界
        g2.fillArc(s * 12 / 100, s * 50 / 100, s * 76 / 100, s * 96 / 100, 0, 180);
    }

    // 锁：灰色锁环加锁体，锁体内嵌白色锁孔
    private static void drawLock(Graphics2D g2, int s) {
        g2.setColor(UiTheme.TEXT_GRAY);
        // 线宽为边长的十分之一，圆头端点让线条更柔和
        g2.setStroke(new BasicStroke(s / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // 锁环：上半圆弧，两端没入锁体
        g2.drawArc(s * 30 / 100, s * 6 / 100, s * 40 / 100, s * 50 / 100, 0, 180);
        // 锁体：圆角矩形
        g2.fillRoundRect(s * 18 / 100, s * 30 / 100, s * 64 / 100, s * 60 / 100, s / 5, s / 5);
        // 锁孔：白色小圆点形成镂空效果
        g2.setColor(UiTheme.WHITE);
        int hole = s / 5;
        g2.fillOval((s - hole) / 2, s * 50 / 100, hole, hole);
    }

    // 胶囊：主题色横向胶囊加白色中线分割
    private static void drawPill(Graphics2D g2, int s) {
        g2.setColor(UiTheme.PRIMARY);
        int height = s * 36 / 100;
        g2.fillRoundRect(s * 8 / 100, (s - height) / 2, s * 84 / 100, height, height, height);
        // 白色中线把胶囊一分为二，暗示“一板药品”的语义
        g2.setColor(UiTheme.WHITE);
        g2.setStroke(new BasicStroke(s / 12f));
        g2.drawLine(s / 2, (s - height) / 2, s / 2, (s + height) / 2);
    }

    // 柱状图：三根圆角柱逐根升高，底部对齐
    private static void drawChart(Graphics2D g2, int s) {
        g2.setColor(UiTheme.PRIMARY);
        int bottom = s * 88 / 100;
        int width = s * 16 / 100;
        int gap = s * 12 / 100;
        int radius = s / 10;
        // 三根柱子高度依次递增，模拟销售增长趋势
        int[] heights = {s * 32 / 100, s * 52 / 100, s * 72 / 100};
        int x = s * 12 / 100;
        for (int height : heights) {
            g2.fillRoundRect(x, bottom - height, width, height, radius, radius);
            x += width + gap;
        }
    }

    // 电源：缺口圆环加竖线，构成标准电源符号
    private static void drawPower(Graphics2D g2, int s) {
        g2.setColor(UiTheme.TEXT_GRAY);
        g2.setStroke(new BasicStroke(s / 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // 圆环留出顶部缺口，竖线穿过缺口
        g2.drawArc(s * 20 / 100, s * 24 / 100, s * 60 / 100, s * 60 / 100, 110, 320);
        g2.drawLine(s / 2, s * 8 / 100, s / 2, s * 44 / 100);
    }
}