
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