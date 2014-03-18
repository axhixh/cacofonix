
package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.reporter.GetDataPoints.Context;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ashish
 */
public class CountByIntervalTest {
    
    @Test
    public void testNoInterval() {
        CountByInterval op = new CountByInterval();
        List<DataPoint> series = Arrays.asList(new DataPoint(10, 34), new DataPoint(20, 50), 
                new DataPoint(25, 23));
        List<DataPoint> result = op.execute(series, new Context(){

            @Override
            public String getParam(String name) {
                return null;
            }
        });
        
       assertEquals(1, result.size());
       assertEquals(series.size(), (long)result.get(0).getValue());
    }
    
    @Test
    public void testEmptySeries() {
        CountByInterval op = new CountByInterval();
        List<DataPoint> series = Collections.emptyList();
        List<DataPoint> result = op.execute(series, new Context(){

            @Override
            public String getParam(String name) {
                return "10";
            }
        });
        
       assertEquals(0, result.size());
    }
   
    @Test
    public void testTimeSeries() {
        CountByInterval op = new CountByInterval();
        List<DataPoint> series = Arrays.asList(
                new DataPoint(1000, 23),
                new DataPoint(1200, 23),
                new DataPoint(2000, 1),
                new DataPoint(2200, 2),
                new DataPoint(2600, 2),
                new DataPoint(3100, 5)
        );
        List<DataPoint> result = op.execute(series, new Context(){

            @Override
            public String getParam(String name) {
                return "1";
            }
        });
        
       assertEquals(3, result.size());
       assertEquals(2, (long)result.get(0).getValue());
       assertEquals(3, (long)result.get(1).getValue());
       assertEquals(1, (long)result.get(2).getValue());
       
       assertEquals(1000, result.get(0).getTimestamp());
       assertEquals(3000, result.get(2).getTimestamp());
    }
}
