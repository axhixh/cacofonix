
package gaul.cacofonix;

/**
 *
 * @author ashish
 */
public class DataPoint {
    private final long timestamp;
    private final double value;

    public DataPoint(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DataPoint{" + "timestamp=" + timestamp + ", value=" + value + '}';
    }
    
    
}
