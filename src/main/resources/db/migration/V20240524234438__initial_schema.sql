CREATE TABLE IF NOT EXISTS resource_attributes
(
    digest     INTEGER PRIMARY KEY NOT NULL,
    attributes JSON
);

CREATE TABLE IF NOT EXISTS log
(
    log_id                     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    timestamp                  DATETIME                          NOT NULL,
    severity                   TEXT,
    service_name               TEXT,
    scope                      TEXT,
    body                       TEXT,
    trace_id                   TEXT,
    span_id                    TEXT,
    attributes                 JSON,
    resource_attributes_digest INTEGER,
    FOREIGN KEY (resource_attributes_digest) REFERENCES resource_attributes (digest) ON DELETE CASCADE
);

CREATE INDEX log_timestamp ON log (timestamp);
CREATE INDEX log_service_name ON log (service_name);
CREATE INDEX log_severity ON log (severity);
CREATE INDEX log_trace_id ON log (trace_id);

CREATE VIRTUAL TABLE log_fts USING fts5
(
    body,
    content='log',
    content_rowid='log_id',
    tokenize='trigram'
);

CREATE TRIGGER log_ai
    AFTER INSERT
    ON log
BEGIN
    INSERT INTO log_fts (rowid, body) VALUES (new.log_id, new.body);
END;

CREATE TRIGGER log_au
    AFTER UPDATE
    ON log
BEGIN
    INSERT INTO log_fts (log_fts, rowid, body) VALUES ('delete', old.log_id, old.body);
    INSERT INTO log_fts (rowid, body) VALUES (new.log_id, new.body);
END;

CREATE TRIGGER log_ad
    AFTER DELETE
    ON log
BEGIN
    INSERT INTO log_fts (log_fts, rowid, body) VALUES ('delete', old.log_id, old.body);
END;