package gaul.cacofonix.reporter;

import gaul.cacofonix.Metric;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import np.com.axhixh.ember.Request;
import np.com.axhixh.ember.Response;
import np.com.axhixh.ember.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ashish
 */
class GetMetricList extends Route {

    private static final Logger log = LogManager.getLogger("cacofonix.reporter");

    private final Datastore store;

    GetMetricList(Datastore store) {
        super("/api/metrics/");
        this.store = store;
    }

    @Override
    public void handle(Request request, Response rspns) {
        log.debug("Returning metrics list");
        try {
            final List<Metric> metrics = store.getMetrics();
            rspns.addHeader("Access-Control-Allow-Origin", "*");
            rspns.stream(new Response.Stream() {

                @Override
                public void write(OutputStream out) throws IOException {
                    for (Metric metric : metrics) {
                        String line = String.format("%s\t%s\t%d\n",
                                metric.getName(), metric.getUnit(),
                                metric.getRetention());
                        out.write(line.getBytes());
                    }
                }
            });
        } catch (DatastoreException err) {
            rspns.error(err);
        }
    }

}
