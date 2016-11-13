--
-- Indexes
-- Should be separate for schema for initialization scripts (and some types of tests)
--

CREATE UNIQUE INDEX idx_ice_item_alias ON ice_item(alias);
