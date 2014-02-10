package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ashish
 */
public class MemoryDatastore implements Datastore {

    private final Map<String, List<DataPoint>> store = new ConcurrentHashMap<>();

    @Override
    public List<String> getMetrics() {
        List<String> metrics = new ArrayList<>(store.keySet());
        Collections.sort(metrics);
        return metrics;
    }
    
    @Override
    public void save(String metric, DataPoint dp) {
        System.out.println(metric + '@' + dp.getTimestamp() + 
                        ": " + dp.getValue()); 
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
    public List<DataPoint> query(String metric, long start, long end) {
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
    public void close() {
        // do nothing
    }
}
