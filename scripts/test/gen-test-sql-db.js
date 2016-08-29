
// sample run:
// node ./gen-test-sql-db.js -authorCount 7 -originsCount 3 -bookCount 25 -fileName /tmp/s1.sql
// then display contents: cat /tmp/s1.sql


// insert into database:
// java -cp ~/.m2/repository/com/h2database/h2/1.4.190/h2-1.4.190.jar org.h2.tools.RunScript -url jdbc:h2:/tmp/liten1 -user sa -script ../../liten-dao/src/main/resources/litenDao/sql/catalog/catalog-schema.sql
// java -cp ~/.m2/repository/com/h2database/h2/1.4.190/h2-1.4.190.jar org.h2.tools.RunScript -url jdbc:h2:/tmp/liten1 -user sa -script /tmp/s1.sql
// rlwrap java -cp ~/.m2/repository/com/h2database/h2/1.4.190/h2-1.4.190.jar org.h2.tools.Shell -url jdbc:h2:/tmp/liten1 -user sa

// add indexes:
// java -cp ~/.m2/repository/com/h2database/h2/1.4.190/h2-1.4.190.jar org.h2.tools.RunScript -url jdbc:h2:/tmp/liten1 -user sa -script ../../liten-dao/src/main/resources/litenDao/sql/catalog/catalog-indexes.sql

var genUtil = require('./util/gen-util');
var producerUtil = require('./util/producer-util');
var data = require('./util/sample-data');
var fs = require('fs');

//
// Random Name Generator
//

function randPersonName() {
    var result = [];
//    genUtil.appendRandName(result, data.PERSON_NAME_PREFIX);
    genUtil.appendRandName(result, data.PERSON_LAST_NAME);
    genUtil.appendRandName(result, data.PERSON_FIRST_NAME);
//    genUtil.appendRandName(result, data.PERSON_NAME_SUFFIX);
    return result.join(" ");
}

function randBookName(result) {
    var arr = result || [];
    genUtil.appendRandName(arr, data.BOOK_NAME_PART, 1, 10);
    return result == null ? arr.join(" ") : result;
}


function setUpEntityTypes(context) {
    context.entityTypes = producerUtil.nameValuePairs(function (ins) {
        ins(1, 'author');
        ins(2, 'language');
        ins(3, 'person');
        ins(5, 'book');
        ins(6, 'movie');
        ins(7, 'series');
        ins(8, 'genre');
        ins(9, 'book_origin');
    });
}

function setUpLanguages(context) {
    context.languages = producerUtil.nameValuePairs(function (ins) {
        ins(50, "en");
        ins(51, "ru");
        ins(52, "cn");
    });
}

function setUpGenres(context) {
    context.genres = producerUtil.nameValuePairs(function (ins) {
        ins(101, 'Poetry');
        ins(102, 'Fantasy');
        ins(103, 'Science Fiction');
        ins(105, 'Biography');
        ins(106, 'Novel');
        ins(107, 'Drama');
        ins(114, 'Modern');
        ins(117, 'Classic');
        ins(118, 'History');
        ins(119, 'Adaptation');
        ins(123, 'Tale');
        ins(129, 'Short Story');
        ins(140, 'Realistic Fiction');
        ins(141, 'Folklore');
        ins(145, 'Fable');
        ins(148, 'Speech');
        ins(150, 'Narrative');
        ins(185, 'Essay');
        ins(186, 'Mystery');
    });
}

function lookupItemId(itemMap, itemName) {
  if (itemMap == null) {
    throw new Error("Item map is undefined");
  }

  if (!(itemName in itemMap)) {
    throw new Error("itemName=" + itemName + " is not in target map");
  }

  return itemMap[itemName];
}

function insertOrigins(context) {
   var count = context.originsCount || 3;
    context.origins = producerUtil.nameValuePairs(function (ins) {
        for (var i = 0; i < count; ++i) {
            ins(200 + i, "Origin_" + i);
        }
    });

}

