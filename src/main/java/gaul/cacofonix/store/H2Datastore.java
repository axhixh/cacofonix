package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import java.util.List;

/**
 *
 * @author ashish
 */
public class H2Datastore implements Datastore {

    @Override
    public List<String> getMetrics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void save(String metric, DataPoint dp) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public List<DataPoint> query(String metric, long start, long end) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
