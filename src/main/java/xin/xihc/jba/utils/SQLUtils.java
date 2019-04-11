package xin.xihc.jba.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import xin.xihc.jba.core.PageInfo;
import xin.xihc.jba.tables.TableManager;
import xin.xihc.jba.tables.properties.ColumnProperties;
import xin.xihc.jba.tables.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;

import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * sql工具
 *
 * @author Leo.Xi
 * @version 1.0
 * @since 1.0
 */
public class SQLUtils {

	public static final String AND = " AND ";

	/**
	 * 获取where子句后面的拼接name=:name
	 *
	 * @param model 表对象
	 * @return sql
	 */
	public static String getWhereSql(Object model) {
		Objects.requireNonNull(model, "model is null");

		TableProperties tableProperties = TableManager.getTable(model.getClass());

		StringJoiner where = new StringJoiner(AND);
		LinkedHashMap<String, ColumnProperties> columns = tableProperties.getColumns();
		for (String field : columns.keySet()) {
			PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(model.getClass(), field);
			try {
				Object value = propertyDescriptor.getReadMethod().invoke(model);
				if (value != null) {
					where.add(columns.get(field).colName() + "=:" + field);
				}
			} catch (Exception e) {
				JbaLog.error(e);
			}
		}
		return where.toString();
	}

	/**
	 * 获取分页sql
	 *
	 * @param sql      sql语句
	 * @param pageInfo 分页信息
	 * @return 分页后的sql
	 */
	public static String getPageSql(final String sql, PageInfo pageInfo) {
		String pageSql = sql;
		if (null == pageInfo) {
			return pageSql;
		}
		// 计算起始索引
		// 使用limit 0, 10分页 -- 索引从0开始
		int iBegin = (pageInfo.getPageNo() - 1) * pageInfo.getPageSize();
		pageSql = sql + " LIMIT " + iBegin + "," + pageInfo.getPageSize();
		return pageSql;
	}

	/**
	 * 填充GUID
	 *
	 * @param model 需要填充的表对象
	 * @return 返回填充后的表对象
	 */
	public static void fillGuid(Object model) {
		TableProperties table = TableManager.getTable(model.getClass());

		LinkedHashMap<String, ColumnProperties> columns = table.getColumns();
		for (String field : columns.keySet()) {
			try {
				ColumnProperties col = columns.get(field);
				PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(model.getClass(), field);
				Object value = propertyDescriptor.getReadMethod().invoke(model);
				if (value == null) {
					switch (col.policy()) {
						case GUID:
							propertyDescriptor.getWriteMethod().invoke(model, CommonUtil.newGuid(false));
							break;
						case GUID_UP:
							propertyDescriptor.getWriteMethod().invoke(model, CommonUtil.newGuid(true));
							break;
						default:
							break;
					}
				}
			} catch (Exception e) {
				JbaLog.error(e);
			}
		}
	}

	/**
	 * Delete语句拼接
	 *
	 * @param model 参数对象
	 * @return 删除数据的sql
	 * @throws RuntimeException 运行期异常
	 */
	public static String getDeleteSql(Object model) {
		Objects.requireNonNull(model, "表对象model不允许为空");

		TableProperties tableProperties = TableManager.getTable(model.getClass());

		String where = getWhereSql(model);
		if (where.length() < 1) {
			throw new RuntimeException("表对象中字段全为null");
		}
		return "DELETE FROM " + tableProperties.getTableName() + " WHERE " + where;
	}

	/**
	 * Update语句拼接
	 *
	 * @param model      参数对象
	 * @param fieldNames 根据的字段
	 * @return
	 * @throws RuntimeException
	 */
	public static String getUpdateSql(Object model, String... fieldNames) throws RuntimeException {
		Objects.requireNonNull(model, "表对象model不允许为空");

		TableProperties tableProperties = TableManager.getTable(model.getClass());
		LinkedHashMap<String, ColumnProperties> columns = tableProperties.getColumns();
		if (null == fieldNames || fieldNames.length < 1) { // 寻找主键
			for (String field : columns.keySet()) {
				if (columns.get(field).primary()) {
					fieldNames = new String[1];
					fieldNames[0] = field;
					break;
				}
			}
		}
		// 没有主键
		if (null == fieldNames || fieldNames.length < 1) {
			throw new RuntimeException(String.format("表【%s】没有设置主键", tableProperties.getTableName()));
		}
		// 转小写
		List<String> fieldList = Stream.of(fieldNames).map(String::toLowerCase).collect(Collectors.toList());

		StringJoiner fValues = new StringJoiner(",");
		StringJoiner wValues = new StringJoiner(AND);
		for (String field : columns.keySet()) {
			try {
				PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(model.getClass(), field);
				Object value = propertyDescriptor.getReadMethod().invoke(model);
				if (value != null) {
					if (!fieldList.contains(field.toLowerCase())) {
						fValues.add(columns.get(field).colName() + "=:" + field);
					} else {
						wValues.add(columns.get(field).colName() + "=:" + field);
					}
				} else if (fieldList.contains(field.toLowerCase())) {
					throw new RuntimeException("WHERE子句中存在字段【" + field + "】值为空");
				}
			} catch (Exception e) {
				JbaLog.error(e);
			}
		}

		if (fValues.length() < 1) {
			throw new RuntimeException("无属性需要更新");
		}

		return "UPDATE " + tableProperties.getTableName() + " SET " + fValues.toString() + " WHERE " + wValues
				.toString();
	}

	/**
	 * insert语句拼接
	 *
	 * @param model 对象参数
	 * @return
	 */
	public static String getInsertSql(Object model) {
		Objects.requireNonNull(model, "表对象model不允许为空");

		TableProperties tableProperties = TableManager.getTable(model.getClass());

		// 填充需要填入的GUID
		fillGuid(model);

		StringJoiner fValues = new StringJoiner(",");
		StringJoiner vValues = new StringJoiner(",");
		LinkedHashMap<String, ColumnProperties> columns = tableProperties.getColumns();
		for (String field : columns.keySet()) {
			try {
				PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(model.getClass(), field);
				Object value = propertyDescriptor.getReadMethod().invoke(model);
				if (value != null) {
					fValues.add(columns.get(field).colName());
					vValues.add(":" + field);
				}
			} catch (Exception e) {
				JbaLog.error(e);
			}
		}

		if (fValues.length() < 1) {
			throw new RuntimeException("属性都为空,请确认");
		}
		return "INSERT INTO " + tableProperties.getTableName() + "(" + fValues.toString() + ") VALUES (" + vValues
				.toString() + ")";
	}

	/**
	 * 转换字段为数据库列名
	 *
	 * @param bys
	 * @return
	 */
	public static String getOrderBy(String... bys) {
		StringJoiner order = new StringJoiner(",");
		for (String by : bys) {
			String[] fields = by.trim().split(",");
			for (String field : fields) {
				String[] split = field.trim().split(" ");
				if (split.length == 1) {
					order.add(underscoreName(split[0]));
				} else if (split.length == 2) {
					order.add(underscoreName(split[0]) + " " + split[1]);
				}
			}
		}
		return order.toString();
	}

	/**
	 * 驼峰转为下划线
	 *
	 * @param name
	 * @return
	 */
	public static String underscoreName(String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(name.substring(0, 1).toLowerCase());
		for (int i = 1; i < name.length(); i++) {
			String s = name.substring(i, i + 1);
			String slc = s.toLowerCase();
			if (!s.equals(slc)) {
				result.append("_").append(slc);
			} else {
				result.append(s);
			}
		}
		return result.toString();
	}

}
