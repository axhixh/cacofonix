package gaul.cacofonix.reporter;

import gaul.cacofonix.store.Datastore;
import java.io.IOException;
import java.util.concurrent.Executors;
import np.com.axhixh.ember.Ember;
import static np.com.axhixh.ember.Ember.delete;
import static np.com.axhixh.ember.Ember.get;
import static np.com.axhixh.ember.Ember.put;
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

        get(new GetMetricList(store));
        get(new GetDataPoints("/api/metrics/:metric", store, new Identity()));
        get(new GetDataPoints("/api/metrics/:metric/count", store, new CountByInterval()));
        
        delete(new DeleteMetric(store));
        put(new UpdateMetric(store));
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
