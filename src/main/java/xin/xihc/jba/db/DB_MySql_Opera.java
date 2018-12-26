/**
 *
 */
package xin.xihc.jba.db;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.db.bean.MysqlColumnInfo;
import xin.xihc.jba.tables.InitDataInterface;
import xin.xihc.jba.tables.properties.ColumnProperties;
import xin.xihc.jba.tables.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * mysql数据库操作
 *
 * @author Leo.Xi
 * @version 1.3.0 不再支持java基本数据类型
 * @date 2018年1月24日
 * @desc 增加:对java.sql.Date,Time,Timestamp字段的支持
 * @since 1.1.4
 */
public class DB_MySql_Opera implements I_TableOperation {

	private String table_schema; // 数据库schema

	private JbaTemplate jbaTemplate;

	private static Map<Class, String> javaClassToMysqlFieldName = new HashMap<>();

	static {
		//		javaClassToMysqlFieldName.put(byte.class, "tinyint");
		javaClassToMysqlFieldName.put(Byte.class, "tinyint");
		//		javaClassToMysqlFieldName.put(short.class, "smallint");
		javaClassToMysqlFieldName.put(Short.class, "smallint");
		//		javaClassToMysqlFieldName.put(int.class, "int");
		javaClassToMysqlFieldName.put(Integer.class, "int");
		//		javaClassToMysqlFieldName.put(long.class, "bigint");
		javaClassToMysqlFieldName.put(Long.class, "bigint");
		//		javaClassToMysqlFieldName.put(String.class, "varchar"); // String为默认，不需要加上
		//		javaClassToMysqlFieldName.put(double.class, "double");
		javaClassToMysqlFieldName.put(Double.class, "double");
		//		javaClassToMysqlFieldName.put(float.class, "double");
		javaClassToMysqlFieldName.put(Float.class, "double");
		javaClassToMysqlFieldName.put(BigDecimal.class, "decimal");
		javaClassToMysqlFieldName.put(java.util.Date.class, "datetime");
		javaClassToMysqlFieldName.put(java.sql.Timestamp.class, "timestamp");
		javaClassToMysqlFieldName.put(java.sql.Date.class, "date");
		javaClassToMysqlFieldName.put(java.sql.Time.class, "time");
		//		javaClassToMysqlFieldName.put(boolean.class, "tinyint");
		javaClassToMysqlFieldName.put(Boolean.class, "tinyint");
	}

	public DB_MySql_Opera(JbaTemplate jbaTemplate) {
		this.jbaTemplate = jbaTemplate;
		table_schema = getSchema();
	}

	/**
	 * 先获取当前数据库名
	 *
	 * @return
	 */
	private String getSchema() {
		return jbaTemplate.queryColumn("select database()", null, String.class);
	}

	@Override
	public boolean isTableExists(final String tblName) {
		boolean res = false;
		String sql = "select count(1) FROM information_schema.TABLES WHERE table_name ='" + tblName + "' AND table_schema='" + this.table_schema + "'";
		Integer count = jbaTemplate.queryColumn(sql, null, Integer.class);
		if (count > 0) {
			res = true;
		}
		return res;
	}

