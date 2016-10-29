
DROP TABLE IF EXISTS user_access;

CREATE TABLE user_access (
  user_access_token   VARCHAR(128)  NOT NULL,
  user_id             BIGINT        NOT NULL,

  CONSTRAINT user_access_pk PRIMARY KEY (user_access_token),
  CONSTRAINT user_access_user_fk
    FOREIGN KEY (user_id) REFERENCES "user" (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);