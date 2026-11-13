package harry.stockinqbasic.service;

import harry.stockinqbasic.model.StockBasicInfo;
import harry.stockinqbasic.model.StockView;
import harry.stockinqbasic.util.SimpleLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 股票搜索+报价综合服务
 */
public class StockSearchWithQuoteService {

    private final StockSearchService searchService;
    private final StockService stockService;

    public StockSearchWithQuoteService(StockSearchService searchService,
                                       StockService stockService) {
        this.searchService = searchService;
        this.stockService = stockService;
    }

    /**
     * 根据代码前缀搜索，并返回每支股票的涨跌信息
     * @param region 区域，如 HK, US, SZ, SH
     * @param codePrefix 股票代码前缀关键字
     * @return 股票展示视图列表
     */
    public List<StockView> searchAndQuote(String region, String codePrefix) {
        SimpleLogger.debug("开始搜索并获取报价: " + region + ":" + codePrefix);
        
        try {
            // 1. 先通过 /symbol/list 搜出候选列表（只知道代码+名称）
            List<StockBasicInfo> basicList =
                    searchService.searchByCodePrefix(region, codePrefix);
                    
            SimpleLogger.debug("搜索到候选股票 " + basicList.size() + " 只");

            // 2. 对每支股票进一步获取报价+涨跌
            List<StockView> views = new ArrayList<>();
            for (StockBasicInfo basic : basicList) {
                try {
                    SimpleLogger.debug("获取股票详细信息: " + region + ":" + basic.getCode());
                    
                    StockView view = stockService.getStockView(region, basic.getCode());
                    views.add(view);
                    
                    SimpleLogger.debug("成功添加股票视图: " + basic.getCode());
                    
                } catch (Exception e) {
                    SimpleLogger.error("获取报价失败: " + basic.getCode() + " - " + e.getMessage());
                    // 单支失败不影响其他，继续处理
                }
            }
            
            SimpleLogger.info("完成搜索并获取报价: 区域=" + region + ", 前缀=" + codePrefix + ", 返回结果=" + views.size() + "个");
                
            return views;
        } catch (Exception e) {
            SimpleLogger.error("搜索+报价失败: " + region + ":" + codePrefix + ", 错误=" + e.getMessage());
            throw new RuntimeException("搜索+报价失败: " + region + ":" + codePrefix, e);
        }
    }
}