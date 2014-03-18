package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author ashish
 */
public class CountByInterval implements GetDataPoints.Function {

    @Override
    public List<DataPoint> execute(List<DataPoint> input, GetDataPoints.Context context) {
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        String sInterval = context.getParam("interval");
        int interval;
        try {
            interval = (sInterval != null) ? Integer.parseInt(sInterval) : -1;

        } catch (NumberFormatException ignored) {
            interval = -1;
        }

        if (interval == -1) {
            int value = input.size();
            long start = input.get(0).getTimestamp();

            return Arrays.asList(new DataPoint(start, value));
        }

        int intervalMillis = interval * 1000;
        int counter = 0;
        long modTime = 0;

        List<DataPoint> result = new ArrayList<>();
        for (DataPoint dp : input) {
            long timestamp = dp.getTimestamp();
            if (timestamp / intervalMillis == modTime) {
                counter++;
            } else {
                if (modTime != 0) {
                    result.add(new DataPoint(modTime * intervalMillis, counter));
                }
                counter = 1;
                modTime = timestamp / intervalMillis;
            }
        }
        if (modTime != 0) {
            result.add(new DataPoint(modTime * intervalMillis, counter));
        }
        return result;
    }
}
