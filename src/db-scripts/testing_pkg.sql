/*
Зачатки пакета-ядра.

Вначале надо создать объект директорию
CREATE DIRECTORY TOMITA_DIR AS '\tomita-win32\my';
GRANT READ, WRITE ON DIRECTORY TOMITA_DIR TO PUBLIC;

Пакет работает с временными таблицами
CREATE TABLE test_reference_answer
  (
    question_id     NUMBER(2),
    answer_ref_text VARCHAR2(1000)
  );
CREATE TABLE test_answer
  (question_id NUMBER(2), answer_text VARCHAR2(1000)
  );

Таблица для работы с XML как CLOB  
CREATE TABLE tempxmltoclob
  ( idxml NUMBER(20), 
    theClob CLOB,
    CONSTRAINT tempxml_pk PRIMARY KEY (idxml)
  );

  
*/ 







CREATE OR REPLACE PACKAGE testing_pkg
IS
  PROCEDURE do_input( /* Создание файла input.txt для tomita из данных таблицы  test_answer */
      p_dir      VARCHAR2,
      p_filename VARCHAR2);
  PROCEDURE do_reference_input( /* Создание файла input.txt для tomita из данных таблицы  test_reference_answer */
      p_dir      VARCHAR2,
      p_dir      VARCHAR2,
      p_filename VARCHAR2);
  PROCEDURE do_parse; /* Запуск tomita */
  PROCEDURE do_remove( /* Удаление файлов. Вынес в отдельную процедуру для дальнейшей расширяемости */
      p_dir      VARCHAR2,
      p_filename VARCHAR2);
  PROCEDURE do_xml_to_clob( /* Заносит из полученного tomita файла output.xml в clob таблицы tempxmltoclob */
	  p_id       IN NUMBER,
      p_filename IN VARCHAR2); 
END testing_pkg;
/

