
package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author ashish
 */
public class H2DatastoreTest {
    
    H2Datastore store;
    
    @Before
    public void setUp() {
        store = new H2Datastore("jdbc:h2:mem:cacofonix");
    }
    
    @After
    public void tearDown() {
        store.close();
    }
    
    @Test
    public void testCreateAndList() throws Exception {
        List<Metric> initial = store.getMetrics();
        assertEquals(0, initial.size());
        
        Metric m666 = new Metric("m666", "count", 60000, 350000);
        store.createOrUpdate(m666);
        
        Metric m42 = new Metric("m42", "%", 5, 5);
        store.createOrUpdate(m42);
        
        List<Metric> updated = store.getMetrics();
        assertEquals(2, updated.size());
        
        // verify it is sorted
        assertEquals("m42", updated.get(0).getName());
    }
    
    @Test
    public void testDelete() throws Exception {
        Metric m666 = new Metric("m666", "count", 60000, 350000);
        store.createOrUpdate(m666);
        
        Metric m42 = new Metric("m42", "%", 5, 5);
        store.createOrUpdate(m42);
        
        List<Metric> initial = store.getMetrics();
        assertEquals(2, initial.size());
        
        store.delete("m666");
        List<Metric> updated = store.getMetrics();
        assertEquals(1, updated.size());
        
        for (Metric m: updated) {
            assertNotEquals("m666", m.getName());
        }        
    }
    
    @Test
    public void testDeleteNonExisting() {
        try {
            store.delete("not-existing-metric");
        } catch (DatastoreException err) {
            fail("Should not throw exception");
        }
    }
    
    @Test
    public void testDeleteMetricWithDataPoint() throws Exception {
        long now = System.currentTimeMillis();
        store.save("m42", new DataPoint(now, 7));
        
        List<Metric> metrics = store.getMetrics();
        assertEquals(1, metrics.size());
        
        store.delete("m42");
        assertTrue(store.getMetrics().isEmpty());
    }
    
    @Test
    public void testDataPointsForExistingMetric() throws Exception {
        Metric m42 = new Metric("m42", "%", 5, 50000);
        store.createOrUpdate(m42);
        
        long now = System.currentTimeMillis();
        long hourAgo = now - (3600 * 1000);
        store.save("m42", new DataPoint(hourAgo, 5));
        store.save("m42", new DataPoint(now, 7));
        List<DataPoint> fullResult = store.query("m42", hourAgo, now);
        assertEquals(2, fullResult.size());
        List<DataPoint> partialResult = store.query("m42", now - 60000, now);
        assertEquals(1, partialResult.size());
        List<DataPoint> emptyResult = store.query("m42", hourAgo - 60000, hourAgo - 5000);
        assertTrue(emptyResult.isEmpty());
    }
    
    @Test
    public void testDataPointsForNonExistingMetric() throws Exception {
        long now = System.currentTimeMillis();
        List<DataPoint> result = store.query("non-existing", now - 1000, now + 1000);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testDataPointsForAutoCreatedMetric() throws Exception {
        long now = System.currentTimeMillis();
        store.save("m42", new DataPoint(now, 7));
        
        List<Metric> metrics = store.getMetrics();
        assertEquals(1, metrics.size());
        
        List<DataPoint> result = store.query("m42", now - 60000, now);
        assertEquals(1, result.size());
    }
}
