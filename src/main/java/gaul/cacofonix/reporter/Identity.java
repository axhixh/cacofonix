package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import java.util.List;

/**
 *
 * @author ashish
 */
class Identity implements GetDataPoints.Function {

    @Override
    public List<DataPoint> execute(List<DataPoint> input, GetDataPoints.Context context) {
        return input;
    }

}