CREATE OR REPLACE PACKAGE BODY testing_pkg
IS

  PROCEDURE do_input(
      p_dir      VARCHAR2,
      p_filename VARCHAR2)
  IS
    f_file UTL_FILE.FILE_TYPE;
    CURSOR cur_answer
    IS
      SELECT question_id, answer_text FROM test_answer ORDER BY question_id;
  BEGIN
    f_file := UTL_FILE.FOPEN (p_dir, p_filename, 'W');
    FOR answer_rec IN cur_answer
    LOOP
      UTL_FILE.PUT_LINE (f_file, answer_rec.answer_text);
      UTL_FILE.NEW_LINE (f_file);
    END LOOP;
    UTL_FILE.FCLOSE (f_file);
  EXCEPTION
  WHEN UTL_FILE.INVALID_FILEHANDLE THEN
    RAISE_APPLICATION_ERROR(-20001,'File handle is invalid.');
  WHEN UTL_FILE.WRITE_ERROR THEN
    RAISE_APPLICATION_ERROR (-20002, 'Operating system error occurred during the write operation.');
  WHEN UTL_FILE.INVALID_PATH THEN
    RAISE_APPLICATION_ERROR(-20003,'File location is invalid.');
  WHEN UTL_FILE.INVALID_MODE THEN
    RAISE_APPLICATION_ERROR (-20004, 'The open_mode parameter in FOPEN is invalid.');
  WHEN UTL_FILE.INVALID_OPERATION THEN
    RAISE_APPLICATION_ERROR(-20005,'File could not be opened or operated on as requested.');
  WHEN UTL_FILE.READ_ERROR THEN
    RAISE_APPLICATION_ERROR (-20006, 'Operating system error occurred during the read operation.');
  WHEN UTL_FILE.INTERNAL_ERROR THEN
    RAISE_APPLICATION_ERROR(-20007,'Unspecified PL/SQL error.');
  WHEN UTL_FILE.FILE_OPEN THEN
    RAISE_APPLICATION_ERROR (-20008, 'The requested operation failed because the file is open.');
  WHEN UTL_FILE.INVALID_FILENAME THEN
    RAISE_APPLICATION_ERROR(-20009,'The filename parameter is invalid.');
  WHEN UTL_FILE.ACCESS_DENIED THEN
    RAISE_APPLICATION_ERROR (-20010, 'Permission to access to the file location is denied.');
  END do_input;
  
  PROCEDURE do_reference_input(
      p_dir      VARCHAR2,
      p_filename VARCHAR2)
  IS
    f_file UTL_FILE.FILE_TYPE;
    CURSOR cur_reference_answer
    IS
      SELECT question_id,
        answer_ref_text
      FROM test_reference_answer
      ORDER BY question_id;
  BEGIN
    f_file := UTL_FILE.FOPEN (p_dir, p_filename, 'W');
    FOR answer_rec IN cur_reference_answer
    LOOP
      UTL_FILE.PUT_LINE (f_file, answer_rec.answer_ref_text);
      UTL_FILE.NEW_LINE (f_file);
    END LOOP;
    UTL_FILE.FCLOSE (f_file);
  EXCEPTION
  WHEN UTL_FILE.INVALID_FILEHANDLE THEN
    RAISE_APPLICATION_ERROR(-20001,'File handle is invalid.');
  WHEN UTL_FILE.WRITE_ERROR THEN
    RAISE_APPLICATION_ERROR (-20002, 'Operating system error occurred during the write operation.');
  WHEN UTL_FILE.INVALID_PATH THEN
    RAISE_APPLICATION_ERROR(-20003,'File location is invalid.');
  WHEN UTL_FILE.INVALID_MODE THEN
    RAISE_APPLICATION_ERROR (-20004, 'The open_mode parameter in FOPEN is invalid.');
  WHEN UTL_FILE.INVALID_OPERATION THEN
    RAISE_APPLICATION_ERROR(-20005,'File could not be opened or operated on as requested.');
  WHEN UTL_FILE.READ_ERROR THEN
    RAISE_APPLICATION_ERROR (-20006, 'Operating system error occurred during the read operation.');
  WHEN UTL_FILE.INTERNAL_ERROR THEN
    RAISE_APPLICATION_ERROR(-20007,'Unspecified PL/SQL error.');
  WHEN UTL_FILE.FILE_OPEN THEN
    RAISE_APPLICATION_ERROR (-20008, 'The requested operation failed because the file is open.');
  WHEN UTL_FILE.INVALID_FILENAME THEN
    RAISE_APPLICATION_ERROR(-20009,'The filename parameter is invalid.');
  WHEN UTL_FILE.ACCESS_DENIED THEN
    RAISE_APPLICATION_ERROR (-20010, 'Permission to access to the file location is denied.');
  END do_reference_input;
  
  PROCEDURE do_parse /* запуск следующей команды: 'cmd.exe @"/c cd c:\tomita-win32\my && tomitaparser config.proto"' */
  IS
    parser_dir     CONSTANT VARCHAR2(1000) := '\tomita-win32\my';
    parser_program CONSTANT VARCHAR2(1000) := 'tomitaparser';
    parser_config  CONSTANT VARCHAR2(1000) := 'config.proto';
  BEGIN
    DBMS_SCHEDULER.CREATE_JOB 
    ( 
      job_name => 'TOMITA_JOB',
      job_type => 'EXECUTABLE',
      number_of_arguments => 6,
      job_action => '\windows\system32\cmd.exe' 
      );
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',1,'/c');
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',2,'cd');
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',3,parser_dir);
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',4,'&&');
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',5,parser_program);
    DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('TOMITA_JOB',6,parser_config);
    DBMS_SCHEDULER.ENABLE('TOMITA_JOB');
  EXCEPTION
  WHEN OTHERS THEN
    RAISE_APPLICATION_ERROR(-20011,'Error in parsing!');
  END do_parse;
  
  
  PROCEDURE do_remove(
      p_dir      VARCHAR2,
      p_filename VARCHAR2)
  IS
  BEGIN
    UTL_FILE.FREMOVE (p_dir, p_filename);
  EXCEPTION
  WHEN UTL_FILE.DELETE_FAILED THEN
    RAISE_APPLICATION_ERROR(-20012,'The requested file delete operation failed.');
  WHEN UTL_FILE.INVALID_PATH THEN
    RAISE_APPLICATION_ERROR(-20003,'File location is invalid.');
  WHEN UTL_FILE.INVALID_OPERATION THEN
    RAISE_APPLICATION_ERROR(-20005,'File could not be opened or operated on as requested.');
  WHEN UTL_FILE.INVALID_FILENAME THEN
    RAISE_APPLICATION_ERROR(-20009,'The filename parameter is invalid.');
  WHEN UTL_FILE.ACCESS_DENIED THEN
    RAISE_APPLICATION_ERROR (-20010, 'Permission to access to the file location is denied.');
  END do_remove;
  
  PROCEDURE do_xml_to_clob(
	  p_id       IN NUMBER,
      p_filename IN VARCHAR2)
  IS
    parser_db_dir  CONSTANT VARCHAR2(1000) := 'TOMITA_DIR';
	  l_clob CLOB;
    l_bfile bfile;
    dest_offset  NUMBER := 1;
    src_offset  NUMBER := 1;
    lang_ctx    NUMBER := DBMS_LOB.DEFAULT_LANG_CTX;
    warning     NUMBER;
  BEGIN	  
    INSERT INTO tempxmltoclob VALUES
      (p_id, empty_clob()
      ) RETURNING theClob INTO l_clob;
    l_bfile := BFILENAME(parser_db_dir, p_filename);
    DBMS_LOB.FILEOPEN(l_bfile);
    DBMS_LOB.LOADCLOBFROMFILE( 
	  DEST_LOB     => l_clob
      , SRC_BFILE    => l_bfile
      , AMOUNT       => DBMS_LOB.GETLENGTH(l_bfile)
      , DEST_OFFSET  => dest_offset
      , SRC_OFFSET   => src_offset
      , BFILE_CSID   => DBMS_LOB.DEFAULT_CSID
      , LANG_CONTEXT => lang_ctx
      , WARNING      => warning
      );
    DBMS_LOB.FILECLOSE(l_bfile);
  EXCEPTION
  WHEN DBMS_LOB.INVALID_ARGVAL THEN
    RAISE_APPLICATION_ERROR(-20013,'The argument is expecting a nonNULL, valid value but the argument value passed in is NULL, invalid, or out of range.');
  WHEN DBMS_LOB.ACCESS_ERROR THEN
    RAISE_APPLICATION_ERROR(-20014,'You are trying to write too much data to the LOB: LOB size is limited to 4 gigabytes.');	
  WHEN DBMS_LOB.NOEXIST_DIRECTORY THEN
    RAISE_APPLICATION_ERROR(-20015,'The directory leading to the file does not exist.');
  WHEN DBMS_LOB.NOPRIV_DIRECTORY THEN
    RAISE_APPLICATION_ERROR(-20016,'The user does not have the necessary access privileges on the directory or the file for the operation.');
  WHEN DBMS_LOB.INVALID_DIRECTORY THEN
    RAISE_APPLICATION_ERROR(-20017,'The directory used for the current operation is not valid if being accessed for the first time, or if it has been modified by the DBA since the last access.');
  WHEN DBMS_LOB.OPERATION_FAILED THEN
    RAISE_APPLICATION_ERROR(-20018,'The operation attempted on the file failed.');	
  WHEN DBMS_LOB.UNOPENED_FILE THEN
    RAISE_APPLICATION_ERROR(-20019,'The file is not open for the required operation to be performed.');
  WHEN DBMS_LOB.OPEN_TOOMANY THEN
    RAISE_APPLICATION_ERROR(-20020,'The number of open files has reached the maximum limit.');	
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20021,'EndofLob indicator for looping read operations.');
  WHEN VALUE_ERROR THEN
     RAISE_APPLICATION_ERROR(-20022,'PL/SQL error for invalid values to subprograms parameters.');	
  END do_xml_to_clob;

  
END testing_pkg;
/

----

