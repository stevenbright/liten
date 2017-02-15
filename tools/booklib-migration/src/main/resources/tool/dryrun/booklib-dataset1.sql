-- Fixture Data
-- NOTE: There should be no zero values assigned to 'id' fields.

INSERT INTO author (id, f_name) VALUES (1, 'Jack London');
INSERT INTO author (id, f_name) VALUES (2, 'Edgar Poe');
INSERT INTO author (id, f_name) VALUES (3, 'Stephen King');
INSERT INTO author (id, f_name) VALUES (4, 'Joe Hill');
INSERT INTO author (id, f_name) VALUES (5, 'Arkady Strugatsky');
INSERT INTO author (id, f_name) VALUES (6, 'Boris Strugatsky');
INSERT INTO author (id, f_name) VALUES (7, 'Victor Pelevin');
INSERT INTO author (id, f_name) VALUES (8, 'Jason Ciaramella');

INSERT INTO genre (id, code) VALUES (1, 'sci_fi');
INSERT INTO genre (id, code) VALUES (2, 'fantasy');
INSERT INTO genre (id, code) VALUES (3, 'essay');
INSERT INTO genre (id, code) VALUES (4, 'novel');
INSERT INTO genre (id, code) VALUES (5, 'comics');
INSERT INTO genre (id, code) VALUES (6, 'western');
INSERT INTO genre (id, code) VALUES (7, 'horror');

INSERT INTO lang_code (id, code) VALUES (1, 'en');
INSERT INTO lang_code (id, code) VALUES (2, 'ru');

INSERT INTO book_origin (id, code) VALUES (1, 'EnglishClassicBooks');
INSERT INTO book_origin (id, code) VALUES (2, 'EnglishModernBooks');
INSERT INTO book_origin (id, code) VALUES (3, 'EnglishMisc');
INSERT INTO book_origin (id, code) VALUES (4, 'RussianBooks');

INSERT INTO series (id, name) VALUES (1, 'Noon: 22nd Century');
INSERT INTO series (id, name) VALUES (2, 'The Dark Tower');

