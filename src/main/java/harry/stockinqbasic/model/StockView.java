package harry.stockinqbasic.model;

/**
 * 最终给 UI 展示的视图，包括涨跌
 */
public class StockView {
    private String region;
    private String code;
    private String name;
    private double lastPrice;   // 当前价
    private double change;      // 涨跌额
    private double changePct;   // 涨跌幅(百分数，比如 1.23 表示 +1.23%)
    private double prevClose;   // 昨收价

    public StockView() {}

    public StockView(String region, String code, String name, 
                    double lastPrice, double change, double changePct, double prevClose) {
        this.region = region;
        this.code = code;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isUp() {
        return change > 0;
    }

    public boolean isDown() {
        return change < 0;
    }

    public boolean isFlat() {
        return Math.abs(change) < 0.001; // 避免浮点数精度问题
    }

    /**
     * 校验涨跌数据是否一致
     * @return 验结果和详细信息
     */
    public ValidationResult validateChangeData() {
        if (prevClose <= 0) {
            return new ValidationResult(false, "昨收价为0或负数，无法校验");
        }

        // 计算期望的涨跌额和涨跌幅
        double expectedChange = lastPrice - prevClose;
        double expectedChangePct = (expectedChange / prevClose) * 100.0;

        // 容差值，考虑浮点数精度问题
        double changeTolerance = 0.001;
        double pctTolerance = 0.01; // 0.01%

        boolean changeValid = Math.abs(change - expectedChange) <= changeTolerance;
        boolean pctValid = Math.abs(changePct - expectedChangePct) <= pctTolerance;

        if (changeValid && pctValid) {
            return new ValidationResult(true, "数据校验成功");
        } else {
            String message = String.format(
                "数据校验失败: 当前价=%.2f, 昨收=%.2f | " +
                "涨跌额: API=%.2f, 计算=%.2f, 差值=%.3f | " +
                "涨跌幅: API=%.2f%%, 计算=%.2f%%, 差值=%.3f%%",
                lastPrice, prevClose,
                change, expectedChange, Math.abs(change - expectedChange),
                changePct, expectedChangePct, Math.abs(changePct - expectedChangePct)
            );
            return new ValidationResult(false, message);
        }
    }

    /**
     * 校验结果内部类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s %s 当前价: %.2f 昨收: %.2f 涨跌: %.2f (%.2f%%)",
                region, code, name, lastPrice, prevClose, change, changePct);
    }
}
