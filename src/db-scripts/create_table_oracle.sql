/*
DROP TABLE IF EXISTS "user";
DROP SEQUENCE IF EXISTS "user_seq";

CREATE SEQUENCE user_seq START WITH 1;

CREATE TABLE "user" (
  user_id           BIGINT                NOT NULL  DEFAULT nextval('user_seq'),
  user_name         VARCHAR(64)  UNIQUE   NOT NULL,
  user_email        VARCHAR(254) UNIQUE   NOT NULL,
  user_password     VARCHAR(60)           NOT NULL,
  user_signup_time  TIMESTAMP             NOT NULL  DEFAULT now(),
  user_confirmed    BOOLEAN               NOT NULL  DEFAULT FALSE,

  CONSTRAINT user_pk PRIMARY KEY (user_id)
);
*/

DROP TABLE "user" CASCADE CONSTRAINTS;
DROP SEQUENCE user_seq;

CREATE SEQUENCE user_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

/* Oracle 12c */
CREATE TABLE "user" (
  user_id           NUMBER(19) DEFAULT user_seq.NEXTVAL,
  user_name         VARCHAR2(64)  UNIQUE   NOT NULL,
  user_email        VARCHAR2(254) UNIQUE   NOT NULL,
  user_password     VARCHAR2(60)           NOT NULL,
  user_signup_time  TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  user_confirmed    NUMBER(1) DEFAULT 0 NOT NULL, 
  CONSTRAINT user_pk PRIMARY KEY (user_id),
  CONSTRAINT user_confirmed_ck CHECK (user_confirmed IN (0, 1)) 
); 

----
 /* Oracle 11g */
CREATE TABLE "user" (
  user_id           NUMBER(19),
  user_name         VARCHAR2(64)  UNIQUE   NOT NULL,
  user_email        VARCHAR2(254) UNIQUE   NOT NULL,
  user_password     VARCHAR2(60)           NOT NULL,
  user_signup_time  TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  user_confirmed    NUMBER(1) DEFAULT 0 NOT NULL, 
  CONSTRAINT user_pk PRIMARY KEY (user_id),
  CONSTRAINT user_confirmed_check CHECK (user_confirmed IN (0, 1)) 
); 

 DROP TRIGGER user_trigger;
 
 CREATE TRIGGER user_trigger
 BEFORE INSERT OR UPDATE OF user_id ON "user"
 FOR EACH ROW
 BEGIN
	:NEW.user_id := user_seq.NEXTVAL;
 END;
   /
----

 
/*
DROP TABLE IF EXISTS course;
DROP SEQUENCE IF EXISTS course_seq;

CREATE SEQUENCE course_seq START WITH 1;

CREATE TABLE course (
  course_id BIGINT NOT NULL DEFAULT nextval('course_seq'),
  course_name TEXT NOT NULL,
  course_description TEXT NOT NULL,
  course_creator_id BIGINT NOT NULL,
  CONSTRAINT course_pk PRIMARY KEY (course_id),
  CONSTRAINT course_user_fk FOREIGN KEY (course_creator_id)
    REFERENCES "user"(user_id)
);
*/
DROP TABLE course CASCADE CONSTRAINTS;
DROP SEQUENCE course_seq;

CREATE SEQUENCE course_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

 /* Oracle 12c */
 CREATE TABLE course (
  course_id NUMBER(19) DEFAULT course_seq.NEXTVAL,
  course_name VARCHAR2(100) NOT NULL,
  course_description VARCHAR2(4000) NOT NULL,
  course_creator_id NUMBER(19) NOT NULL,
  CONSTRAINT course_pk PRIMARY KEY (course_id),
  CONSTRAINT course_user_fk FOREIGN KEY (course_creator_id)
    REFERENCES "user"(user_id)
);

---- 
 /* Oracle 11g */
 CREATE TABLE course (
  course_id NUMBER(19),
  course_name VARCHAR2(100) NOT NULL,
  course_description VARCHAR2(4000) NOT NULL,
  course_creator_id NUMBER(19) NOT NULL,
  CONSTRAINT course_pk PRIMARY KEY (course_id),
  CONSTRAINT course_user_fk FOREIGN KEY (course_creator_id)
    REFERENCES "user"(user_id)
);
 
 DROP TRIGGER course_trigger;
 
 CREATE TRIGGER course_trigger
  BEFORE INSERT OR UPDATE OF course_id ON course
 FOR EACH ROW
 BEGIN
	:NEW.course_id := course_seq.NEXTVAL;
 END;
  /
