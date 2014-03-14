package gaul.cacofonix.reporter;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executors;
import np.com.axhixh.ember.Ember;
import np.com.axhixh.ember.Request;
import np.com.axhixh.ember.Response;
import np.com.axhixh.ember.Route;
import static np.com.axhixh.ember.Ember.delete;
import static np.com.axhixh.ember.Ember.get;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ashish
 */
public class Reporter {

    private static final Logger log = LogManager.getLogger("cacofonix.reporter");
    
    private final String addr;
    private final int port;
    private final Datastore store;

    public Reporter(String addr, int port, Datastore store) {
        this.addr = addr;
        this.port = port;
        this.store = store;
    }

    public void start() throws IOException {
        Ember.setAddr(addr);
        Ember.setPort(port);
        Ember.setExecutor(Executors.newCachedThreadPool());

        get(new Route("/api/metrics/") {

            @Override
            public void handle(Request rqst, Response rspns) {
                log.debug("Returning metrics list");
                try {
                    final List<Metric> metrics = store.getMetrics();
                    rspns.addHeader("Access-Control-Allow-Origin", "*");
                    rspns.stream(new Response.Stream() {

                        @Override
                        public void write(OutputStream out) throws IOException {
                            for (Metric metric : metrics) {
                                String line = metric.getName() + "\n";
                                out.write(line.getBytes());
                            }
                        }
                    });
                } catch (DatastoreException err) {
                    rspns.error(err);
                }
            }
        });

        get(new Route("/api/metrics/:metric") {

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
                    final List<DataPoint> points = store.query(metricName, start, end);
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
        });
        
        delete(new Route("/api/metrics/:metric") {

            @Override
            public void handle(Request request, Response response) {
                String metricName = request.getPathParam(":metric");
                try {
                    store.delete(metricName);
                    response.send(String.format("Metric %s deleted.", metricName));
                } catch (DatastoreException err) {
                    response.error(err);
                }
            }
        });
    }

    public void stop() {
        Ember.stop();
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
}
