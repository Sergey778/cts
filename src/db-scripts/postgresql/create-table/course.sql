
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