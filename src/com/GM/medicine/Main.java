package com.GM.medicine;

// 导入 Color：把输入框的光标颜色设为黑色
import java.awt.Color;
// 导入 SwingUtilities：保证界面在事件分发线程（EDT）中创建，Swing 组件只能在 EDT 上安全操作
import javax.swing.SwingUtilities;
// 导入 UIManager：全局覆盖 Swing LookAndFeel 属性，修改所有 JTextField / JPasswordField 的光标颜色
import javax.swing.UIManager;
// 导入 LoginFrame：程序启动后显示的第一个窗口（登录窗）
import com.GM.medicine.ui.LoginFrame;
// 导入 MainFrame：跳过登录时直接打开的主窗口
import com.GM.medicine.ui.MainFrame;
// 导入 SysUser：测试用户对象类型，主界面靠它知道"是谁登录进来了"
import com.GM.medicine.pojo.entity.SysUser;

/**
 * - 程序入口类
 * - 只做两件事：设置全局光标颜色 → 在事件分发线程中打开登录窗口
 * - 登录成功后由 LoginFrame 负责创建主窗口，本类不做其他业务处理
 */
public class Main {

    /**
     * JVM 启动后第一个被调用的方法
     *
     * @param args 命令行参数，本程序未使用
     */
    public static void main(String[] args) {
        // 【必须】在任何 JTextField / JPasswordField 创建之前设置，否则组件已取了默认值就不会再变
        // 两个 key 分别对应普通文本框和密码框的"光标竖线颜色"，统一设为黑色
        UIManager.put("TextField.caretForeground", Color.BLACK);
        UIManager.put("PasswordField.caretForeground", Color.BLACK);


        // 测试变量：模拟数据库初始账号 admin（角色 = ROLE_ADMIN），配合下方跳过登录直接进主界面
        SysUser textUser = new SysUser();
        textUser.setId(1);
        textUser.setUserName("admin");
        textUser.setRealName("管理员");
        textUser.setRole(SysUser.ROLE_ADMIN);

        // invokeLater 把 Runnable 丢进事件分发线程队列中排队执行
        // Swing 规范：所有 UI 创建 / 更新都必须在 EDT 上，否则多线程下会随机出现 NullPointerException / 组件不刷新等问题
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 创建登录窗口并立即显示；LoginFrame 构造器内部会调 initFrame + initComponents
                //new LoginFrame().setVisible(true);
                new MainFrame(textUser).setVisible(true);
                
            }
        });
    }
}