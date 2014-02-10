
package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import java.util.List;

/**
 *
 * @author ashish
 */
public interface Datastore {
    List<String> getMetrics() throws DatastoreException;
    void save(String metric, DataPoint dp)  throws DatastoreException;
    List<DataPoint> query(String metric, long start, long end)  throws DatastoreException;
    void close();
}
