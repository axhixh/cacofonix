package gaul.cacofonix.reporter;

import gaul.cacofonix.Metric;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Properties;
import np.com.axhixh.ember.Request;
import np.com.axhixh.ember.Response;
import np.com.axhixh.ember.Route;

/**
 *
 * @author ashish
 */
class UpdateMetric extends Route {

    private final Datastore store;

    UpdateMetric(Datastore store) {
        super("/api/metrics/");
        this.store = store;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            Properties prop = new Properties();
            prop.load(new StringReader(request.getContent()));

            String name = prop.getProperty("name");
            if (name == null) {
                response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                response.send("Metric name missing");
                return;
            }
            String unit = prop.getProperty("unit", "");
            String sRetention = prop.getProperty("retention", "15552000");
            int retention = Integer.parseInt(sRetention);
            String sFrequency = prop.getProperty("frequency", "-1");
            int frequency = Integer.parseInt(sFrequency);

            Metric m = new Metric(name, unit, frequency, retention);
            store.createOrUpdate(m);
            response.send("Created or updated metric");
        } catch (IOException | NumberFormatException | DatastoreException err) {
            response.error(err);
        }
    }

}
