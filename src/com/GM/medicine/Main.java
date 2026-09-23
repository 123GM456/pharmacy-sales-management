package com.GM.medicine;

// 导入 SwingUtilities：保证界面在事件分发线程中创建，避免 Swing 组件的线程安全问题
import javax.swing.SwingUtilities;
// 导入 LoginFrame：程序启动后显示的第一个窗口
import com.GM.medicine.ui.LoginFrame;

/**
 * - 程序入口类
 * - 负责启动图形界面，在事件分发线程中打开登录窗口
 * - 登录成功后由 LoginFrame 负责创建主窗口，本类不做其他业务处理
 */
public class Main {

    // 程序入口方法，启动登录窗口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}