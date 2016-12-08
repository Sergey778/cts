/*
  Purpose of this file is to create all tables with help of psql
  Usage:
  1) Set directory with creation sql scripts as current directory
    e.g. cd .../project-dir/src/db-scripts/postgresql/create-table
  2) Execute following command:
    psql --dbname=your database name --username="your user name" --file=create_all.sql
 */


\i user.sql
\i user_group.sql
\i user_groups.sql
\i user_auth_ref.sql
\i user_token.sql
\i course.sql
\i course_groups.sql
\i question_group.sql
\i question.sql
\i test.sql
\i test_question.sql
\i test_question_group.sql
\i question_answer.sql
\i test_try.sql
\i test_try_answers.sql
\i course_tests.sql
\i words.sql
\i synonyms.sql