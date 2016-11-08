
DROP TABLE IF EXISTS question_answer;

CREATE TABLE question_answer (
  question_id BIGINT NOT NULL,
  question_answer_value VARCHAR(1024) NOT NULL,
  question_answer_creator_id BIGINT NOT NULL,
  CONSTRAINT question_answer_pk PRIMARY KEY (question_id, question_answer_value),
  CONSTRAINT question_answer_question_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT question_answer_creator_fk FOREIGN KEY (question_answer_creator_id)
    REFERENCES "user" (user_id) ON DELETE CASCADE ON UPDATE CASCADE 
)