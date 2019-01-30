package xin.xihc.jba.utils;

import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.core.PageInfo;
import xin.xihc.jba.tables.TableManager;
import xin.xihc.jba.tables.properties.ColumnProperties;
import xin.xihc.jba.tables.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

import java.lang.reflect.Field;
import java.util.*;
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
	 * Class的字段列表缓存
	 */
	private static LinkedHashMap<Class<?>, List<Field>> classFieldsCache = new LinkedHashMap<>(16);

	/**
	 * 增加缓存功能
	 *
	 * @param clazz
	 * @return
	 * @since 1.3.3
	 */
	public static List<Field> getModelFields(Class<?> clazz) {
		if (null == clazz) {
			return new ArrayList<>(0);
		}

		if (classFieldsCache.containsKey(clazz)) {
			return classFieldsCache.get(clazz);
		}

		List<Field> allFields = CommonUtil.getAllFields(clazz, false, false);
		classFieldsCache.put(clazz, allFields);
		return allFields;
	}

	/**
	 * 获取where子句后面的拼接name=:name
	 *
	 * @param model 表对象
	 * @return sql
	 */
	public static String getWhereSql(Object model) {
		Objects.requireNonNull(model, "nonNull");

		List<Field> allFields = getModelFields(model.getClass());
		StringJoiner where = new StringJoiner(AND);
		allFields.stream().forEach(field -> {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					where.add(field.getName() + "=:" + field.getName());
				}
			} catch (Exception e) {
				LogFileUtil.exception(JbaTemplate.jbaLogName, e);
				e.printStackTrace();
			}
		});
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
		if (table == null) {
			throw new RuntimeException("该对象并不是表对象");
		}
		List<Field> allFields = getModelFields(model.getClass());
		allFields.stream().forEach(field -> {
			try {
				field.setAccessible(true);
				ColumnProperties col = table.getColProperties(field.getName());
				if (col.policy() == Column.Policy.GUID) {
					if (field.get(model) == null) {
						field.set(model, CommonUtil.newGuid(false));
					}
				} else if (col.policy() == Column.Policy.GUID_UP) {
					if (field.get(model) == null) {
						field.set(model, CommonUtil.newGuid(true));
					}
				}
			} catch (Exception e) {
				LogFileUtil.exception(JbaTemplate.jbaLogName, e);
				e.printStackTrace();
			}
		});
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
		if (null == fieldNames || fieldNames.length < 1) { // 寻找主键
			if (null != tableProperties) {
				LinkedHashMap<String, ColumnProperties> columns = tableProperties.getColumns();
				for (ColumnProperties columnProperties : columns.values()) {
					if (columnProperties.primary()) {
						fieldNames = new String[1];
						fieldNames[0] = columnProperties.colName();
						break;
					}
				}
			}
		}
		// 没有主键
		if (null == fieldNames || fieldNames.length < 1) {
			throw new RuntimeException(String.format("表【%s】没有设置主键", tableProperties.getTableName()));
		}
		// 转小写
		List<String> fieldList = Stream.of(fieldNames).map(val -> val.toLowerCase()).collect(Collectors.toList());

		StringJoiner fValues = new StringJoiner(",");
		StringJoiner wValues = new StringJoiner(AND);

		// 使用jdk8的stream语句
		getModelFields(model.getClass()).stream().forEach(field -> {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					if (!fieldList.contains(field.getName().toLowerCase())) {
						fValues.add(field.getName() + "=:" + field.getName());
					} else {
						wValues.add(field.getName() + "=:" + field.getName());
					}
				} else if (fieldList.contains(field.getName().toLowerCase())) {
					throw new RuntimeException("WHERE子句中存在字段【" + field.getName() + "】值为空");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogFileUtil.exception(JbaTemplate.jbaLogName, e);
				e.printStackTrace();
			}
		});
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
		// 使用jdk8的stream语句
		getModelFields(model.getClass()).stream().forEach(field -> {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					fValues.add(field.getName());
					vValues.add(":" + field.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogFileUtil.exception(JbaTemplate.jbaLogName, e);
				e.printStackTrace();
			}
		});
		if (fValues.length() < 1) {
			throw new RuntimeException("属性都为空,请确认");
		}
		return "INSERT INTO " + tableProperties.getTableName() + "(" + fValues.toString() + ") VALUES (" + vValues
				.toString() + ")";
	}

}
