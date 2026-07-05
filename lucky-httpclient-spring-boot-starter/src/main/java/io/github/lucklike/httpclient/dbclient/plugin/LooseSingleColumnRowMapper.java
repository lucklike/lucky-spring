package io.github.lucklike.httpclient.dbclient.plugin;

import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 宽松的SingleColumnRowMapper
 *
 * @param <T> 类型泛型
 */
public class LooseSingleColumnRowMapper<T> extends SingleColumnRowMapper<T> {

    @NonNull
    private final Class<?> _requiredType;

    /**
     * Create a new {@code SingleColumnRowMapper}.
     *
     * @param requiredType the type that each result object is expected to match
     */
    public LooseSingleColumnRowMapper(@NonNull Class<T> requiredType) {
        super(Objects.requireNonNull(requiredType));
        _requiredType = requiredType;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Validate column count.
        ResultSetMetaData rsmd = rs.getMetaData();

        // Extract column value from JDBC ResultSet.
        Object result = getColumnValue(rs, 1, this._requiredType);
        if (result != null && !this._requiredType.isInstance(result)) {
            // Extracted value does not match already: try to convert it.
            try {
                return (T) convertValueToRequiredType(result, this._requiredType);
            }
            catch (IllegalArgumentException ex) {
                throw new TypeMismatchDataAccessException(
                        "Type mismatch affecting row number " + rowNum + " and column type '" +
                                rsmd.getColumnTypeName(1) + "': " + ex.getMessage());
            }
        }
        return (T) result;
    }

}
