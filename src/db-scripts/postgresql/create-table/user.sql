
DROP TABLE IF EXISTS "user";
DROP SEQUENCE IF EXISTS "user_seq";

CREATE SEQUENCE user_seq START WITH 1;

CREATE TABLE "user" (
  user_id           BIGINT                NOT NULL  DEFAULT nextval('user_seq'),
  user_name         TEXT         UNIQUE   NOT NULL,
  user_email        TEXT         UNIQUE   NOT NULL,
  user_password     TEXT                  NOT NULL,
  user_signup_time  TIMESTAMP             NOT NULL  DEFAULT now(),
  user_confirmed    NUMERIC(1)            NOT NULL  DEFAULT 0,

  CONSTRAINT user_pk PRIMARY KEY (user_id),
  CONSTRAINT user_confirmed_check CHECK (user_confirmed IN (0, 1))
);