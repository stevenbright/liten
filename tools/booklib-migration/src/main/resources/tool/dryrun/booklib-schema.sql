-- Author info
CREATE TABLE author (
  id        INTEGER PRIMARY KEY,
  f_name    VARCHAR(256) NOT NULL
);

-- Genre codes
CREATE TABLE genre (
  id        INTEGER PRIMARY KEY,
  code      CHAR(32) NOT NULL,
  CONSTRAINT uq_genre_code UNIQUE (code)
);

-- Language codes
CREATE TABLE lang_code (
  id        INTEGER PRIMARY KEY,
  code      CHAR(8) NOT NULL,
  CONSTRAINT uq_lang_code UNIQUE (code)
);

-- Book range (origin)
CREATE TABLE book_origin (
  id        INTEGER PRIMARY KEY,
  code      VARCHAR(256) NOT NULL,
  CONSTRAINT uq_book_origin UNIQUE (code)
);

-- Book meta information
CREATE TABLE book_meta (
  id        INTEGER PRIMARY KEY,
  title     VARCHAR(1024) NOT NULL,
  f_size    INTEGER NOT NULL,
  add_date  DATE,
  lang_id   INTEGER,
  origin_id INTEGER NOT NULL,
  CONSTRAINT fk_book_meta_lang FOREIGN KEY (lang_id) REFERENCES lang_code (id),
  CONSTRAINT fk_book_meta_origin FOREIGN KEY (origin_id) REFERENCES book_origin (id)
);

-- Book series
CREATE TABLE series (
  id        INTEGER PRIMARY KEY,
  name      VARCHAR(1024) NOT NULL,
  CONSTRAINT uq_series_name UNIQUE (name)
);

-- Book-to-series mapping
CREATE TABLE book_series (
  book_id   INTEGER NOT NULL,
  series_id INTEGER NOT NULL,
  pos       INTEGER,
  CONSTRAINT pk_book_series PRIMARY KEY (book_id, series_id)
);

-- Book-to-author link
CREATE TABLE book_author (
  book_id   INTEGER NOT NULL,
  author_id INTEGER NOT NULL,
  CONSTRAINT pk_book_author PRIMARY KEY (book_id, author_id),
  CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES book_meta(id),
  CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author(id)
);

-- Book-to-genre link
CREATE TABLE book_genre (
  book_id   INTEGER NOT NULL,
  genre_id  INTEGER NOT NULL,
  CONSTRAINT pk_book_genre PRIMARY KEY (book_id, genre_id),
  CONSTRAINT fk_book_genre_book FOREIGN KEY (book_id) REFERENCES book_meta(id),
  CONSTRAINT fk_book_genre_genre FOREIGN KEY (genre_id) REFERENCES genre(id)
);

--
-- Indexes
--

CREATE UNIQUE INDEX idx_author_f_name ON author(f_name);

