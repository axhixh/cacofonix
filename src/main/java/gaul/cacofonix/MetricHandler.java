
package gaul.cacofonix;

import gaul.cacofonix.store.Datastore;

/**
 *
 * @author ashish
 */
public class MetricHandler implements PacketHandler {
    private final Datastore store;
    
    public MetricHandler(Datastore store) {
        this.store = store;
    }
    
    @Override
    public void handle(String line) {
        String[] split = line.split(" ");
        if (split.length >= 3) {
            try {
                String metricName = split[0];
                long timestamp = Long.parseLong(split[1]);
                double value = Double.parseDouble(split[2]);
                store.save(metricName, new DataPoint(timestamp, value));
            } catch (NumberFormatException err) {
                
            }
        }
    }
}