function insertSeries(context) {
    var count = context.seriesCount || 5;
    context.series = producerUtil.nameValuePairs(function (ins) {
        for (var i = 0; i < count; ++i) {
            ins(300 + i, "Series_" + i);
        }
    });
}

function idByEntityTypeName(context, name) {
    if (name in context.entityTypes) {
        return context.entityTypes[name];
    }

    throw new Error("No entity type with name=" + name);
}

function getUniqueName(entityMap, generatorFn) {
    var name;
    for (;;) {
        name = generatorFn();
        if (name in entityMap) {
            continue;
        }
        return name;
    }
}

function insertAuthors(context) {
    var authors = {};
    var count = context.authorCount || 10;

    var next = 1000;
    for (var i = 0; i < count; ++i) {
        next = next + genUtil.rand(1, 3);
        authors[getUniqueName(authors, randPersonName)] = next;
    }

    context.authors = authors;
}

function insertBooks(context) {
    var books = {};
    var count = context.bookCount || 15;

    var next = 1000000;
    for (var i = 0; i < count; ++i) {
        next = next + genUtil.rand(1, 3);
        books[getUniqueName(books, randBookName)] = next;
    }

    context.books = books;
}

function generateRelations(target,
                           maxCount/*2*/,
                           lhsTarget/*context.authors*/,
                           lhsKeyArray/*Object.keys(context.authors)*/,
                           rhsTarget/*context.books*/,
                           rhsKeysArray/*Object.keys(context.books)*/) {
  for (var i = 0; i < rhsKeysArray.length; ++i) {
    var rhsId = rhsTarget[rhsKeysArray[i]];

    var lhsIds = [];
    var genCount = genUtil.rand(1, maxCount + 1);
    for (var j = 0; j < genCount;) {
      var lhsId = lhsTarget[lhsKeyArray[genUtil.rand(0, lhsKeyArray.length)]];
      if (lhsIds.indexOf(lhsId) < 0) {
        lhsIds.push(lhsId);
        ++j;
      }
    }

    target[rhsId] = lhsIds;
  }

  return target;
}

function insertLanguageRelations(context) {
  var cnLangId = lookupItemId(context.languages, 'cn');
  var ruLangId = lookupItemId(context.languages, 'ru');
  var enLangId = lookupItemId(context.languages, 'en');

  context.bookLanguages = {};

  for (var bookName in context.books) {
    var bookId = context.books[bookName];

    // language (just pick random)
    var languageSelectorRand = genUtil.rand(0, 100);
    if (languageSelectorRand < 2) {
      context.bookLanguages[bookId] = [cnLangId]; // 2% - just chinese books
    } else if (languageSelectorRand == 2) { // 1% - books in all languages - en, ru and cn
      context.bookLanguages[bookId] = [cnLangId, ruLangId, enLangId];
    } else if (languageSelectorRand < 7) { // 5% - books in en and ru languages
      context.bookLanguages[bookId] = [ruLangId, enLangId];
    } else if (languageSelectorRand < 30) {
      context.bookLanguages[bookId] = [ruLangId];
    } else {
      context.bookLanguages[bookId] = [enLangId];
    }
  }
}

function insertRelations(context) {
  insertLanguageRelations(context);

  var books = genUtil.randShuffle(Object.keys(context.books));
  var authors = genUtil.randShuffle(Object.keys(context.authors));

  // books w/ one author
  var oneAuthorIdx = Math.floor(books.length * 0.85);
  var twoAuthorIdx = oneAuthorIdx + Math.floor(books.length * 0.1);

  var oneAuthorBooks = books.slice(0, oneAuthorIdx);
  var twoAuthorBooks = books.slice(oneAuthorIdx, twoAuthorIdx);
  var manyAuthorBooks = books.slice(twoAuthorIdx, books.length); // <- up to 30 authors - 0.05%

  // preparation vars
  var bookAuthors = generateRelations({}, 1, context.authors, authors, context.books, oneAuthorBooks);
  bookAuthors = generateRelations(bookAuthors, 2, context.authors, authors, context.books, twoAuthorBooks);

  context.bookAuthors = bookAuthors;
  context.bookGenres = generateRelations({}, 1, context.genres, Object.keys(context.genres), context.books, books);
}