----  


 
  /*
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
*/

DROP TABLE user_group CASCADE CONSTRAINTS;
DROP SEQUENCE user_group_seq;

CREATE SEQUENCE user_group_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

 /* Oracle 12c */
CREATE TABLE user_group (
  user_group_id NUMBER(19) DEFAULT user_group_seq.NEXTVAL,
  user_group_leader NUMBER(19) NOT NULL,
  user_group_name VARCHAR2(100) NOT NULL,
  user_group_parent_id NUMBER(19),
  CONSTRAINT user_group_pk PRIMARY KEY (user_group_id),
  CONSTRAINT user_group_leader_fk FOREIGN KEY (user_group_leader)
    REFERENCES "user" (user_id) ON DELETE CASCADE,
  CONSTRAINT user_group_parent_fk FOREIGN KEY (user_group_parent_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE
);

----
 /* Oracle 11g */
CREATE TABLE user_group (
  user_group_id NUMBER(19),
  user_group_leader NUMBER(19) NOT NULL,
  user_group_name VARCHAR2(100) NOT NULL,
  user_group_parent_id NUMBER(19),
  CONSTRAINT user_group_pk PRIMARY KEY (user_group_id),
  CONSTRAINT user_group_leader_fk FOREIGN KEY (user_group_leader)
    REFERENCES "user" (user_id) ON DELETE CASCADE,
  CONSTRAINT user_group_parent_fk FOREIGN KEY (user_group_parent_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE
);

 DROP TRIGGER user_group_trigger;
 
 CREATE TRIGGER user_group_trigger
 BEFORE INSERT OR UPDATE OF user_group_id ON user_group
 FOR EACH ROW
 BEGIN
	:NEW.user_group_id := user_group_seq.NEXTVAL;
 END;
   /
----

  
/*  
DROP TABLE IF EXISTS course_groups;

CREATE TABLE course_groups (
  course_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  CONSTRAINT course_groups_pk PRIMARY KEY (course_id, group_id),
  CONSTRAINT course_groups_group_fk FOREIGN KEY (group_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT course_groups_course_fk FOREIGN KEY (course_id)
    REFERENCES course (course_id) ON DELETE CASCADE ON UPDATE CASCADE
);  
*/

DROP TABLE course_groups CASCADE CONSTRAINTS;

CREATE TABLE course_groups (
  course_id NUMBER(19) NOT NULL,
  group_id NUMBER(19) NOT NULL,
  CONSTRAINT course_groups_pk PRIMARY KEY (course_id, group_id),
  CONSTRAINT course_groups_group_fk FOREIGN KEY (group_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE,
  CONSTRAINT course_groups_course_fk FOREIGN KEY (course_id)
    REFERENCES course (course_id) ON DELETE CASCADE
);  


/*
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
*/

DROP TABLE test CASCADE CONSTRAINTS;
DROP SEQUENCE test_seq;

CREATE SEQUENCE test_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

 /* Oracle 12c */
CREATE TABLE test (
  test_id           NUMBER(19) DEFAULT test_seq.NEXTVAL,
  test_name         VARCHAR2(128)    NOT NULL,
  test_creator_id   NUMBER(19) NOT NULL,
  CONSTRAINT test_pk PRIMARY KEY (test_id),
  CONSTRAINT test_user_fk FOREIGN KEY (test_creator_id)
    REFERENCES "user" (user_id)
);

----
 /* Oracle 11g */
CREATE TABLE test (
  test_id           NUMBER(19),
  test_name         VARCHAR2(128)    NOT NULL,
  test_creator_id   NUMBER(19) NOT NULL,
  CONSTRAINT test_pk PRIMARY KEY (test_id),
  CONSTRAINT test_user_fk FOREIGN KEY (test_creator_id)
    REFERENCES "user" (user_id)
); 

 DROP TRIGGER test_trigger;
 
 CREATE TRIGGER test_trigger
 BEFORE INSERT OR UPDATE OF test_id ON test
 FOR EACH ROW
 BEGIN
	:NEW.test_id := test_seq.NEXTVAL;
 END;
   /
---- 
 
 
/*
DROP TABLE IF EXISTS course_tests;

CREATE TABLE course_tests (
  course_id BIGINT NOT NULL,
  test_id BIGINT NOT NULL,
  CONSTRAINT course_tests_pk PRIMARY KEY (course_id, test_id),
  CONSTRAINT course_tests_course_fk FOREIGN KEY (course_id)
    REFERENCES course (course_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT course_tests_tests_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id) ON DELETE CASCADE ON UPDATE CASCADE 
);
*/

DROP TABLE course_tests CASCADE CONSTRAINTS;

CREATE TABLE course_tests (
  course_id NUMBER(19) NOT NULL,
  test_id NUMBER(19) NOT NULL,
  CONSTRAINT course_tests_pk PRIMARY KEY (course_id, test_id),
  CONSTRAINT course_tests_course_fk FOREIGN KEY (course_id)
    REFERENCES course (course_id) ON DELETE CASCADE,
  CONSTRAINT course_tests_tests_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id) ON DELETE CASCADE 
);



/*
DROP TABLE IF EXISTS question_group;
DROP SEQUENCE IF EXISTS question_group_seq;

CREATE SEQUENCE question_group_seq START WITH 1;

CREATE TABLE question_group (
  question_group_id BIGINT NOT NULL DEFAULT nextval('question_group_seq'),
  question_group_name VARCHAR(80) NOT NULL,
  question_group_creator_id BIGINT,
  question_group_parent_id BIGINT,

  CONSTRAINT question_group_pk PRIMARY KEY (question_group_id),
  CONSTRAINT question_group_parental_fk FOREIGN KEY (question_group_parent_id)
    REFERENCES question_group(question_group_id),
  CONSTRAINT question_group_user_fk FOREIGN KEY (question_group_creator_id)
    REFERENCES "user"(user_id)
);
*/

DROP TABLE question_group CASCADE CONSTRAINTS;
DROP SEQUENCE question_group_seq;

CREATE SEQUENCE question_group_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

 /* Oracle 12c */
CREATE TABLE question_group (
  question_group_id NUMBER(19) DEFAULT question_group_seq.NEXTVAL,
  question_group_name VARCHAR2(100) NOT NULL,
  question_group_creator_id NUMBER(19),
  question_group_parent_id NUMBER(19),

  CONSTRAINT question_group_pk PRIMARY KEY (question_group_id),
  CONSTRAINT question_group_parental_fk FOREIGN KEY (question_group_parent_id)
    REFERENCES question_group(question_group_id),
  CONSTRAINT question_group_user_fk FOREIGN KEY (question_group_creator_id)
    REFERENCES "user"(user_id)
);


----
 /* Oracle 11g */
CREATE TABLE question_group (
  question_group_id NUMBER(19),
  question_group_name VARCHAR2(100) NOT NULL,
  question_group_creator_id NUMBER(19),
  question_group_parent_id NUMBER(19),
  CONSTRAINT question_group_pk PRIMARY KEY (question_group_id),
  CONSTRAINT question_group_parental_fk FOREIGN KEY (question_group_parent_id)
    REFERENCES question_group(question_group_id),
  CONSTRAINT question_group_user_fk FOREIGN KEY (question_group_creator_id)
    REFERENCES "user"(user_id)
);

 DROP TRIGGER question_group_trigger;
 
 CREATE TRIGGER question_group_trigger
 BEFORE INSERT OR UPDATE OF question_group_id ON question_group
 FOR EACH ROW
 BEGIN
	:NEW.question_group_id := question_group_seq.NEXTVAL;
 END;
   / 
---- 



/*
DROP TABLE IF EXISTS question;
DROP SEQUENCE IF EXISTS question_seq;

CREATE SEQUENCE question_seq START WITH 1;

CREATE TABLE question (
  question_id             BIGINT        NOT NULL DEFAULT nextval('question_seq'),
  question_creator_id     BIGINT        NOT NULL,
  question_modifier_id    BIGINT        NOT NULL,
  question_create_time    TIMESTAMP     NOT NULL DEFAULT now(),
  question_modify_time    TIMESTAMP     NOT NULL DEFAULT now(),
  question_text           VARCHAR(1024) NOT NULL,
  question_group_id       BIGINT        NOT NULL,

  CONSTRAINT question_pk PRIMARY KEY (question_id),
  CONSTRAINT question_question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id),
  CONSTRAINT question_creator_id_fk FOREIGN KEY (question_creator_id)
    REFERENCES "user" (user_id),
  CONSTRAINT question_modifier_id_fk FOREIGN KEY (question_modifier_id)
    REFERENCES "user" (user_id)
);
*/

DROP TABLE question CASCADE CONSTRAINTS;
DROP SEQUENCE question_seq;

CREATE SEQUENCE question_seq
 START WITH     1
 INCREMENT BY   1
 NOMAXVALUE
 NOCYCLE;

 /* Oracle 12c */
CREATE TABLE question (
  question_id             NUMBER(19)    DEFAULT question_seq.NEXTVAL,
  question_creator_id     NUMBER(19)        NOT NULL,
  question_modifier_id    NUMBER(19)        NOT NULL,
  question_create_time    TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  question_modify_time    TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  question_text           VARCHAR2(4000) NOT NULL,
  question_group_id       NUMBER(19)        NOT NULL,
  CONSTRAINT question_pk PRIMARY KEY (question_id),
  CONSTRAINT question_question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id) ON DELETE CASCADE,
  CONSTRAINT question_creator_id_fk FOREIGN KEY (question_creator_id)
    REFERENCES "user" (user_id),
  CONSTRAINT question_modifier_id_fk FOREIGN KEY (question_modifier_id)
    REFERENCES "user" (user_id)
);
 
----
 /* Oracle 11g */
CREATE TABLE question (
  question_id             NUMBER(19),
  question_creator_id     NUMBER(19)        NOT NULL,
  question_modifier_id    NUMBER(19)        NOT NULL,
  question_create_time    TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  question_modify_time    TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
  question_text           VARCHAR2(4000) NOT NULL,
  question_group_id       NUMBER(19)        NOT NULL,
  CONSTRAINT question_pk PRIMARY KEY (question_id),
  CONSTRAINT question_question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id) ON DELETE CASCADE,
  CONSTRAINT question_creator_id_fk FOREIGN KEY (question_creator_id)
    REFERENCES "user" (user_id),
  CONSTRAINT question_modifier_id_fk FOREIGN KEY (question_modifier_id)
    REFERENCES "user" (user_id)
);
 
 DROP TRIGGER question_trigger;
 
 CREATE TRIGGER question_trigger
 BEFORE INSERT OR UPDATE OF question_id ON question
 FOR EACH ROW
 BEGIN
	:NEW.question_id := question_seq.NEXTVAL;
 END;
   / 
