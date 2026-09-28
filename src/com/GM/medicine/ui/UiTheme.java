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
// 导入 Insets：声明圆角边框占用的留白，保证光标和文字不贴线
import java.awt.Insets;
// 导入 BasicStroke：设置圆角描边的线宽，支持 1px 细线与 2px 加粗
import java.awt.BasicStroke;
// 导入 RenderingHints：抗锯齿渲染提示的键值定义
import java.awt.RenderingHints;
// 导入 FocusEvent：登录输入框获得 / 失去焦点时的事件参数类型
import java.awt.event.FocusEvent;
// 导入 FocusListener：监听输入框焦点变化的接口，用于切换登录输入框的焦点边框样式
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

    // 计算圆角按钮的描边色：全部复用主题常量，且与当前底色不同、随底色联动
    // 悬停 setBackground 换底色后，paintComponent 会用新底色重新查表，描边一起变色
    private static Color strokeColor(Color background) {
        // 主题绿底 → 深绿描边（PRIMARY_DARK）
        if (background.equals(PRIMARY)) {
            return PRIMARY_DARK;
        }
        // 悬停后的深绿底 → 深灰描边（TEXT_DARK），避免描边与底色同色而"消失"
        if (background.equals(PRIMARY_DARK)) {
            return TEXT_DARK;
        }
        // 其余浅灰底（退出按钮常态与悬停态）→ 中灰描边（TEXT_GRAY）
        return TEXT_GRAY;
    }

    /**
     * 创建圆角扁平按钮：配置与 createFlatButton 完全一致，仅把直角底色换成自绘的圆角底色
     * 只影响本工厂创建的按钮，createFlatButton 与所有直角按钮不受影响
     *
     * @param text       按钮文字
     * @param background 初始背景色
     * @param foreground 文字颜色
     * @return 设置好圆角扁平样式的 JButton 实例
     */
    public static JButton createRoundButton(String text, Color background, Color foreground) {
        // 匿名子类：关闭默认直角填充，先自绘圆角底色再画文字
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                // 克隆画布开抗锯齿：圆角边缘平滑，用完 dispose 不污染后续绘制
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 用背景色填充圆角矩形：悬停变色通过 setBackground 换色即可生效
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); // 可修改参数：圆角半径（20）
                // 画圆角描边：颜色随当前底色联动（见 strokeColor），悬停变色时描边一起变
                g2.setColor(strokeColor(getBackground()));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                // 父类继续绘制按钮文字
                super.paintComponent(g);
            }
        };
        // 以下配置与 createFlatButton 保持一致
        button.setFont(FONT_NORMAL);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        // 关闭 Swing 默认底色绘制：底色完全由上面的 paintComponent 自绘圆角
        button.setContentAreaFilled(false);
        // 按钮本体透明：直角矩形不再填充，只显示圆角底色
        button.setOpaque(false);
        // 用空边框控制按钮内边距：上 8 / 右 18 / 下 8 / 左 18
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        // 鼠标移动到按钮上时显示手型光标，暗示这个区域可以点击
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 注册匿名 MouseAdapter：鼠标进入按钮区域时换悬停色，离开时恢复原色（复用 hoverColor）
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                // 禁用状态下不响应悬停变色，保持置灰外观提示不可点击
                if (button.isEnabled()) {
                    button.setBackground(hoverColor(background));
                }
            }
            public void mouseExited(MouseEvent e) {
                // 离开时恢复原色；禁用状态恢复置灰浅色，避免退回主题绿误导为可点击
                button.setBackground(button.isEnabled() ? background : BORDER);
            }
        });
        return button;
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
     * 创建扁平化下拉框：白底主体 + 右侧浅灰箭头色块 + 黑色箭头，整体无立体感
     * 鼠标悬停、下拉框获得焦点、点击展开时箭头色块加深；主体边框与输入框同款圆角浅灰线
     *
     * @param items 下拉选项文字数组
     * @return 设置好扁平样式的下拉框
     */
    public static javax.swing.JComboBox<String> createComboBox(String[] items) {
        // 匿名子类：仅固定首选高度与输入框一致，避免默认 UI 撑高导致与表单其他输入框不齐
        javax.swing.JComboBox<String> box = new javax.swing.JComboBox<String>(items) {
            public java.awt.Dimension getPreferredSize() {
                // 宽度沿用默认计算（由最长选项决定），高度强制为 22
                java.awt.Dimension size = super.getPreferredSize();
                size.height = 22; // 可修改参数：下拉框高度（实测 roundBorder 输入框首选高度 22）
                return size;
            }
        };
        // 字体与颜色：普通字体、深灰文字、白底，与输入框保持一致
        box.setFont(FONT_NORMAL);
        box.setForeground(TEXT_DARK);
        box.setBackground(WHITE);
        // 圆角浅灰边框：与输入框复用同一套 roundBorder 圆角描边，保证外观统一
        box.setBorder(roundBorder(TEXT_GRAY, 1));
        // 替换默认 Metal 立体箭头：右侧浅灰色块 + 黑色箭头，悬停/聚焦/展开时色块加深
        box.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            // 下拉列表弹层默认带深色立体边框，统一改为与输入框一致的浅灰 1px 细边框
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                javax.swing.plaf.basic.ComboPopup popup = super.createPopup();
                ((javax.swing.JComponent) popup).setBorder(javax.swing.BorderFactory.createLineBorder(TEXT_GRAY, 1)); // 可修改参数：弹层边框颜色
                return popup;
            }
            // 封闭框聚焦时 BasicComboBoxUI 会强制用下拉列表的选中色渲染当前值（默认是深色底），
            // 安装 UI 后把列表选中色改为白底深字：主体聚焦时保持白底，加深只作用于箭头色块
            public void installUI(javax.swing.JComponent c) {
                super.installUI(c);
                listBox.setSelectionBackground(WHITE);
                listBox.setSelectionForeground(TEXT_DARK);
            }
            protected javax.swing.JButton createArrowButton() {
                // 匿名子类：自绘实心下三角，形状等效字符"▼"但不受字体和按钮宽度影响——
                // 窄按钮放不下字符时 Swing 会把文字截断显示为"…"省略号，自绘则任何宽度都正确
                javax.swing.JButton button = new javax.swing.JButton() {
                    protected void paintComponent(Graphics g) {
                        // 先铺满浅灰色块底色（悬停/聚焦加深通过 setBackground 换色即可生效）
                        g.setColor(getBackground());
                        g.fillRect(0, 0, getWidth(), getHeight());
                        // 开抗锯齿后用多边形填充画实心下三角：底边在上、顶点在下，水平垂直居中
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;
                        int halfWidth = 5; // 可修改参数：三角形底边半宽（总宽 10）
                        int height = 6; // 可修改参数：三角形高度（6）
                        int[] xs = {centerX - halfWidth, centerX + halfWidth, centerX};
                        int[] ys = {centerY - height / 2, centerY - height / 2, centerY + height / 2};
                        // 箭头用深灰近黑色，与文字主色一致
                        g2.setColor(TEXT_DARK);
                        g2.fillPolygon(xs, ys, 3);
                        g2.dispose();
                    }
                };
                // 常态浅灰色块：与系统背景色一致，扁平无立体边框
                button.setBackground(BG);
                button.setOpaque(true);
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8)); // 可修改参数：箭头区大小（上下 2 / 左右 8）
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // 鼠标悬停时色块加深，移开恢复
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        button.setBackground(new Color(0xE8ECF0)); // 可修改参数：悬停/聚焦加深色
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        button.setBackground(BG);
                    }
                });
                // 下拉框获得焦点（点击或 Tab 切入）时色块同步加深，失去焦点恢复
                box.addFocusListener(new java.awt.event.FocusListener() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        button.setBackground(new Color(0xE8ECF0));
                    }
                    public void focusLost(java.awt.event.FocusEvent e) {
                        button.setBackground(BG);
                    }
                });
                return button;
            }
        });
        // 下拉列表项渲染器：统一字体、白底、选中项浅灰底，去掉默认的立体选中框
        box.setRenderer(new javax.swing.DefaultListCellRenderer() {
            // DefaultListCellRenderer 会在"自身背景色与父容器相同"时把 isOpaque 判为 false 而跳过绘制背景，
            // 导致 UI 预填的灰色（Metal 默认 ComboBox.background=#EEEEEE）透出来；这里强制始终绘制背景
            public boolean isOpaque() {
                return true;
            }
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // 先由父类完成基础渲染，再覆盖扁平样式
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_NORMAL);
                setForeground(TEXT_DARK);
                if (index == -1) {
                    // index=-1 表示封闭状态下拉框的当前值：白底与输入框一致；上下不留白，防止固定高度下文字被裁剪
                    setBackground(WHITE);
                    setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2)); // 可修改参数：当前值内边距（左右 2）
                } else {
                    // 下拉列表项：选中项浅灰底、其余白底，用背景色区分选中
                    setBackground(isSelected ? BG : WHITE);
                    setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6)); // 可修改参数：列表项内边距（上下 4 / 左右 6）
                }
                return this;
            }
        });
        return box;
    }

    /**
     * 创建圆角白底输入框：内部先自绘圆角白色底再画文字，配合 roundBorder 圆角线组成完整的圆角输入框
     * 背景：白底。为什么自绘：输入框必须 setOpaque(false) 才不会被直角白底截断圆角线，
     * 透明后透出的是父容器底色（登录窗浅灰、对话框白色，不统一），所以白底也要按圆角自绘
     *
     * @param columns 推荐列数（估算宽度用），0 表示不限制
     * @return 圆角白底的文本输入框
     */
    public static javax.swing.JTextField createTextField(int columns) {
        javax.swing.JTextField field = new javax.swing.JTextField(columns) {
            public void paintComponent(Graphics g) {
                // 先画圆角白底再画文字：顺序反了会用白底盖住文字
                paintRoundBackground(this, g);
                super.paintComponent(g);
            }
        };
        return field;
    }

    /**
     * 创建圆角白底密码框：与 createTextField 同一套圆角白底自绘，仅输入内容以圆点显示
     *
     * @param columns 推荐列数（估算宽度用），0 表示不限制
     * @return 圆角白底的密码输入框
     */
    public static javax.swing.JPasswordField createPasswordField(int columns) {
        javax.swing.JPasswordField field = new javax.swing.JPasswordField(columns) {
            public void paintComponent(Graphics g) {
                // 先画圆角白底再画文字：顺序反了会用白底盖住文字
                paintRoundBackground(this, g);
                super.paintComponent(g);
            }
        };
        return field;
    }

    // 画圆角白色底：输入框与密码框共用的底色绘制，圆角半径与 roundBorder 保持一致
    // 参数 c 用于取组件当前宽高（每帧可能变化），g 为组件传入的画布
    private static void paintRoundBackground(javax.swing.JComponent c, Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // 开抗锯齿：圆角边缘平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(WHITE); // 可修改参数：输入框内部底色（白）
        // 圆角半径 10 与 roundBorder 一致（想调整弧度两处一起改）；宽高各内收 1px，底色不溢出圆角描边
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
        g2.dispose();
    }

    /**
     * 为输入框安装焦点边框效果：未选中时浅灰 1px 圆角线，选中时深色 1px 圆角线
     * 圆角线由 roundBorder 自绘（Swing 没有现成的圆角边框），留白直接并入边框 insets
     * 关键前提：必须关闭组件的不透明填充——JTextField 默认的直角白底会盖住圆角外的三角区域，
     * setOpaque(false) 后由组件自绘的圆角白底负责背景（见 createTextField / createPasswordField）
     *
     * @param component 要安装效果的组件（JTextField / JPasswordField 等）
     */
    public static void installFocusBorder(final javax.swing.JComponent component) {
        // 关闭直角白色填充：否则圆角线画在直角白底上，四个角会被白底截断
        component.setOpaque(false);

        // 未选中边框：TEXT_GRAY 浅灰圆角线；聚焦边框：TEXT_DARK 深色圆角线
        // 两者 insets 完全一致（上下 2 / 左右 6），切换时组件尺寸不变，不会引起布局跳动
        final javax.swing.border.Border normalBorder = roundBorder(TEXT_GRAY, 1);
        final javax.swing.border.Border focusBorder = roundBorder(TEXT_DARK, 1);
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

    // 创建圆角描边：匿名 Border 内部类自绘圆角矩形，输入框与导航按钮共用这一套绘制
    // insets 取上下 线宽+1 / 左右 线宽+5：线宽 1 时为上下 2 / 左右 6，线宽 2 时为上下 3 / 左右 7
    public static javax.swing.border.Border roundBorder(final Color color, final int thickness) {
        // 圆角半径（像素）：想调整弧度只改这一个数
        final int arc = 10;
        return new javax.swing.border.Border() {
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                // g.create() 克隆画布，改变抗锯齿等状态后用 dispose 恢复，不污染组件后续绘制
                Graphics2D g2 = (Graphics2D) g.create();
                // 开抗锯齿：不开的话圆角曲线会有明显锯齿
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                // 换粗画笔支持加粗：线画在路径两侧各一半，整体内收半个线宽避免溢出组件边界
                g2.setStroke(new BasicStroke(thickness));
                int half = thickness / 2;
                // 画圆角描边：宽高各减一个线宽，让线完整落在组件边界内
                g2.drawRoundRect(x + half, y + half, width - thickness, height - thickness, arc, arc);
                g2.dispose();
            }
            public Insets getBorderInsets(java.awt.Component c) {
                // 上下留白 1 / 左右留白 5，加上线宽本身，保证光标和文字不贴着圆角线
                return new Insets(thickness + 1, thickness + 5, thickness + 1, thickness + 5);
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

        // 按钮行：居中放一个圆角“确定”按钮（登录类：绿底白字+描边），复用 createRoundButton 工厂，尺寸与登录按钮一致
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 12));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        javax.swing.JButton okButton = createRoundButton("确定", PRIMARY, WHITE);
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

    /**
     * 弹出与系统扁平风格一致的确认对话框：Windows 系统标题栏 + 白底正文 + "确定 / 取消"两个扁平按钮
     * 替代系统自带的 JOptionPane.showConfirmDialog（灰色 Metal 底色，与扁平界面不协调）
     *
     * @param parent  父组件，弹窗在其上方居中显示；传 null 则屏幕居中
     * @param message 提示文字内容
     * @return 点击"确定"返回 true；点击"取消"或点标题栏 X 关闭返回 false
     */
    public static boolean showConfirmDialog(final java.awt.Component parent, String message) {
        // 记录用户选择：只有点了"确定"才置为 true，其余任何关闭方式都视为取消
        final boolean[] confirmed = {false};

        // 保留 Windows 系统自带标题栏（可拖动、可点 X 关闭），只有内容区按扁平风格自绘
        final javax.swing.JDialog dialog = new javax.swing.JDialog();
        // 系统标题栏上显示的文字
        dialog.setTitle("确认");
        // 模态：弹窗关闭前阻塞父窗口操作
        dialog.setModal(true);
        // 确认弹窗不允许拉伸，避免内容区被拉变形
        dialog.setResizable(false);

        // 内容面板：纯白底，与提示弹窗保持一致
        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBackground(WHITE);
        dialog.setContentPane(root);

        // 正文：深灰文字 + 主题普通字体，四周留白让文字不贴窗口边缘
        javax.swing.JLabel messageLabel = new javax.swing.JLabel(message);
        messageLabel.setFont(FONT_NORMAL);
        messageLabel.setForeground(TEXT_DARK);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(22, 16, 22, 16));
        root.add(messageLabel, java.awt.BorderLayout.CENTER);

        // 按钮行：居中放"确定"（登录类：绿底白字）和"取消"（退出类：白底深灰字）两个圆角按钮，样式与登录/退出按钮一致
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 16, 12));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        javax.swing.JButton okButton = createRoundButton("确定", PRIMARY, WHITE);
        javax.swing.JButton cancelButton = createRoundButton("取消", WHITE, TEXT_DARK);
        okButton.setPreferredSize(new java.awt.Dimension(90, 32));
        cancelButton.setPreferredSize(new java.awt.Dimension(90, 32));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        root.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // 点击"确定"：记录选择并关闭弹窗，方法返回 true
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmed[0] = true;
                dialog.dispose();
            }
        });
        // 点击"取消"：直接关闭弹窗，confirmed 保持 false
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        // 宽高按内容自适应，最窄不低于 320 保证长提示不至于拥挤换行
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 320), dialog.getHeight());
        // 在父窗口上方居中显示；parent 为 null 时自动改为屏幕居中
        dialog.setLocationRelativeTo(parent);
        // setVisible(true) 模态阻塞到这里，弹窗关闭后才能把用户的选择返回给调用方
        dialog.setVisible(true);
        return confirmed[0];
    }
}