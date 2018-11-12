package callLog.domain;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/9 21:01
 */
public class CallLogDateRange {

    private String startRow;
    private String endRow;

    public String getStartRow() {
        return startRow;
    }

    public void setStartRow(String startRow) {
        this.startRow = startRow;
    }

    public String getEndRow() {
        return endRow;
    }

    public void setEndRow(String endRow) {
        this.endRow = endRow;
    }

    @Override
    public String toString() {
        return "CallLogDateRange{" +
                "startRow='" + startRow + '\'' +
                ", endRow='" + endRow + '\'' +
                '}';
    }
}
