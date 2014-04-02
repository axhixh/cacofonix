
package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashish
 */
public class Delta implements GetDataPoints.Function {

    @Override
    public List<DataPoint> execute(List<DataPoint> input, GetDataPoints.Context context) {
        if (input.size() < 2) {
            return input;
        }
        
        List<DataPoint> result = new ArrayList<>(input.size() - 1);
        
        DataPoint prev = input.get(0);
        for (int i = 1; i < input.size(); i++) {
            DataPoint current = input.get(i);
            result.add(new DataPoint(current.getTimestamp(), current.getValue() - prev.getValue()));
            prev = current;
        }
        
        return result;
    }
    
}
