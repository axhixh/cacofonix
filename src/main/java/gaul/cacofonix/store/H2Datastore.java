package gaul.cacofonix.store;

import gaul.cacofonix.DataPoint;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author ashish
 */
public class H2Datastore implements Datastore {
    private final Connection conn;

    public H2Datastore(String dbPath) {
        org.h2.Driver.load();
        try {
            conn = DriverManager.getConnection("jdbc:h2:" + dbPath + "/cacofonix");
            conn.setAutoCommit(true);
            init();
        } catch (IOException | SQLException err) {
            throw new RuntimeException("Error setting up datastore.", err);
        }
    }

    private void init() throws SQLException, IOException {
        String sql = load("store/createh2.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
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
    public List<String> getMetrics() throws DatastoreException {
        try {
            try (Statement stmt = conn.createStatement()) {
                ResultSet result = stmt.executeQuery(
                        "select metric_name from metric order by metric_name");
                List<String> names = new LinkedList<>();
                while (result.next()) {
                    names.add(result.getString(1));
                }
                return names;
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
    public void save(String metric, DataPoint dp) throws DatastoreException {
        System.out.println(metric + " @" + dp.getTimestamp() + " -> " + dp.getValue());
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
            int metricId = getMetricId(metric);
            if (metricId == -1) {
                return Collections.emptyList();
            }

            try (Statement stmt = conn.createStatement()) {
                String query = String.format("select tstamp, value from datapoint " +
                        "where metric_id = %d and tstamp >= %d and tstamp <= %d " +
                        "order by tstamp", metricId, start, end);
                ResultSet result = stmt.executeQuery(query);
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
    public void close() {
        try {
            conn.close();
        } catch (SQLException ex) {
            
        }
    }
}
