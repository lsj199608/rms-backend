package rms.dashboard.mapper;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter, java.sql.Types.OTHER);
	}

	@Override
	public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Object obj = rs.getObject(columnName);
		return toUuid(obj);
	}

	@Override
	public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Object obj = rs.getObject(columnIndex);
		return toUuid(obj);
	}

	@Override
	public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Object obj = cs.getObject(columnIndex);
		return toUuid(obj);
	}

	private static UUID toUuid(Object obj) {
		if (obj == null) {
			return null;
	}
		if (obj instanceof UUID) {
			return (UUID) obj;
		}
		return UUID.fromString(obj.toString());
	}
}