	@Transactional(rollbackFor = DataAccessException.class)
	@Override
	public void createTable(TableProperties tbl) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			sql.append(columnPro(col, after, true));
			sql.append(",");
			after = col.colName();
		}
		sql.deleteCharAt(sql.length() - 1)
		   .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '" + tbl.getRemark() + "';");
		jbaTemplate.executeSQL(sql.toString());
		// 初始化数据
		if (tbl.getTableBean() instanceof InitDataInterface) {
			Thread thread = new Thread(() -> ((InitDataInterface) tbl.getTableBean()).doInit(jbaTemplate));
			thread.setName("initDataThread-" + tbl.getTableName());
			thread.start();
		}
	}

	/**
	 * 根据数据库列类型返回java数据类型
	 *
	 * @param dataType 列类型
	 * @return
	 */
	private Class getClassByColumnDataType(String dataType) {
		Set<Class> keySet = javaClassToMysqlFieldName.keySet();
		for (Class key : keySet) {
			if (javaClassToMysqlFieldName.get(key).equals(dataType)) {
				return key;
			}
		}
		return String.class;
	}

	/**
	 * 获取数据库中的表结构信息
	 *
	 * @param tableName 表名
	 * @return
	 */
	private List<ColumnProperties> getColumnsInfo(final String tableName) {
		List<MysqlColumnInfo> list = jbaTemplate.queryMixModelList(
				"select * from information_schema.columns where table_name = '" + tableName + "' AND table_schema='" + this.table_schema + "'",
				null, MysqlColumnInfo.class, null);
		if (list.size() < 1) {
			return new ArrayList<>(0);
		}
		List<ColumnProperties> result = new ArrayList(list.size());
		ColumnProperties prop;
		for (MysqlColumnInfo item : list) {
			prop = new ColumnProperties();
			prop.colName(item.getColumn_name());
			prop.type(getClassByColumnDataType(item.getData_type()));
			prop.defaultValue(CommonUtil.isNullEmpty(item.getColumn_default()) ? "" : item.getColumn_default());
			prop.remark(CommonUtil.isNullEmpty(item.getColumn_comment()) ? "" : item.getColumn_comment());
			prop.notNull("NO".equals(item.getIs_nullable()));
			if ("PRI".equals(item.getColumn_key())) { // 是主键
				prop.primary(true);
				if ("auto_increment".equals(item.getExtra())) { // 自增主键
					prop.policy(Column.Policy.AUTO);
					prop.length(item.getNumeric_precision());
					prop.precision(item.getNumeric_scale());
				} else if (("varchar".equals(item.getData_type()) || "char".equals(item.getData_type())) && item
						.getCharacter_maximum_length() == 32) {
					prop.policy(Column.Policy.GUID);
					prop.length(item.getCharacter_maximum_length());
				}
			}
			if ("varchar".equals(item.getData_type()) || "char".equals(item.getData_type())) {
				prop.length(item.getCharacter_maximum_length());
			} else if ("text".equals(item.getData_type())) {// text字段长度为65535
				prop.length(65535);
			} else if (Number.class.isAssignableFrom(prop.type())) { // 如果是数值的
				prop.length(item.getNumeric_precision());
				prop.precision(item.getNumeric_scale());
			}
			result.add(prop);
		}
		return result;
	}

	@Transactional(rollbackFor = DataAccessException.class)
	@Override
	public void updateTable(TableProperties tbl) {
		// 先获取表结构信息
		List<ColumnProperties> dbColumnPropsList = getColumnsInfo(tbl.getTableName());

		ArrayList<String> sqls = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + tbl.getTableName() + " ");
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			boolean is2Add = true;

			for (ColumnProperties item : dbColumnPropsList) {
				if (col.colName().toLowerCase().equals(item.colName().toLowerCase())) { // 相同列
					is2Add = false;
					if (!Objects.equals(javaClassToMysqlFieldName.get(col.type()),
							javaClassToMysqlFieldName.get(item.type())) || !item.equals(col)) {// 如果对应的数据库字段类型不一样
						if (col.primary()) { // 同一列，主键设置的不一致
							if (item.policy() == Column.Policy.AUTO) {
								String ss = "ALTER TABLE " + tbl.getTableName() + " MODIFY " + item
										.colName() + " int," + "DROP PRIMARY KEY";
								jbaTemplate.executeSQL(ss);
							} else {
								String ss = "ALTER TABLE " + tbl.getTableName() + " DROP PRIMARY KEY";
								jbaTemplate.executeSQL(ss);
							}
						}
						sqls.add("MODIFY " + columnPro(col, after, false));
					}
					after = col.colName();
					dbColumnPropsList.remove(item);
					break;
				}
			}
			// 是新增列
			if (is2Add) {
				sqls.add("ADD COLUMN " + columnPro(col, after, false));
				after = col.colName();
			}
		}
		// 最后list剩余的则是需要删除的列
		for (ColumnProperties item : dbColumnPropsList) {
			sqls.add("DROP COLUMN " + item.colName());
		}
		for (int i = 0; i < sqls.size(); i++) {
			sql.append(sqls.get(i) + ",");
		}
		sql.append(" COMMENT = '" + tbl.getRemark() + "'");
		jbaTemplate.executeSQL(sql.toString());
	}

	/**
	 * 需要设置精度
	 *
	 * @param clazz
	 * @return
	 */
	private boolean needPrecision(Class clazz) {
		if (clazz.equals(Double.class) || clazz.equals(double.class) || clazz.equals(float.class) || clazz
				.equals(Float.class)) {
			return true;
		}
		if (clazz.equals(BigDecimal.class)) {
			return true;
		}
		return false;
	}

	/**
	 * 得到字段名+属性拼接
	 *
	 * @param col
	 * @return
	 */
	private String columnPro(ColumnProperties col, String after, boolean isCreate) {
		StringBuilder temp = new StringBuilder();
		temp.append(col.colName() + " ");
		if (javaClassToMysqlFieldName.containsKey(col.type())) {
			temp.append(javaClassToMysqlFieldName.get(col.type()));
			if (needPrecision(col.type())) { // 需要精度的
				temp.append("(" + col.length() + "," + col.precision() + ")");
			}
		} else {
			if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 20000) {
				temp.append("text");
				if (isCreate) {
					temp.append(" BINARY");
				}
			} else {
				temp.append("varchar");
				if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 0) {
					temp.append("(" + col.length() + ")");
				}
				if (isCreate) {
					temp.append(" BINARY");
				}
			}
		}
		if (CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
			temp.append(" PRIMARY KEY ");
			switch (col.policy()) {
				case AUTO:
					temp.append(" AUTO_INCREMENT ");
					break;
				default:
					break;
			}
		} else {
			if (CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
				temp.append(" NOT NULL ");
			} else {
				temp.append(" NULL ");
			}
		}
		if (CommonUtil.isNotNullEmpty(col.remark())) {
			temp.append(" COMMENT '" + col.remark() + "'");
		}
		if (CommonUtil.isNotNullEmpty(col.defaultValue())) {
			if (col.type().equals(String.class)) {
				temp.append(" DEFAULT '" + col.defaultValue() + "'");
			} else if (col.type().isEnum()) {
				temp.append(" DEFAULT '" + col.defaultValue() + "'");
			} else if (Number.class.isAssignableFrom(col.type())) {
				temp.append(" DEFAULT " + col.defaultValue());
			} else if (col.type().equals(Timestamp.class)) {
				temp.append(" DEFAULT " + col.defaultValue());
			}
		} else if (!col.notNull()) {
			temp.append(" DEFAULT null ");
		}
		if (!isCreate) {
			if (CommonUtil.isNotNullEmpty(after)) {
				temp.append(" AFTER " + after);
			} else {
				temp.append(" FIRST");
			}
		}
		return temp.toString();
	}

	@Override
	public void dropTable(TableProperties tbl) {
		jbaTemplate.executeSQL("DROP TABLE " + tbl.getTableName());
	}

}
