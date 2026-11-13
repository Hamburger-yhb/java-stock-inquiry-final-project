package harry.stockinqbasic.model;

/**
 * 实时报价：当前价等
 */
public class StockRealtimeQuote {
    private String region;
    private String code;
    private double lastPrice;  // 当前价(ld)
    private double change;     // 涨跌额(ch)
    private double changePct;  // 涨跌幅%(chp)
    private double prevClose;  // 昨收价(计算得出)

    public StockRealtimeQuote() {}

    public StockRealtimeQuote(String region, String code, double lastPrice) {
        this.region = region;
        this.code = code;
        this.lastPrice = lastPrice;
    }

    public StockRealtimeQuote(String region, String code, double lastPrice, double change, double changePct, double prevClose) {
        this.region = region;
        this.code = code;
        this.lastPrice = lastPrice;
        this.change = change;
        this.changePct = changePct;
        this.prevClose = prevClose;
    }

    // Getters and Setters
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePct() {
        return changePct;
    }

    public void setChangePct(double changePct) {
        this.changePct = changePct;
    }

    public double getPrevClose() {
        return prevClose;
    }

    public void setPrevClose(double prevClose) {
        this.prevClose = prevClose;
    }

    @Override
    public String toString() {
        return String.format("%s:%s 当前价: %.2f, 涨跌: %.2f(%.2f%%)", 
            region, code, lastPrice, change, changePct);
    }
}
