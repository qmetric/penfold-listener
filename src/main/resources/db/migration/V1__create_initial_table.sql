CREATE TABLE event_tracker
(
  tracker_id VARCHAR(128) NOT NULL,
  event_id INT NOT NULL,
  seen DATETIME NOT NULL,
  started DATETIME,
  completed DATETIME,
  PRIMARY KEY (tracker_id)
);
