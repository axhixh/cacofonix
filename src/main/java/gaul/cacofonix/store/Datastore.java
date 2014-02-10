
package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import java.util.List;

/**
 *
 * @author ashish
 */
public interface Datastore {
    List<String> getMetrics();
    void save(String metric, DataPoint dp);
    List<DataPoint> query(String metric, long start, long end);
}
