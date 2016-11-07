
DROP TABLE IF EXISTS user_token;

CREATE TABLE user_token (
  user_id BIGINT NOT NULL,
  user_token_value CHAR(36) NOT NULL,
  user_token_valid_until TIMESTAMP NOT NULL DEFAULT now() + INTERVAL '14' DAY,

  CONSTRAINT user_token_pk PRIMARY KEY (user_token_value),
  CONSTRAINT user_token_user_fk FOREIGN KEY (user_id) REFERENCES "user" (user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);