function insertBlock(context, block, comment, name, result) {
    comment(name + " entries");
    var typeId = idByEntityTypeName(context, name);
    Object.keys(block).map(function (name) {
      result.push("INSERT INTO item (id, title, type_id) VALUES (" + block[name] + ", " +
        producerUtil.sqlStringify(name) + ", " + typeId + ");");
    });
}

function insertConcreteRelationBlock(context, comment, result, rhsToLhsMap, idTypeName) {
  comment("book relations: " + idTypeName);
  var relationTypeId = idByEntityTypeName(context, idTypeName);
  Object.keys(rhsToLhsMap).forEach(function (rhsId) {
    var lhsIds = rhsToLhsMap[rhsId];
    lhsIds.forEach(function (lhsId) {
      result.push("INSERT INTO item_relation (lhs, rhs, type_id) VALUES (" +
        lhsId + ", " + rhsId + ", " + relationTypeId + ");");
    });
  });
}

function insertRelationBlock(context, comment, result) {
  insertConcreteRelationBlock(context, comment, result, context.bookAuthors, 'author');
  insertConcreteRelationBlock(context, comment, result, context.bookGenres, 'genre');
  insertConcreteRelationBlock(context, comment, result, context.bookLanguages, 'language');
}

function generateContent(context, result) {
    var comment = function (what) {
        result.push("\n");
        result.push("-- " + what);
    }

    comment("Entity Types");
    Object.keys(context.entityTypes).map(function (name) {
        result.push("INSERT INTO entity_type (id, name) VALUES (" + context.entityTypes[name] + ", " +
            producerUtil.sqlStringify(name) + ");");
    });

    insertBlock(context, context.genres, comment, "genre", result);
    insertBlock(context, context.languages, comment, "language", result);
    insertBlock(context, context.series, comment, "series", result);
    insertBlock(context, context.origins, comment, "book_origin", result);
    insertBlock(context, context.authors, comment, "person", result);
    insertBlock(context, context.books, comment, "book", result);

    insertRelationBlock(context, comment, result);

    result.push('\n');
}

function generateTestDb(context) {
    context = context || {};

    var result = [];

    setUpEntityTypes(context);
    setUpGenres(context);
    setUpLanguages(context);
    insertOrigins(context);
    insertSeries(context);
    insertAuthors(context);
    insertBooks(context);
    insertRelations(context);

    generateContent(context, result);

    if (context.fileName != null) {
        fs.writeFileSync(context.fileName, result.join("\n"));
    } else {
        console.log(result.join("\n"));
    }
}

//
// Command Line Arguments
//

var authorCount = 5;
var originsCount = 2;
var bookCount = 20;
var fileName = '/tmp/liten-out.sql';

// parse command line args

var ARGS = process.argv.slice(2);

function getArgumentOrDefault(argName, fnConverter, defaultValue) {
  var argIdx = ARGS.indexOf(argName);
  if (argIdx < 0) {
    return defaultValue;
  }

  var argStrValue = ARGS[argIdx + 1];
  if (typeof argStrValue !== 'string') {
    console.error('Bad argument value for parameter', argName);
    return defaultValue;
  }

  return fnConverter(argStrValue);
}

authorCount = getArgumentOrDefault("-authorCount", function (a) { return parseInt(a); }, authorCount);
originsCount = getArgumentOrDefault("-originsCount", function (a) { return parseInt(a); }, originsCount);
bookCount = getArgumentOrDefault("-bookCount", function (a) { return parseInt(a); }, bookCount);
fileName = getArgumentOrDefault("-fileName", function (a) { return a; }, fileName);

console.log('Running with arguments: authorCount:', authorCount, ", originsCount", originsCount,
  ", bookCount:", bookCount, ", fileName", fileName);

generateTestDb({
  authorCount: authorCount,
  originsCount: originsCount,
  bookCount: bookCount,
  fileName: fileName
});

console.log('DONE')
