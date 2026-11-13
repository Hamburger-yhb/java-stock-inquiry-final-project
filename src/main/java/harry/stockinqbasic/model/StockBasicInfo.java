package harry.stockinqbasic.model;

/**
 * 股票基础信息：代码、名称、昨收价
 */
public class StockBasicInfo {
    private String region;    // HK, US, SZ...
    private String code;      // 700, AAPL, 000001...
    private String name;      // 公司名称
    private double prevClose; // 昨收价(pc)

    public StockBasicInfo() {}

    public StockBasicInfo(String region, String code, String name, double prevClose) {
        this.region = region;
        this.code = code;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrevClose() {
        return prevClose;
    }

    public void setPrevClose(double prevClose) {
        this.prevClose = prevClose;
    }

    @Override
    public String toString() {
        return String.format("%s:%s %s (昨收价: %.2f)", region, code, name, prevClose);
    }
}
