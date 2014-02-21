package gaul.cacofonix.reporter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import static com.sun.net.httpserver.HttpServer.create;
import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ashish
 */
public class Reporter {
    private static final Logger log = LogManager.getLogger("cacofonix.reporter");
    private static final String METRICS_PATH = "/api/metrics/";
    private final String addr;
    private final int port;
    private final Datastore store;
    private HttpServer server;

    public Reporter(String addr, int port, Datastore store) {
        this.addr = addr;
        this.port = port;
        this.store = store;
    }

    public void start() throws IOException {
        server = create(new InetSocketAddress(addr, port), 5);
        server.createContext(METRICS_PATH, new QueryHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public void stop() {
        server.stop(10);
    }

    private class QueryHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange he) throws IOException {
            URI request = he.getRequestURI();
            log.debug("Reporter query at " + request.toString());
            String path = request.getPath();
            if (METRICS_PATH.equalsIgnoreCase(path)) {
                getMetrics(he);
            } else {
                getDataPoints(path, request, he);
            }
        }

        private void getMetrics(HttpExchange he) throws IOException {
            log.debug("Returning metrics list");
            try {
                List<Metric> metrics = store.getMetrics();
                he.sendResponseHeaders(200, 0);
                try (OutputStream os = he.getResponseBody()) {
                    for (Metric metric : metrics) {
                        String line = metric.getName() + "\n";
                        os.write(line.getBytes());
                    }
                }
            } catch (DatastoreException err) {
                handleError(err, he);
            }
        }

        private void getDataPoints(String path, URI request, HttpExchange he) throws IOException {
            log.debug("Returning data points");
            String metricName = path.substring(METRICS_PATH.length());
            String query = request.getQuery();
            long start = get("start", query);
            long end = get("end", query);
            try {
                List<DataPoint> points = store.query(metricName, start, end);
                Headers responseHeaders = he.getResponseHeaders();
                responseHeaders.set("X-Metric-Name", metricName);
                //responseHeaders.set("Content-Type", "text/tab-separated-values");
                //responseHeaders.set("Content-Disposition",
                //        "attachment; filename=" + metricName + ".tsv");
                he.sendResponseHeaders(200, 0); // use chunked
                try (OutputStream os = he.getResponseBody()) {
                    os.write("time\tvalue\n".getBytes());
                    for (DataPoint point : points) {
                        String line = point.getTimestamp() + "\t"
                                + point.getValue() + "\n";
                        os.write(line.getBytes());
                    }
                }
            } catch (DatastoreException err) {
                handleError(err, he);
            }
        }

        private long get(String param, String query) {
            String p = param + '=';
            int index = query.indexOf(p);
            if (index < 0) {
                return -1;
            }

            int start = index + p.length();
            int end = query.indexOf('&', start);
            String t = end < 0 ? query.substring(start)
                    : query.substring(start, end);

            try {
                return Long.parseLong(t);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        private void handleError(Exception err, HttpExchange he) throws IOException {
            String msg = err.getMessage();
            if (msg == null) {
                msg = "Unknown error";
            }
            byte[] data = msg.getBytes();
            he.sendResponseHeaders(500, data.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(data);
            }
        }

    }
}
