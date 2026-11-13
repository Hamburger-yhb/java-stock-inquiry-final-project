package harry.stockinqbasic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import harry.stockinqbasic.client.ItickApiClient;
import harry.stockinqbasic.model.StockRealtimeQuote;
import harry.stockinqbasic.util.SimpleLogger;

/**
 * 股票实时报价服务
 */
public class StockQuoteService {

    private final ItickApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockQuoteService(ItickApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取单支股票当前价
     * @param region 区域，如 HK, US, SZ, SH
     * @param code 股票代码，如 "700", "AAPL"
     * @return 实时报价信息
     */
    public StockRealtimeQuote getRealtimeQuote(String region, String code) {
        SimpleLogger.debug("开始获取实时报价: " + region + ":" + code);
        
        String path = String.format("/stock/quote?region=%s&code=%s",
                region, code);

        try {
            String json = apiClient.get(path);
            SimpleLogger.debug("获取到报价响应，长度: " + (json != null ? json.length() : "null") + " 字符");
            SimpleLogger.debug("报价API响应内容: " + json);

            JsonNode root = objectMapper.readTree(json);
            SimpleLogger.debug("报价JSON根节点: " + root.toString());
            
            JsonNode data = root.get("data");
            if (data == null) {
                SimpleLogger.error("报价JSON中没有找到data字段，完整响应: " + json);
                throw new RuntimeException("API响应中缺少data字段");
            }
            
            SimpleLogger.debug("报价data节点内容: " + data.toString());

            StockRealtimeQuote quote = new StockRealtimeQuote();
            quote.setRegion(region);
            quote.setCode(data.get("s").asText());  // s = symbol code
            quote.setLastPrice(data.get("ld").asDouble()); // ld = 当前价
            
            // 从 API 获取涨跌信息
            double change = data.has("ch") ? data.get("ch").asDouble() : 0.0; // ch = 涨跌额
            double changePct = data.has("chp") ? data.get("chp").asDouble() : 0.0; // chp = 涨跌幅(%)
            
            // 查找API是否直接提供昨收价
            double prevClose;
            if (data.has("pc")) {
                // 如果API直接提供昨收价
                prevClose = data.get("pc").asDouble();
                SimpleLogger.debug("API直接提供昨收价: " + prevClose);
            } else if (data.has("o")) {
                // 尝试使用开盘价作为参考(不精确)
                prevClose = data.get("o").asDouble();
                SimpleLogger.debug("使用开盘价作为昨收价参考: " + prevClose);
            } else {
                // 计算昨收价: 昨收价 = 当前价 - 涨跌额
                prevClose = quote.getLastPrice() - change;
                SimpleLogger.debug("计算昨收价: " + prevClose + " (当前价 - 涨跌额)");
            }
            
            quote.setChange(change);
            quote.setChangePct(changePct);
            quote.setPrevClose(prevClose);
            
            SimpleLogger.debug("实时报价获取成功: " + region + ":" + quote.getCode() + 
                ", 当前价: " + quote.getLastPrice() + 
                ", 涨跌额: " + change + 
                ", 涨跌幅: " + changePct + "%" + 
                ", 昨收价: " + prevClose);

            return quote;
        } catch (Exception e) {
            SimpleLogger.error("获取实时报价失败: " + region + ":" + code + ", 错误=" + e.getMessage());
            throw new RuntimeException("parse /stock/quote failed for " + region + ":" + code, e);
        }
    }
}
