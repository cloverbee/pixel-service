CREATE KEYSPACE IF NOT EXISTS pixelwar
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS pixelwar.pixel_history (
                                                      x int,
                                                      y int,
                                                      event_time timestamp,
                                                      user_id text,
                                                      color text,
                                                      PRIMARY KEY ((x, y), event_time)
) WITH CLUSTERING ORDER BY (event_time DESC);

CREATE INDEX IF NOT EXISTS idx_user_id ON pixelwar.pixel_history (user_id);