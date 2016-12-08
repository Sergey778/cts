
DROP TABLE IF EXISTS question;
DROP SEQUENCE IF EXISTS question_seq;

CREATE SEQUENCE question_seq START WITH 1;

CREATE TABLE question (
  question_id             BIGINT        NOT NULL DEFAULT nextval('question_seq'),
  question_creator_id     BIGINT        NOT NULL,
  question_modifier_id    BIGINT        NOT NULL,
  question_create_time    TIMESTAMP     NOT NULL DEFAULT now(),
  question_modify_time    TIMESTAMP     NOT NULL DEFAULT now(),
  question_text           TEXT          NOT NULL,
  question_group_id       BIGINT        NOT NULL,

  CONSTRAINT question_pk PRIMARY KEY (question_id),
  CONSTRAINT question_question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id),
  CONSTRAINT question_creator_id_fk FOREIGN KEY (question_creator_id)
    REFERENCES "user" (user_id),
  CONSTRAINT question_modifier_id_fk FOREIGN KEY (question_modifier_id)
    REFERENCES "user" (user_id)
);