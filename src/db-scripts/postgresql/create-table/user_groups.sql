
DROP TABLE IF EXISTS user_groups;

CREATE TABLE user_groups (
  user_id BIGINT NOT NULL,
  user_group_id BIGINT NOT NULL,
  full_member NUMERIC(1) NOT NULL DEFAULT 0,
  CONSTRAINT user_groups_pk PRIMARY KEY (user_id, user_group_id),
  CONSTRAINT user_groups_user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id),
  CONSTRAINT user_groups_group_fk FOREIGN KEY (user_group_id)
    REFERENCES user_group (user_group_id),
  CONSTRAINT full_member_check CHECK (full_member IN (0, 1))
);