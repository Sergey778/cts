
DROP TABLE IF EXISTS user_group;
DROP SEQUENCE IF EXISTS user_group_seq;

CREATE SEQUENCE user_group_seq START WITH 1;

CREATE TABLE user_group (
  user_group_id BIGINT NOT NULL DEFAULT nextval('user_group_seq'),
  user_group_leader BIGINT NOT NULL,
  user_group_name TEXT NOT NULL,
  user_group_parent_id BIGINT,
  CONSTRAINT user_group_pk PRIMARY KEY (user_group_id),
  CONSTRAINT user_group_leader_fk FOREIGN KEY (user_group_leader)
    REFERENCES "user" (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT user_group_parent_fk FOREIGN KEY (user_group_parent_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE ON UPDATE CASCADE
);