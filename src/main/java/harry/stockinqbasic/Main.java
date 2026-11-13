package harry.stockinqbasic;

import harry.stockinqbasic.ui.MainFrame;
import harry.stockinqbasic.util.SimpleLogger;

import javax.swing.*;

/**
 * Harry股票查询系统主程序
 * 
 * 使用前请确保：
 * 1. 在 ItickApiClient 类中设置正确的 TOKEN
 * 2. 网络连接正常
 */
public class Main {
    public static void main(String[] args) {
        // 开启DEBUG日志以便调试
        SimpleLogger.setDebugEnabled(true);
        
        SimpleLogger.info("Harry股票查询系统开始启动...");
        SimpleLogger.info("系统信息: Java=" + System.getProperty("java.version") + ", OS=" + System.getProperty("os.name"));
        
        // 设置系统外观
        try {
            // Java 8兼容的方式设置外观
            String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLookAndFeel);
            SimpleLogger.debug("已设置系统外观: " + systemLookAndFeel);
        } catch (Exception e) {
            // 使用默认外观
            SimpleLogger.error("无法设置系统外观，使用默认外观: " + e.getMessage());
        }
        
        // 启动Swing界面
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleLogger.debug("在EDT线程中初始化界面...");
                    new MainFrame().setVisible(true);
                    SimpleLogger.info("程序启动成功！");
                } catch (Exception e) {
                    SimpleLogger.error("程序启动失败: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "程序启动失败: " + e.getMessage(), 
                        "启动错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}