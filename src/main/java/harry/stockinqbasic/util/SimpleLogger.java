package harry.stockinqbasic.util;

/**
 * 简化的日志工具类，支持INFO/DEBUG/ERROR级别
 * 使用全局单例模式，所有输入都是字符串
 */
public class SimpleLogger {
    
    // 日志开关
    private static boolean debugEnabled = false;
    private static boolean infoEnabled = true;
    private static boolean errorEnabled = true;
    
    /**
     * 开启/关闭DEBUG日志
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }
    
    /**
     * 开启/关闭INFO日志
     */
    public static void setInfoEnabled(boolean enabled) {
        infoEnabled = enabled;
    }
    
    /**
     * 开启/关闭ERROR日志
     */
    public static void setErrorEnabled(boolean enabled) {
        errorEnabled = enabled;
    }
    
    /**
     * DEBUG级别日志
     */
    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    /**
     * INFO级别日志
     */
    public static void info(String message) {
        if (infoEnabled) {
            System.out.println("[INFO] " + message);
        }
    }
    
    /**
     * ERROR级别日志
     */
    public static void error(String message) {
        if (errorEnabled) {
            System.out.println("[ERROR] " + message);
        }
    }
}