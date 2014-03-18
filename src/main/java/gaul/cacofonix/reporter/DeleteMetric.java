package gaul.cacofonix.reporter;

import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.DatastoreException;
import np.com.axhixh.ember.Request;
import np.com.axhixh.ember.Response;
import np.com.axhixh.ember.Route;

/**
 *
 * @author ashish
 */
class DeleteMetric extends Route {

    private final Datastore store;

    DeleteMetric(Datastore store) {
        super("/api/metrics/:metric");
        this.store = store;
    }

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

}
