--
-- Tables
--

CREATE TABLE entity_type (
  id                INTEGER PRIMARY KEY,
  name              CHAR(32) NOT NULL,
  CONSTRAINT uq_entity_type_name UNIQUE (name)
);

CREATE TABLE item (
  id                INTEGER PRIMARY KEY,
  type_id           INTEGER NOT NULL,

  title             VARCHAR(1024) NOT NULL,

  description       VARCHAR(1024),
  CONSTRAINT fk_item_type FOREIGN KEY (type_id) REFERENCES entity_type(id) ON DELETE CASCADE
);
