package liten.tool.bm.transfer.util.flibusta;

import com.truward.time.UtcTime;
import com.truward.time.jdbc.UtcTimeSqlUtil;
import liten.tool.bm.transfer.support.model.BookMeta;
import liten.tool.bm.transfer.support.model.NamedValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Flibusta DB mappers.
 */
public final class FlibustaMappers {
  private FlibustaMappers() {}

  public static final class NamedValueRowMapper implements RowMapper<NamedValue> {
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

  public static final class BookMetaRowMapper implements RowMapper<BookMeta> {

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