----



/*
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
);
*/

DROP TABLE question_answer CASCADE CONSTRAINTS;

CREATE TABLE question_answer (
  question_answer_id NUMBER(19) NOT NULL,
  question_id NUMBER(19) NOT NULL,
    /* ! answer_number */  /*  !!!!!!!!!! */
  question_answer_value VARCHAR2(1000) NOT NULL,
  question_answer_creator_id NUMBER(19)NOT NULL,
  question_answer_tomita_xml VARCHAR2(4000) DEFAULT NULL,
  CONSTRAINT question_answer_pk PRIMARY KEY (question_id, answer_number),
  CONSTRAINT question_answer_question_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id) ON DELETE CASCADE,
  CONSTRAINT question_answer_creator_fk FOREIGN KEY (question_answer_creator_id)
    REFERENCES "user" (user_id) ON DELETE CASCADE
);



/*
DROP TABLE IF EXISTS test_question;

CREATE TABLE test_question (
  test_id       BIGINT    NOT NULL,
  question_id   BIGINT    NOT NULL,

  CONSTRAINT test_question_pk PRIMARY KEY (test_id, question_id),
  CONSTRAINT test_question_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT test_question_question_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id)
);
*/

DROP TABLE test_question CASCADE CONSTRAINTS;

CREATE TABLE test_question (
  test_id       NUMBER(19)    NOT NULL,
  question_id   NUMBER(19)    NOT NULL,
  CONSTRAINT test_question_pk PRIMARY KEY (test_id, question_id),
  CONSTRAINT test_question_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id) ON DELETE CASCADE,
  CONSTRAINT test_question_question_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id) ON DELETE CASCADE
);

 


