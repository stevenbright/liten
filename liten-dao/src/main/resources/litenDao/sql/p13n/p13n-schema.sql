
CREATE TABLE favorite (
  user_id       INTEGER NOT NULL,

  --
  -- denormalized value - may refer to different things,
  -- it is up to the client to find out what exactly this field is referring to
  --
  entity_id     INTEGER NOT NULL,

  entity_kind   INTEGER NOT NULL,

  CONSTRAINT pk_favorite PRIMARY KEY (user_id, entity_id, entity_kind)
);

