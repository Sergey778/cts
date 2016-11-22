
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