/*
DROP TABLE IF EXISTS test_question_group;

CREATE TABLE test_question_group (
  test_id BIGINT NOT NULL,
  question_group_id BIGINT NOT NULL,
  question_count INT4 NOT NULL,

  CONSTRAINT test_question_group_pk PRIMARY KEY (test_id, question_group_id),
  CONSTRAINT test_question_group_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT test_question_group_question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id),
  CONSTRAINT question_count_check CHECK (question_count > 0)
);
*/

DROP TABLE test_question_group CASCADE CONSTRAINTS;

CREATE TABLE test_question_group (
  test_id NUMBER(19) NOT NULL,
  question_group_id NUMBER(19) NOT NULL,
  question_count NUMBER(10) NOT NULL,
  CONSTRAINT test_question_group_pk PRIMARY KEY (test_id, question_group_id),
  CONSTRAINT test_question_group_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id) ON DELETE CASCADE,
	/* !  SQL Error: ORA-00972: слишком длинный идентификатор*/
  CONSTRAINT question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id) ON DELETE CASCADE,
  CONSTRAINT question_count_check CHECK (question_count > 0)
);



/*
DROP TABLE IF EXISTS user_auth_ref;

CREATE TABLE user_auth_ref (
  user_id                     BIGINT    NOT NULL,
  user_auth_ref               CHAR(36)  NOT NULL UNIQUE,
  -- Type must be 'R' - for password reset or 'C' - for sign up confirmation
  user_auth_ref_type          CHAR(1)   NOT NULL,
  user_auth_ref_valid_until   TIMESTAMP NOT NULL DEFAULT now() + INTERVAL '1' DAY,
  
  CONSTRAINT user_auth_ref_pk PRIMARY KEY (user_auth_ref),
  CONSTRAINT user_auth_ref_user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT user_auth_ref_type_chk CHECK (user_auth_ref_type IN ('R', 'C'))
);
*/

