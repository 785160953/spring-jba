package xin.xihc.jba.core;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

import java.sql.Types;

/**
 * 增加对java枚举类型的支持
 *
 * @author Leo.Xi
 * @Date 2018年5月2日
 */
public class JbaBeanProperty extends BeanPropertySqlParameterSource {

	public JbaBeanProperty(Object object) {
		super(object);
	}

	@Override
	public int getSqlType(String paramName) {
		int sqlType = super.getSqlType(paramName);
		if (sqlType == TYPE_UNKNOWN && hasValue(paramName)) {
			if (null != getValue(paramName) && getValue(paramName).getClass().isEnum()) {
				sqlType = Types.VARCHAR;
			}
		}
		return sqlType;
	}

}
