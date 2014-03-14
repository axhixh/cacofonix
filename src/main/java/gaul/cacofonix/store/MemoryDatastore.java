package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ashish
 */
public class MemoryDatastore implements Datastore {
    private static final Logger log = LogManager.getLogger("cacofonix.datastore");
    private final Map<Metric, List<DataPoint>> store = new ConcurrentHashMap<>();

    @Override
    public List<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>(store.keySet());
        Collections.sort(metrics);
        return metrics;
    }
    
    @Override
    public void save(String metricName, DataPoint dp) {
        log.info(metricName + '@' + dp.getTimestamp() + 
                        ": " + dp.getValue()); 
        Metric metric = new Metric(metricName, 0,0);
        List<DataPoint> points;
        if (store.containsKey(metric)) {
            points = store.get(metric);
        } else {
            points = new ArrayList<>();
        }
        
        points.add(dp);
        store.put(metric, points);
    }

    @Override
    public List<DataPoint> query(String metricName, long start, long end) {
        Metric metric = new Metric(metricName, 0, 0);
        if (!store.containsKey(metric)) {
            return Collections.emptyList();
        }
        
        List<DataPoint> all = store.get(metric);
        List<DataPoint> filtered = new ArrayList<>();
        
        for (DataPoint p : all) {
            if (p.getTimestamp() >= start && p.getTimestamp() <= end) {
                filtered.add(p);
            }
        }
        
        return filtered;
    }
    
    @Override
    public void delete(String metricName) {
        store.remove(new Metric(metricName, 0, 0));
    }
    
    @Override
    public void close() {
        // do nothing
    }
}
