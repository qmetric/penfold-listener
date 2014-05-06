CREATE TABLE event_trackers
(
  id VARCHAR(128) NOT NULL,
  status VARCHAR(128) NOT NULL,
  event_id INT NOT NULL,
  seen DATETIME NOT NULL,
  started DATETIME,
  completed DATETIME,
  PRIMARY KEY (id)
);
