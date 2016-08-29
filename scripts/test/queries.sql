
-- Select all relations in named form
SELECT i.id, i2.title, et2.name, i.title FROM item AS i
  INNER JOIN entity_type AS et ON et.id=i.type_id
  INNER JOIN item_relation AS ir ON ir.rhs=i.id
  INNER JOIN item AS i2 ON i2.id=ir.lhs
  INNER JOIN entity_type AS et2 ON et2.id=i2.type_id
  WHERE et.name='book'
  ORDER BY i.title;
