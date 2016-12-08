
DROP TABLE IF EXISTS question_group;
DROP SEQUENCE IF EXISTS question_group_seq;

CREATE SEQUENCE question_group_seq START WITH 1;

CREATE TABLE question_group (
  question_group_id BIGINT NOT NULL DEFAULT nextval('question_group_seq'),
  question_group_name TEXT NOT NULL,
  question_group_creator_id BIGINT,
  question_group_parent_id BIGINT,

  CONSTRAINT question_group_pk PRIMARY KEY (question_group_id),
  CONSTRAINT question_group_parental_fk FOREIGN KEY (question_group_parent_id)
    REFERENCES question_group(question_group_id),
  CONSTRAINT question_group_user_fk FOREIGN KEY (question_group_creator_id)
    REFERENCES "user"(user_id)
);