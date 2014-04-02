package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
import gaul.cacofonix.Metric;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author ashish
 */
public class H2Datastore implements Datastore {
    private static final Logger logger = LogManager.getLogger("cacofonix.datastore");
    private final Timer timer;
    private final Connection conn;

    public H2Datastore(String dbUrl) {
        org.h2.Driver.load();
        try {
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            timer = new Timer("Database Cleaner", true);
            init();
        } catch (IOException | SQLException err) {
            throw new RuntimeException("Error setting up datastore at " + dbUrl, err);
        }
        logger.info("Started data store at " + dbUrl);
        
    }

    private void init() throws SQLException, IOException {
        String sql = load("store/createh2.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        
        timer.scheduleAtFixedRate(new Cleaner(), 43200_000, 4200_000);
    }

    private String load(String resource) throws IOException {
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream inputStream = loader.getResourceAsStream(resource);
        if (inputStream == null) {
            throw new FileNotFoundException("Unable to load " + resource);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }
    
    private int getMetricId(String metric) throws SQLException {
        String query = "select id from metric where metric_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, metric);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
            return -1;
        }        
    }

    @Override
    public List<Metric> getMetrics() throws DatastoreException {
        try {
            try (Statement stmt = conn.createStatement()) {
                ResultSet result = stmt.executeQuery(
                        "select id, metric_name, unit, frequency, retention from metric order by metric_name");
                List<Metric> metrics = new LinkedList<>();
                while (result.next()) {
                    Metric m = new PersistedMetric(result.getInt(1),
                            result.getString(2), result.getString(3), result.getInt(4), result.getInt(5));
                    metrics.add(m);
                }
                return metrics;
            }
        } catch (SQLException err) {
            throw new DatastoreException("Unable to get list of metrics. "
                    + err.getMessage(), err);
        }
    }

    private void saveMetric(String metric) throws SQLException {
        String query = "insert into metric (metric_name) values (?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, metric);
            stmt.executeUpdate();
        }
    }

    @Override
    public void createOrUpdate(Metric m) throws DatastoreException {
        try {
            int id = getMetricId(m.getName());
            if (id != -1) {
                String query = "update metric set metric_name = ?, unit = ?, frequency = ?, retention = ? where id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, m.getName());
                    stmt.setString(2, m.getUnit());
                    stmt.setInt(3, m.getFrequeny());
                    stmt.setInt(4, m.getRetention());
                    stmt.setInt(5, id);
                    stmt.executeUpdate();
                }
            } else {
                String query = "insert into metric (metric_name, unit, frequency, retention) values (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, m.getName());
                    stmt.setString(2, m.getUnit());
                    stmt.setInt(3, m.getFrequeny());
                    stmt.setInt(4, m.getRetention());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException err) {
            throw new DatastoreException("Unable to save metric", err);
        }
    }
    
    @Override
    public void save(String metric, DataPoint dp) throws DatastoreException {
        logger.info("{}, {}, {}", metric, dp.getTimestamp(), dp.getValue());
        try {
            int id = getMetricId(metric);
            if (id == -1) {
                saveMetric(metric);
                id = getMetricId(metric);
            }
            String query = "insert into datapoint values (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                stmt.setLong(2, dp.getTimestamp());
                stmt.setDouble(3, dp.getValue());
                stmt.executeUpdate();
            }
        } catch (SQLException err) {
            throw new DatastoreException("Unable to save metric. "
                    + err.getMessage(), err);
        }
    }

    @Override
    public List<DataPoint> query(final String metric, long start, long end) throws DatastoreException {
        try {
            String query;
            String metricName;
            if (metric.endsWith("*")) {
                query = "select tstamp, value from datapoint " + 
                        "where metric_id in (select id from metric where metric_name like ? ) " +
                        "and tstamp >= ? and tstamp <= ? order by tstamp";
                metricName = metric.replace('*', '%');
            } else {
                query = "select tstamp, value from datapoint, metric " +
                        "where metric_id=id and metric_name=? " +
                        "and tstamp >= ? and tstamp <= ? order by tstamp";
                metricName = metric;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, metricName);
                stmt.setLong(2, start);
                stmt.setLong(3, end);
                
                ResultSet result = stmt.executeQuery();
                List<DataPoint> points = new LinkedList<>();
                while (result.next()) {
                    points.add(new DataPoint(result.getLong(1), result.getDouble(2)));
                }
                return points;
            }
        } catch (SQLException err) {
            String msg = String.format("Unable to get metric for %s. %s",
                    metric, err.getMessage());
            throw new DatastoreException(msg, err);
        }
    }
    
    @Override
    public void delete(String metricName) throws DatastoreException {
        logger.info("Deleting metric {}", metricName);
        try {
            String query = "delete from metric where metric_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, metricName);
                stmt.executeUpdate();
            }
        } catch (SQLException err) {
            throw new DatastoreException("Unable to delete metric " + metricName
                    + ". " + err.getMessage(), err);
        }
    }

    @Override
    public void close() {
        timer.cancel();
        try {
            conn.close();
        } catch (SQLException ex) {
            
        }
    }
    
    private class Cleaner extends TimerTask {
        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();
                PreparedStatement stmt = conn.prepareStatement("delete from datapoint where metric_id = ? and tstamp < ?");
                for (Metric metric : getMetrics()) {
                    if (metric.getRetention() > 0) {
                        PersistedMetric pm = (PersistedMetric)metric;
                        stmt.setInt(1, pm.getId());
                        stmt.setLong(2, now - (pm.getRetention() * 1000L));
                        int rows = stmt.executeUpdate();
                        logger.debug("Deleted {} rows for {}", rows, metric.getName());
                    }
                }
            } catch (SQLException | DatastoreException err) {
                logger.warn("Error running cleaner.", err);
            }
        }
    }
    
    public class PersistedMetric extends Metric {
        private final int id;
        
        public PersistedMetric(int id, String name, String unit, int interval, int retention) {
            super(name, unit, interval, retention);
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
    }
}