-- Far Rainbow
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (1, 'Far Rainbow', 255365, '2007-10-23', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (1, 5);
INSERT INTO book_author (book_id, author_id) VALUES (1, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (1, 4);
INSERT INTO book_genre (book_id, genre_id) VALUES (1, 1);
INSERT INTO book_series (book_id, series_id, pos) VALUES (1, 1, 3);

-- Hermit and Sixfinger
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (2, 'Hermit and Sixfinger', 169981, '2010-01-16', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (2, 7);
INSERT INTO book_genre (book_id, genre_id) VALUES (2, 3);

-- The Dark Tower: The Gunslinger
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (3, 'The Dark Tower: The Gunslinger', 412035, '2008-06-10', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (3, 3);
INSERT INTO book_genre (book_id, genre_id) VALUES (3, 2);
INSERT INTO book_genre (book_id, genre_id) VALUES (3, 6);
INSERT INTO book_series (book_id, series_id, pos) VALUES (3, 2, 1);

-- Hard to Be a God
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (4, 'Hard to Be a God', 198245, '2008-05-14', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (4, 5);
INSERT INTO book_author (book_id, author_id) VALUES (4, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (4, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (4, 4);
INSERT INTO book_series (book_id, series_id, pos) VALUES (4, 1, 4);

-- The Dark Tower VI: Song of Susannah
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (5, 'The Dark Tower: The Wind Through the Keyhole', 412035, '2013-09-19', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (5, 3);
INSERT INTO book_genre (book_id, genre_id) VALUES (5, 2);
INSERT INTO book_genre (book_id, genre_id) VALUES (5, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (5, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (5, 7);
INSERT INTO book_series (book_id, series_id, pos) VALUES (5, 2, 6);

-- The Dark Tower: The Wind Through the Keyhole
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (6, 'The Dark Tower VI: Song of Susannah', 1423102, '2010-03-17', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (6, 3);
INSERT INTO book_genre (book_id, genre_id) VALUES (6, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (6, 2);
INSERT INTO book_genre (book_id, genre_id) VALUES (6, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (6, 7);
INSERT INTO book_series (book_id, series_id, pos) VALUES (6, 2, NULL);

-- Throttle
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (7, 'Throttle', 342781, '2014-01-05', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (7, 4);
INSERT INTO book_author (book_id, author_id) VALUES (7, 3);
INSERT INTO book_genre (book_id, genre_id) VALUES (7, 4);

-- Gunpowder
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (8, 'Gunpowder', 654124, '2014-09-10', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (8, 4);
INSERT INTO book_genre (book_id, genre_id) VALUES (8, 4);

-- The Ugly Swans
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (9, 'The Ugly Swans', 298342, '2011-11-11', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (9, 5);
INSERT INTO book_author (book_id, author_id) VALUES (9, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (9, 4);
INSERT INTO book_genre (book_id, genre_id) VALUES (9, 1);

-- The Time Wanderers
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (10, 'The Time Wanderers', 984512, '2008-09-20', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (10, 5);
INSERT INTO book_author (book_id, author_id) VALUES (10, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (10, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (10, 4);
INSERT INTO book_series (book_id, series_id, pos) VALUES (10, 1, 10);

-- The Expedition into Inferno
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (11, 'The Expedition into Inferno', 211552, '2012-04-28', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (11, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (11, 4);

-- The Powerless that be
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (12, 'The Powerless that be', 124012, '2010-04-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (12, 5);
INSERT INTO book_genre (book_id, genre_id) VALUES (12, 4);


--
-- Inaccurate Additions
--

-- From Beyond
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (13, 'From Beyond', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (13, 5);
INSERT INTO book_author (book_id, author_id) VALUES (13, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (13, 4);

-- The Land of Crimson Clouds
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (14, 'The Land of Crimson Clouds', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (14, 5);
INSERT INTO book_author (book_id, author_id) VALUES (14, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (14, 4);

-- The Way to Amalthea
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (15, 'The Way to Amalthea', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (15, 5);
INSERT INTO book_author (book_id, author_id) VALUES (15, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (15, 4);

-- Escape Attempt
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (16, 'Escape Attempt', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (16, 5);
INSERT INTO book_author (book_id, author_id) VALUES (16, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (16, 4);

-- The Cruise of the Dazzler
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (17, 'The Cruise of the Dazzler', 0, '2014-08-10', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (17, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (17, 4);

-- A Daughter of the Snows
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (18, 'A Daughter of the Snows', 0, '2014-08-10', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (18, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (18, 4);

-- The Call of the Wild
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (19, 'The Call of the Wild', 0, '2014-08-10', 1, 2);
INSERT INTO book_author (book_id, author_id) VALUES (19, 1);
INSERT INTO book_genre (book_id, genre_id) VALUES (19, 4);

-- Monday Begins on Saturday
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (20, 'Monday Begins on Saturday', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (20, 5);
INSERT INTO book_author (book_id, author_id) VALUES (20, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (20, 4);

-- The Final Circle of Paradise
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (21, 'The Final Circle of Paradise', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (21, 5);
INSERT INTO book_author (book_id, author_id) VALUES (21, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (21, 4);

-- Disquiet
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (22, 'Disquiet', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (22, 5);
INSERT INTO book_author (book_id, author_id) VALUES (22, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (22, 4);

-- Snail on the Slope
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (30, 'Disquiet', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (30, 5);
INSERT INTO book_author (book_id, author_id) VALUES (30, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (30, 4);

-- The Ugly Swans
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (31, 'The Ugly Swans', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (31, 5);
INSERT INTO book_author (book_id, author_id) VALUES (31, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (31, 4);

-- The Second Invasion from Mars
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (32, 'The Second Invasion from Mars', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (32, 5);
INSERT INTO book_author (book_id, author_id) VALUES (32, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (32, 4);

-- Tale of the Troika
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (33, 'Tale of the Troika', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (33, 5);
INSERT INTO book_author (book_id, author_id) VALUES (33, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (33, 4);

-- Prisoners of Power
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (34, 'Prisoners of Power', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (34, 5);
INSERT INTO book_author (book_id, author_id) VALUES (34, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (34, 4);

-- The Planet with all the Conveniences
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (35, 'The Planet with all the Conveniences', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (35, 5);
INSERT INTO book_author (book_id, author_id) VALUES (35, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (35, 4);

-- Space Mowgli
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (36, 'Space Mowgli', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (36, 5);
INSERT INTO book_author (book_id, author_id) VALUES (36, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (36, 4);

-- Roadside Picnic
INSERT INTO book_meta (id, title, f_size, add_date, lang_id, origin_id)
  VALUES (37, 'Roadside Picnic', 0, '2014-08-10', 2, 4);
INSERT INTO book_author (book_id, author_id) VALUES (37, 5);
INSERT INTO book_author (book_id, author_id) VALUES (37, 6);
INSERT INTO book_genre (book_id, genre_id) VALUES (37, 4);

COMMIT;

--
-- EOF
--
