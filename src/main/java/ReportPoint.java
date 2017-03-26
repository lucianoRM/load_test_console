/**
 * Created by luciano on 26/03/17.
 */
public class ReportPoint {

    private long min;
    private long max;
    private long avg;
    private long tot;

    public ReportPoint() {
        this.max = 0;
        this.min = 0;
        this.avg = 0;
        this.tot = 0;
    }

    public void update(long newValue) {
        this.tot++;
        if(newValue < this.min) {
            this.min = newValue;
        }
        if(newValue > this.max) {
            this.max = newValue;
        }
        this.avg = ((this.avg * (this.tot - 1)) + newValue)/this.tot;
    }

    public long getMin() {
        return this.min;
    }

    public long getMax() {
        return this.max;
    }

    public long getAvg() {
        return this.avg;
    }

    public long getTot() {
        return this.tot;
    }


}
