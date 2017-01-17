package liten.tool.bm.transfer.support;

import com.truward.time.UtcTime;
import com.truward.time.jdbc.UtcTimeSqlUtil;
import liten.tool.bm.transfer.TransferService;
import liten.tool.bm.transfer.support.model.BookMeta;
import liten.tool.bm.transfer.support.model.NamedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of data transfer service
 */
public final class BooklibTransferService implements TransferService {

  private static final int BOOK_TRANSFER_LIMIT = 1000;

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JdbcOperations db;

  private Long bookTypeId;
  private Long authorTypeId;
  private Long genreTypeId;
  private Long originTypeId;
  private Long languageTypeId;
  private Long seriesTypeId;

  private Map<Long, Long> genreToItem;
  private Map<Long, Long> personToItem;
  private Map<Long, Long> originToItem;
  private Map<Long, Long> langToItem;
  private Map<Long, Long> seriesToItem;

  public BooklibTransferService(JdbcOperations db) {
    this.db = db;
  }

  @Override
  public boolean prepare() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long transferNext(Long startId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void complete() {
    throw new UnsupportedOperationException();
  }

  //
  // Private
  //

  private Long getOrAddEntityType(String entityName) {
    final List<Long> existingIds = db.queryForList("SELECT id FROM entity_type WHERE name=?", Long.class, entityName);
    if (!existingIds.isEmpty()) {
      assert existingIds.size() == 1;
      return existingIds.get(0);
    }

    final Long id = getNextEntityTypeId();
    db.update("INSERT INTO entity_type (id, name) VALUES (?, ?)", id, entityName);
    return id;
  }

  private Long getNextEntityTypeId() {
    return db.queryForObject("SELECT seq_entity_type.nextval", Long.class);
  }


  private static final class NamedValueRowMapper implements RowMapper<NamedValue> {
    private final String name;

    public NamedValueRowMapper(String name) {
      this.name = name;
    }

    @Override
    public NamedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
      final NamedValue result = new NamedValue();
      result.id = rs.getLong("id");
      result.name = rs.getString(name);
      return result;
    }
  }

  private static final class BookMetaRowMapper implements RowMapper<BookMeta> {

    @Override
    public BookMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
      final BookMeta result = new BookMeta();
      result.id = rs.getLong("id");
      result.title = rs.getString("title");
      result.fileSize = rs.getInt("f_size");
      result.dateAdded = UtcTimeSqlUtil.getNullableUtcTime(rs, "add_date", UtcTime.now());
      result.langId = rs.getLong("lang_id");
      result.originId = rs.getLong("origin_id");
      return result;
    }
  }
}
