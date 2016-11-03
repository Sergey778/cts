
DROP TABLE IF EXISTS test;
DROP SEQUENCE IF EXISTS test_seq;

CREATE SEQUENCE test_seq START WITH 1;

CREATE TABLE test (
  test_id           BIGINT          NOT NULL DEFAULT nextval('test_seq'),
  test_name         VARCHAR(128)    NOT NULL,
  test_creator_id   BIGINT          NOT NULL,

  CONSTRAINT test_pk PRIMARY KEY (test_id),
  CONSTRAINT test_user_fk FOREIGN KEY (test_creator_id)
    REFERENCES "user" (user_id)
);