DROP TABLE user_auth_ref CASCADE CONSTRAINTS;

CREATE TABLE user_auth_ref (
  user_id                     NUMBER(19)    NOT NULL,
  user_auth_ref               CHAR(36),
  -- Type must be 'R' - for password reset or 'C' - for sign up confirmation
  user_auth_ref_type          CHAR(1)   NOT NULL,
  user_auth_ref_valid_until   TIMESTAMP DEFAULT SYSTIMESTAMP + INTERVAL '1' DAY NOT NULL , 
  CONSTRAINT user_auth_ref_pk PRIMARY KEY (user_auth_ref),
  CONSTRAINT user_auth_ref_user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id) ON DELETE CASCADE,
  CONSTRAINT user_auth_ref_type_chk CHECK (user_auth_ref_type IN ('R', 'C'))
);



/*
DROP TABLE IF EXISTS user_groups;

CREATE TABLE user_groups (
  user_id BIGINT NOT NULL,
  user_group_id BIGINT NOT NULL,
  full_member CHAR NOT NULL DEFAULT 'N',
  CONSTRAINT user_groups_pk PRIMARY KEY (user_id, user_group_id),
  CONSTRAINT user_groups_user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id),
  CONSTRAINT user_groups_group_fk FOREIGN KEY (user_group_id)
    REFERENCES user_group (user_group_id),
  CONSTRAINT full_member_check CHECK (full_member IN ('T', 'F'))
);
*/

DROP TABLE user_groups CASCADE CONSTRAINTS;

CREATE TABLE user_groups (
  user_id NUMBER(19) NOT NULL,
  user_group_id NUMBER(19) NOT NULL,
  full_member NUMBER(1) DEFAULT 0 NOT NULL,
  /* !!!!!!! */
  CONSTRAINT user_groups_pk PRIMARY KEY (user_id, user_group_id),
  CONSTRAINT user_groups_user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id) ON DELETE CASCADE,
  CONSTRAINT user_groups_group_fk FOREIGN KEY (user_group_id)
    REFERENCES user_group (user_group_id) ON DELETE CASCADE,
  CONSTRAINT full_member_check CHECK (full_member IN (0, 1))
);


/*
DROP TABLE IF EXISTS user_token;

CREATE TABLE user_token (
  user_id BIGINT NOT NULL,
  user_token_value CHAR(36) NOT NULL,
  user_token_valid_until TIMESTAMP NOT NULL DEFAULT now() + INTERVAL '14' DAY,

  CONSTRAINT user_token_pk PRIMARY KEY (user_token_value),
  CONSTRAINT user_token_user_fk FOREIGN KEY (user_id) REFERENCES "user" (user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);
*/

DROP TABLE user_token CASCADE CONSTRAINTS;

CREATE TABLE user_token (
  user_id NUMBER(19) NOT NULL,
  user_token_value CHAR(36),
  user_token_valid_until TIMESTAMP DEFAULT SYSTIMESTAMP + INTERVAL '14' DAY NOT NULL ,
  CONSTRAINT user_token_pk PRIMARY KEY (user_token_value),
  CONSTRAINT user_token_user_fk FOREIGN KEY (user_id) REFERENCES "user" (user_id)
    ON DELETE CASCADE
);

