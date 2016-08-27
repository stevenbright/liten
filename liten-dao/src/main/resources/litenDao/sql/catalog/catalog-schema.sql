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

CREATE TABLE item_relation (
  lhs               INTEGER NOT NULL,
  rhs               INTEGER NOT NULL,
  type_id           INTEGER NOT NULL,
  CONSTRAINT pk_item_relation PRIMARY KEY (lhs, rhs, type_id),
  CONSTRAINT fk_item_relation_lhs FOREIGN KEY (lhs) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_relation_rhs FOREIGN KEY (rhs) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_relation_type FOREIGN KEY (type_id) REFERENCES entity_type(id) ON DELETE CASCADE
);

CREATE TABLE item_download (
  item_id           INTEGER PRIMARY KEY,

  file_size         INTEGER NOT NULL,
  file_type         VARCHAR(32) NOT NULL,
  add_date          DATE,

  download_id       VARCHAR(1024) NOT NULL,
  origin_id         INTEGER NOT NULL,

  CONSTRAINT fk_item_download_id FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_download_origin_id FOREIGN KEY (origin_id) REFERENCES item(id) ON DELETE CASCADE
);

--
-- Sequences
--

CREATE SEQUENCE seq_item              START WITH 1000;
