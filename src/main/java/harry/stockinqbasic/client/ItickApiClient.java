package harry.stockinqbasic.client;

import harry.stockinqbasic.util.SimpleLogger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * iTick API 客户端，封装 HTTP 调用 (Java 8 兼容)
 * 提供股票数据查询、实时报价等功能
 *
 * 支持的市场：
 * - HK: 香港股市
 * - US: 美国股市  
 * - SZ: 深交所
 * - SH: 上交所

 */
public class ItickApiClient {
    
    private static final String BASE_URL = "https://api.itick.org";
    private static final String DEFAULT_TOKEN = "xxx";
    private static final String CONFIG_FILE = System.getProperty("user.home") + "/.stockinq_config";
    
    private static String currentToken = null;
    
    /**
     * 获取当前使用的Token
     */
    public static String getCurrentToken() {
        if (currentToken == null) {
            currentToken = loadTokenFromConfig();
        }
        return currentToken;
    }
    
    /**
     * 设置Token并保存到配置文件
     */
    public static void setToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            SimpleLogger.error("无效的Token，使用默认Token");
            token = DEFAULT_TOKEN;
        }
        
        currentToken = token.trim();
        saveTokenToConfig(currentToken);
        SimpleLogger.info("Token已更新并保存: " + maskToken(currentToken));
    }
    
    /**
     * 从配置文件加载Token
     */
    private static String loadTokenFromConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                String token = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)), StandardCharsets.UTF_8).trim();
                if (!token.isEmpty()) {
                    SimpleLogger.info("从配置文件加载Token: " + maskToken(token));
                    return token;
                }
            }
        } catch (IOException e) {
            SimpleLogger.error("读取配置文件失败: " + e.getMessage());
        }
        
        SimpleLogger.info("使用默认Token: " + maskToken(DEFAULT_TOKEN));
        return DEFAULT_TOKEN;
    }
    
    /**
     * 保存Token到配置文件
     */
    private static void saveTokenToConfig(String token) {
        try {
            Files.write(Paths.get(CONFIG_FILE), token.getBytes(StandardCharsets.UTF_8));
            SimpleLogger.debug("配置已保存到: " + CONFIG_FILE);
        } catch (IOException e) {
            SimpleLogger.error("保存配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 隐藏Token显示（保护隐私）
     */
    private static String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 8) + "***" + token.substring(token.length() - 4);
    }

    /**
     * 测试Token是否有效
     * @param token 要测试的Token
     * @return true如果Token有效，false如果无效
     */
    public static boolean testToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        String oldToken = currentToken;
        try {
            // 临时设置要测试的token
            currentToken = token.trim();
            
            ItickApiClient testClient = new ItickApiClient();
            String response = testClient.get("/symbol/list?type=stock&region=HK&code=700");
            
            if (response != null) {
                // 检查是否包含认证失败的错误码
                if (response.contains("\"code\":401") || response.contains("auth failed")) {
                    SimpleLogger.debug("Token测试失败: 认证失败");
                    return false;
                }
                // 检查是否是正常的API响应（即使没有数据也表示认证成功）
                if (response.contains("\"code\":0") || response.contains("\"data\":")) {
                    SimpleLogger.debug("Token测试成功: 认证通过");
                    return true;
                }
                SimpleLogger.debug("Token测试: 收到响应但格式异常: " + response);
                return false;
            }
            
            return false;
        } catch (Exception e) {
            SimpleLogger.debug("Token测试异常: " + e.getMessage());
            return false;
        } finally {
            // 恢复原来的token
            currentToken = oldToken;
        }
    }

    /**
     * 发送 GET 请求到 iTick API
     * @param pathWithQuery API 路径和查询参数，例如 "/symbol/list?type=stock&region=HK&code=700"
     * @return JSON 响应字符串
     */
    public String get(String pathWithQuery) {
        String fullUrl = BASE_URL + pathWithQuery;
        SimpleLogger.debug("开始HTTP请求: " + fullUrl);
        
        HttpURLConnection connection = null;
        long startTime = System.currentTimeMillis();
        
        try {
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("token", getCurrentToken());
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时
            
            SimpleLogger.debug("HTTP请求配置完成，连接超时: 30秒，读取超时: 30秒");
            
            // 检查响应状态码
            int statusCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            SimpleLogger.debug("HTTP响应状态码: " + statusCode + ", 响应时间: " + responseTime + "ms");
            
            if (statusCode != 200) {
                String errorResponse = readErrorStream(connection);
                SimpleLogger.error("HTTP请求失败: 状态码=" + statusCode + ", 错误响应=" + errorResponse);
                throw new RuntimeException("HTTP error: " + statusCode + " body: " + errorResponse);
            }
            
            // 读取响应内容
            String response = readResponseStream(connection);
            SimpleLogger.debug("HTTP请求成功，响应长度: " + response.length() + " 字符");
            SimpleLogger.debug("HTTP完整响应内容: " + response);

            return response;
            
        } catch (IOException e) {
            long failureTime = System.currentTimeMillis() - startTime;
            SimpleLogger.error("HTTP请求异常: path=" + pathWithQuery + ", 耗时=" + failureTime + "ms, 错误=" + e.getMessage());
            throw new RuntimeException("HTTP request failed: " + pathWithQuery, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
                SimpleLogger.debug("HTTP连接已关闭");
            }
        }
    }
    
    /**
     * 读取正常响应流
     */
    private String readResponseStream(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * 读取错误响应流
     */
    private String readErrorStream(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            return "Failed to read error response";
        }
    }
}