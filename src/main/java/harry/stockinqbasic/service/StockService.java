package harry.stockinqbasic.service;

import harry.stockinqbasic.model.StockBasicInfo;
import harry.stockinqbasic.model.StockRealtimeQuote;
import harry.stockinqbasic.model.StockView;
import harry.stockinqbasic.util.SimpleLogger;

/**
 * 股票综合服务，整合基础信息和实时报价，计算涨跌
 * 支持多市场股票数据获取和分析
 *
 */
public class StockService {

    private final StockInfoService infoService;
    private final StockQuoteService quoteService;

    public StockService(StockInfoService infoService, StockQuoteService quoteService) {
        this.infoService = infoService;
        this.quoteService = quoteService;
    }

    /**
     * 获取单支股票的完整展示信息：代码、名称、当前价、涨跌额、涨跌幅
     * @param region 区域
     * @param code 股票代码
     * @return 股票展示视图
     */
    public StockView getStockView(String region, String code) {
        SimpleLogger.debug("开始获取股票综合信息: " + region + ":" + code);
        
        try {
            // 1. 获取基础信息（含昨收价）
            StockBasicInfo info = infoService.getStockInfo(region, code);
            SimpleLogger.debug("已获取基础信息: 名称=" + info.getName());

            // 2. 获取当前价
            StockRealtimeQuote quote = quoteService.getRealtimeQuote(region, code);
            SimpleLogger.debug("已获取实时报价: 当前价=" + quote.getLastPrice() + 
                ", 涨跌额=" + quote.getChange() + 
                ", 涨跌幅=" + quote.getChangePct() + "%");

            // 3. 使用API提供的涨跌信息（不需要重新计算）
            double lastPrice = quote.getLastPrice();
            double change = quote.getChange();
            double changePct = quote.getChangePct();
            double prevClose = quote.getPrevClose();
            
            SimpleLogger.debug("使用API涨跌数据：当前价=" + lastPrice + ", 涨跌额=" + change + ", 涨跌幅=" + changePct + "%, 昨收价=" + prevClose);

            // 4. 组装视图对象
            StockView view = new StockView();
            view.setRegion(info.getRegion());
            view.setCode(info.getCode());
            view.setName(info.getName());
            view.setLastPrice(lastPrice);
            view.setChange(change);
            view.setChangePct(changePct);
            view.setPrevClose(prevClose); // 使用API提供或计算的昨收价
            
            // 5. 校验数据一致性
            StockView.ValidationResult validation = view.validateChangeData();
            if (validation.isValid()) {
                SimpleLogger.debug("数据校验成功: " + validation.getMessage());
            } else {
                SimpleLogger.error("数据校验失败: " + validation.getMessage());
            }

            SimpleLogger.info("成功获取股票综合信息: " + region + ":" + code + ", 名称=" + info.getName() + ", 当前价=" + lastPrice + ", 涨跌幅=" + changePct + "%, 昨收=" + prevClose);
                
            return view;
        } catch (Exception e) {
            SimpleLogger.error("获取股票信息失败: " + region + ":" + code + ", 错误=" + e.getMessage());
            throw new RuntimeException("获取股票信息失败: " + region + ":" + code, e);
        }
    }
}
