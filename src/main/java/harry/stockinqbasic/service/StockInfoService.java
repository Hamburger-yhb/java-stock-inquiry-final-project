package harry.stockinqbasic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import harry.stockinqbasic.client.ItickApiClient;
import harry.stockinqbasic.model.StockBasicInfo;
import harry.stockinqbasic.util.SimpleLogger;

/**
 * 股票基础信息服务
 */
public class StockInfoService {

    private final ItickApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockInfoService(ItickApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取单支股票的基础信息：代码、名称、昨收价
     * @param region 区域，如 HK, US, SZ, SH
     * @param code 股票代码，如 "700", "AAPL"
     * @return 股票基础信息
     */
    public StockBasicInfo getStockInfo(String region, String code) {
        SimpleLogger.info("开始获取股票信息: " + region + ":" + code);
        
        String path = String.format(
                "/stock/info?type=stock&region=%s&code=%s", region, code);

        try {
            String json = apiClient.get(path);
            SimpleLogger.debug("获取到股票信息响应，长度: " + (json != null ? json.length() : "null") + " 字符");
            
            if (json == null || json.trim().isEmpty()) {
                throw new RuntimeException("API返回空响应");
            }
            
            SimpleLogger.debug("API响应内容: " + json);

            JsonNode root = objectMapper.readTree(json);
            
            if (root == null) {
                throw new RuntimeException("JSON解析结果为null");
            }
            
            SimpleLogger.debug("JSON根节点: " + root.toString());
            
            JsonNode data = root.get("data");
            
            if (data == null) {
                SimpleLogger.error("JSON中没有找到data字段，完整响应: " + json);
                throw new RuntimeException("API响应中缺少data字段");
            }
            
            SimpleLogger.debug("data节点内容: " + data.toString());

            StockBasicInfo info = new StockBasicInfo();
            info.setRegion(region);
            info.setCode(data.get("c").asText());  // c = 股票代码
            info.setName(data.get("n").asText());   // n = 名称
            // 注意：/stock/info 接口没有昨收价信息，需要从报价接口获取
            info.setPrevClose(0.0); // 占位符，实际值从StockService中的报价数据获取
            
            SimpleLogger.info("股票信息获取成功: " + region + ":" + info.getCode() + " - " + info.getName());

            return info;
        } catch (Exception e) {
            SimpleLogger.error("获取股票基本信息失败: " + region + ":" + code);
            SimpleLogger.error("异常类型: " + (e != null ? e.getClass().getSimpleName() : "null"));
            SimpleLogger.error("异常消息: " + (e != null ? e.getMessage() : "null"));
            if (e != null && e.getCause() != null) {
                SimpleLogger.error("异常原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("parse /stock/info failed for " + region + ":" + code, e);
        }
    }
}
