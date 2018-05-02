package xin.xihc.jba.db;

import java.sql.Types;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

/**
 * 增加对java枚举类型的支持
 * 
 * @author 席恒昌
 * @Date 2018年5月2日
 *
 */
public class JbaBeanProperty extends BeanPropertySqlParameterSource {

	public JbaBeanProperty(Object object) {
		super(object);
	}

	@Override
	public int getSqlType(String paramName) {
		int sqlType = super.getSqlType(paramName);
		if (sqlType == TYPE_UNKNOWN && hasValue(paramName)) {
			if (getValue(paramName).getClass().isEnum()) {
				sqlType = Types.VARCHAR;
			}
		}
		return sqlType;
	}

}
