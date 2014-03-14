CREATE TABLE IF NOT EXISTS metric (
  id INTEGER NOT NULL AUTO_INCREMENT,
  metric_name VARCHAR NOT NULL UNIQUE,
  retention INTEGER DEFAULT 3628800,
  frequency INTEGER DEFAULT -1,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS datapoint (
    metric_id INTEGER NOT NULL,
    tstamp BIGINT NOT NULL,
    value DOUBLE,
    PRIMARY KEY (metric_id, tstamp),
    CONSTRAINT datapoint_metric_fkey FOREIGN KEY (metric_id)
      REFERENCES metric (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS datapoint_tstamp ON datapoint (tstamp);
