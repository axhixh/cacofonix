package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import np.com.axhixh.ember.Request;
import np.com.axhixh.ember.Response;
import np.com.axhixh.ember.Route;

/**
 *
 * @author ashish
 */
class GetDataPoints extends Route {

    private final Datastore store;
    private final Function function;

    GetDataPoints(String path, Datastore store, Function func) {
        super(path);
        this.store = store;
        this.function = func;
    }

    @Override
    public void handle(Request rqst, Response rspns) {
        String metricName = rqst.getPathParam(":metric");
        long start = toNumber(rqst.getQueryParam("start"));
        if (start < 0) {
            start = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L;
        }
        long end = toNumber(rqst.getQueryParam("end"));
        if (end < 0) {
            end = System.currentTimeMillis();
        }

        try {
            final List<DataPoint> points = function.execute(
                    store.query(metricName, start, end), new DefaultContext(rqst));
            rspns.addHeader("X-Metric-Name", metricName);
            rspns.addHeader("Access-Control-Allow-Origin", "*");
            rspns.stream(new Response.Stream() {

                @Override
                public void write(OutputStream out) throws IOException {
                    out.write("time\tvalue\n".getBytes());
                    for (DataPoint point : points) {
                        String line = point.getTimestamp() + "\t"
                                + point.getValue() + "\n";
                        out.write(line.getBytes());
                    }
                }
            });
        } catch (DatastoreException err) {
            rspns.error(err);
        }
    }

    private long toNumber(String val) {
        if (val == null) {
            return -1;
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException err) {
            return -1;
        }
    }

    public static interface Function {

        List<DataPoint> execute(List<DataPoint> input, Context context);
    }
    
    public static interface Context {
        String getParam(String name);
    }
    
    private static class DefaultContext implements Context {
        private final Request request;
        
        DefaultContext(Request request) {
            this.request = request;
        }
        
        @Override
        public String getParam(String name) {
            return request.getQueryParam(name);
        }
    }
}
