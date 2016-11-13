--
-- Tables
--

CREATE TABLE entity_type (
  id                INTEGER PRIMARY KEY,
  name              CHAR(32) NOT NULL,
  CONSTRAINT uq_entity_type_name UNIQUE (name)
);

--
-- Catalog model
--

CREATE TABLE ice_item (
  id              INTEGER PRIMARY KEY,
  type_id         INTEGER NOT NULL,

  -- unique item name
  alias           VARCHAR(32),

  CONSTRAINT fk_ice_item_entry_type FOREIGN KEY (type_id) REFERENCES entity_type(id) ON DELETE CASCADE
);

CREATE TABLE ice_sku (
  item_id         INTEGER NOT NULL,
  sku_id          INTEGER NOT NULL,

  title           VARCHAR(1024) NOT NULL,

  language_id     INTEGER NOT NULL,

  -- locale-specific
  wikipedia_url   VARCHAR(1024),

  CONSTRAINT pk_ice_sku PRIMARY KEY (item_id, sku_id),
  CONSTRAINT fk_ice_sku_item FOREIGN KEY (item_id) REFERENCES ice_item(id) ON DELETE CASCADE

  -- Commenting out relation
  --CONSTRAINT fk_ice_sku_language FOREIGN KEY (language_id) REFERENCES ice_item(id) ON DELETE CASCADE
);

CREATE TABLE ice_instance (
  item_id         INTEGER NOT NULL,
  sku_id          INTEGER NOT NULL,
  instance_id     INTEGER NOT NULL,

  created         DATE NOT NULL,

  -- book-specific
  origin_id       INTEGER,
  download_id     INTEGER,

  CONSTRAINT pk_ice_instance PRIMARY KEY (item_id, sku_id, instance_id),
  CONSTRAINT fk_ice_instance_item FOREIGN KEY (item_id) REFERENCES ice_item(id) ON DELETE CASCADE,
  CONSTRAINT fk_ice_instance_item_sku FOREIGN KEY (item_id, sku_id) REFERENCES ice_sku(item_id, sku_id) ON DELETE CASCADE
);

-- represents relationship between ice_items, e.g. author -> book relationships
CREATE TABLE ice_item_relations (
  left_id         INTEGER NOT NULL,
  right_id        INTEGER NOT NULL,
  type_id         INTEGER NOT NULL,
  CONSTRAINT fk_ice_item_relations_left FOREIGN KEY (left_id) REFERENCES ice_item(id) ON DELETE CASCADE,
  CONSTRAINT fk_ice_item_relations_right FOREIGN KEY (right_id) REFERENCES ice_item(id) ON DELETE CASCADE,
  CONSTRAINT fk_ice_item_relations_type FOREIGN KEY (type_id) REFERENCES entity_type(id) ON DELETE CASCADE
);

--
-- Sequences
--
