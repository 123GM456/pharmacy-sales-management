package com.GM.medicine.thread;

// 导入 List：接收 Service 返回的库存预警药品集合
import java.util.List;
// 导入 SwingUtilities：把界面更新任务托付给事件分发线程（EDT），后台线程不得直接碰 Swing 组件
import javax.swing.SwingUtilities;
// 导入 Medicine：库存预警药品实体，可读取名称、库存、预警值等明细
import com.GM.medicine.pojo.entity.Medicine;
// 导入 MedicineService：查询预警药品统一通过它，线程不直接访问 DAO 与 SQL
import com.GM.medicine.service.MedicineService;
// 导入 MainFrame：保存主窗口引用，预警弹窗中用户点击"查看"后通过它跳转到药品管理页
import com.GM.medicine.ui.MainFrame;

/**
 * - 库存预警后台线程
 * - 由 MainFrame 登录进入主窗口后创建并启动，每隔固定时间通过 MedicineService 查询库存预警药品
 * - 查到预警药品时输出控制台明细，并经 EDT 显示主窗口导航栏的"库存预警"入口，点击可跳转预警药品页
 * - 不写 SQL、不直接操作 Swing 组件；导航栏入口的显隐更新封装在 invokeLater 任务中
 * - 退出登录时由 MainFrame 调用 stopThread 停止
 */
public class StockWarningThread extends Thread {

    // 运行标志：stopThread 置 false 后循环在下一轮判断处退出
    // volatile 保证主窗口线程的修改立即对本线程可见，否则可能一直读到旧值停不下来
    private volatile boolean running = true;

    // 检查间隔：每轮查询之间的睡眠毫秒数
    private final int checkInterval = 60000; // 【可修改参数】库存预警检查间隔（60 秒）

    // 药品业务对象：查询预警药品的唯一入口，其方法只用局部变量，多线程调用安全
    private MedicineService medicineService = new MedicineService();

    // 主窗口引用：有预警时经它显示导航栏"库存预警"入口，用户点击入口后跳转到预警药品页
    private final MainFrame mainFrame;

    /**
     * 创建库存预警线程：保存主窗口引用，供显示/隐藏导航栏"库存预警"入口使用
     *
     * @param mainFrame 主窗口，线程由它创建并管理生命周期
     */
    public StockWarningThread(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    
    /**
     * 线程运行方法，实现库存预警逻辑
     */ 
    @Override
    public void run() {
        // 每轮开始前检查停止标志：stopThread 置 false 后在这里退出循环
        while (running) {
            // 输出运行日志，便于在控制台观察线程是否存活
            System.out.println("库存预警线程运行中...");
            // 通过 Service 查询当前库存预警药品（SQL 规则：在售、未过期、库存 ≤ 预警值）
            List<Medicine> warningMedicines = medicineService.findWarningMedicines();
            // 本轮是否存在预警药品
            boolean hasWarning = !warningMedicines.isEmpty();
            // 存在预警：拼接明细输出到控制台，供日志查看本轮检查结果
            if (hasWarning) {
                // 拼接提示明细：药品名（库存 x，预警值 y），数据在后台线程拼好，不碰 Swing 组件
                StringBuilder message = new StringBuilder("以下药品库存不足，请及时补货：");
                for (Medicine medicine : warningMedicines) {
                    message.append("\n").append(medicine.getName())
                            .append("（库存 ").append(medicine.getStock())
                            .append("，预警值 ").append(medicine.getWarningStock()).append("）");
                }
                System.out.println("库存预警：" + message);
            }
            // 经 EDT 更新导航栏"库存预警"入口：有预警显示、无预警隐藏；后台线程不得直接碰 Swing 组件
            // 每轮都按本轮结果设置一次即可，setVisible 重复调用是幂等的
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mainFrame.showStockWarning(hasWarning);
                }
            });
            // 睡眠一个间隔再进入下一轮："定时"靠循环 + 睡眠实现，sleep 让出 CPU 不空转
            try{
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                // stopThread 的 interrupt 会打断 sleep 走到这里：视为停止信号，恢复中断状态并退出循环
                Thread.currentThread().interrupt();
                break;
            }
        }       
    }

    /**
     * 停止线程：由 MainFrame 在退出登录等会话结束时机调用
     * 置 running 让循环条件失效，再 interrupt 立即打断睡眠中的等待，两步配合保证快速停止
     */
    public void stopThread() {
        running = false;
        interrupt();
    }
}
