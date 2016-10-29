
DROP TABLE IF EXISTS "user";
DROP SEQUENCE IF EXISTS "user_seq";

CREATE SEQUENCE user_seq START WITH 1;

CREATE TABLE "user" (
  user_id           BIGINT                NOT NULL  DEFAULT nextval('user_seq'),
  user_name         VARCHAR(64)  UNIQUE   NOT NULL,
  user_email        VARCHAR(254) UNIQUE   NOT NULL,
  user_password     VARCHAR(60)           NOT NULL,
  user_signup_time  TIMESTAMP             NOT NULL  DEFAULT now(),

  CONSTRAINT user_pk PRIMARY KEY (user_id)
);