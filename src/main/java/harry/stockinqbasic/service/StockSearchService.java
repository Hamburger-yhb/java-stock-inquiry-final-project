package harry.stockinqbasic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import harry.stockinqbasic.client.ItickApiClient;
import harry.stockinqbasic.model.StockBasicInfo;
import harry.stockinqbasic.util.SimpleLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 股票搜索服务
 */
public class StockSearchService {

    private final ItickApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockSearchService(ItickApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 搜索股票列表（根据代码前缀），只返回代码+名称
     * @param region 区域，如 HK, US, SZ, SH
     * @param codePrefix 股票代码前缀，如 "700", "AAPL"
     * @return 匹配的股票基础信息列表
     */
    public List<StockBasicInfo> searchByCodePrefix(String region, String codePrefix) {
        SimpleLogger.info("开始搜索股票: region=" + region + ", codePrefix=" + codePrefix);
        
        // 构造 URL 参数
        String path = String.format("/symbol/list?type=stock&region=%s&code=%s",
                region, codePrefix);

        try {
            // 调用接口
            String json = apiClient.get(path);
            SimpleLogger.debug("获取到搜索响应，开始解析JSON");

            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");

            List<StockBasicInfo> result = new ArrayList<>();
            if (data != null && data.isArray()) {
                SimpleLogger.debug("找到 " + data.size() + " 条搜索结果");
                for (JsonNode item : data) {
                    StockBasicInfo info = new StockBasicInfo();
                    info.setRegion(region);
                    info.setCode(item.get("c").asText()); // c = 股票代码
                    info.setName(item.get("n").asText()); // n = 名称
                    info.setPrevClose(0.0); // 此接口没有昨收价，后面再查 /stock/info
                    result.add(info);
                    SimpleLogger.debug("添加股票: " + region + ":" + info.getCode() + " - " + info.getName());
                }
            } else {
                SimpleLogger.debug("未找到搜索结果或数据格式异常");
            }
            
            SimpleLogger.info("股票搜索完成: region=" + region + ", codePrefix=" + codePrefix + ", 结果数量=" + result.size());
            return result;

        } catch (Exception e) {
            SimpleLogger.error("股票搜索失败: region=" + region + ", codePrefix=" + codePrefix + ", 错误=" + e.getMessage());
            throw new RuntimeException("parse /symbol/list failed for " + region + ":" + codePrefix, e);
        }
    }
}