/*
DROP TABLE IF EXISTS words;

CREATE TABLE words (
  word_id INTEGER NOT NULL,
  word_value TEXT NOT NULL, -- IF USE ORACLE SET VARCHAR2(255)
  CONSTRAINT words_pk PRIMARY KEY (word_id)
);
*/

DROP TABLE words CASCADE CONSTRAINTS;

CREATE TABLE words (
  word_id NUMBER(10),
  word_value VARCHAR2(100) NOT NULL,
  CONSTRAINT words_pk PRIMARY KEY (word_id)
);



/*
DROP TABLE IF EXISTS synonyms;

CREATE TABLE synonyms (
  word_id INTEGER NOT NULL,
  synonym_id INTEGER NOT NULL,
  CONSTRAINT synonyms_pk PRIMARY KEY (word_id, synonym_id),
  CONSTRAINT word_fk FOREIGN KEY (word_id) 
    REFERENCES words (word_id),
  CONSTRAINT synonym_fk FOREIGN KEY (synonym_id)
    REFERENCES words (word_id)
);
*/
 
DROP TABLE word_synonyms CASCADE CONSTRAINTS;

CREATE TABLE word_synonyms (
/* ! !!!!!!!!! SQL Error: ORA-00955: имя уже задействовано для существующего объекта ! */
  word_id NUMBER(10) NOT NULL,
  synonym_id NUMBER(10) NOT NULL,
  CONSTRAINT synonyms_pk PRIMARY KEY (word_id, synonym_id),
  CONSTRAINT words_fk FOREIGN KEY (word_id) 
    REFERENCES words (word_id) ON DELETE CASCADE,
  CONSTRAINT synonyms_fk FOREIGN KEY (synonym_id)
    REFERENCES words (word_id) ON DELETE CASCADE
);




/*
DROP TABLE IF EXISTS test_try;

CREATE TABLE test_try (
  test_try_id TEXT NOT NULL,
  test_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  test_try_start TIMESTAMP NOT NULL DEFAULT now(),

  CONSTRAINT test_try_pk PRIMARY KEY (test_try_id),
  CONSTRAINT test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id)
);
*/

DROP TABLE test_try CASCADE CONSTRAINTS;

CREATE TABLE test_try (
  test_try_id CHAR(36) NOT NULL,
  test_id NUMBER(19) NOT NULL,
  user_id NUMBER(19) NOT NULL,
  test_try_start TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT test_try_pk PRIMARY KEY (test_try_id),
  CONSTRAINT test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id)
);




/*
DROP TABLE IF EXISTS test_try_answers;

CREATE TABLE test_try_answers (
  test_try_id TEXT NOT NULL,
  question_id BIGINT NOT NULL,
  answer TEXT DEFAULT NULL,
  is_correct NUMERIC(1) DEFAULT NULL,
  CONSTRAINT test_try_answers_pk PRIMARY KEY (test_try_id, question_id),
  CONSTRAINT test_try_fk FOREIGN KEY (test_try_id)
    REFERENCES test_try (test_try_id),
  CONSTRAINT user_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id),
  CONSTRAINT is_correct_check CHECK ((is_correct IS NULL) OR (is_correct IN (0, 1)))
);
*/

DROP TABLE test_try_answers CASCADE CONSTRAINTS;

CREATE TABLE test_try_answers (
  test_try_id CHAR(36) NOT NULL,
  question_id NUMBER(19) NOT NULL,
  answer VARCHAR2(1000) DEFAULT NULL,
  system_grade NUMBER(3) DEFAULT NULL,
  teacher_grade NUMBER(3) DEFAULT NULL,
  /* ! процент или 0, 1 ? */
  chat_id NUMBER(19);
  CONSTRAINT test_try_answers_pk PRIMARY KEY (test_try_id, question_id),
  CONSTRAINT test_try_fk FOREIGN KEY (test_try_id)
    REFERENCES test_try (test_try_id),
  CONSTRAINT user_ans_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id),
	/* ! SQL Error: ORA-02264: это имя уже используется в существующем ограничении */
  CONSTRAINT is_correct_check CHECK ((is_correct IS NULL) OR (is_correct IN (0, 1)))
);

