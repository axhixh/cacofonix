
package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import java.util.List;

/**
 *
 * @author ashish
 */
public interface Datastore {
    List<Metric> getMetrics() throws DatastoreException;
    void save(String metricName, DataPoint dp)  throws DatastoreException;
    List<DataPoint> query(String metricName, long start, long end)  throws DatastoreException;
    void delete(String metricName) throws DatastoreException;
    void close();
}
