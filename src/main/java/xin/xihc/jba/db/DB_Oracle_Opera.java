/**
 * 
 */
package xin.xihc.jba.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xin.xihc.jba.db.bean.MysqlColumnInfo;
import xin.xihc.jba.properties.ColumnProperties;
import xin.xihc.jba.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

/**
 * oracle数据库操作
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
public class DB_Oracle_Opera implements I_TableOperation {

	public final static String log_name = "DB_Update_Oracle_Sql";

	@Override
	public boolean isTableExists(String tblName, JbaTemplate jbaTemplate) {
		Integer count = jbaTemplate.queryColumn(
				"select Count(1) from  user_tables where table_name='" + tblName.toUpperCase() + "'", null,
				Integer.class);
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void createTable(TableProperties tbl, JbaTemplate jbaTemplate) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			sql.append(columnPro(col, after));
			sql.append(",");
			after = col.colName();
		}
		sql.deleteCharAt(sql.length() - 1).append(")DEFAULT CHARSET=utf8;");
		jbaTemplate.executeSQL(sql.toString());
		LogFileUtil.info(log_name, "创建表【" + tbl.getTableName() + "】语句：" + sql.toString());
	}

	@Override
	public void updateTable(TableProperties tbl, JbaTemplate jbaTemplate) {
		// 先获取表结构信息
		List<MysqlColumnInfo> list = jbaTemplate.queryMixModelList(
				"select * from user_tab_columns where table_name = '" + tbl.getTableName().toUpperCase() + "'", null,
				MysqlColumnInfo.class, null);
		ArrayList<String> sqls = new ArrayList<>(10);
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + tbl.getTableName() + " ");
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			boolean is2Add = true;

			for (MysqlColumnInfo item : list) {
				if (col.colName().toLowerCase().equals(item.getColumn_name().toLowerCase())) {
					is2Add = false;
					// 首先删除主键,若主键是自增的先去掉自增
					if ("PRI".equals(item.getColumn_key())) {
						if ("auto_increment".equals(item.getExtra())) {
							String ss = "ALTER TABLE " + tbl.getTableName() + " MODIFY " + item.getColumn_name()
									+ " int,DROP PRIMARY KEY";
							jbaTemplate.executeSQL(ss);
							LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() + "】先删除主键、自增：" + ss);
						} else {
							String ss = "ALTER TABLE " + tbl.getTableName() + " DROP PRIMARY KEY";
							jbaTemplate.executeSQL(ss);
							LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() + "】先删除主键：" + ss);
						}
					}
					sqls.add("MODIFY " + columnPro(col, after));
					after = col.colName();

					list.remove(item);
					break;
				}
			}
			// 是新增列
			if (is2Add) {
				sqls.add("ADD COLUMN_NAME " + columnPro(col, after));
				after = col.colName();
			}
		}
		// 最后list剩余的则是需要删除的列
		for (MysqlColumnInfo item : list) {
			sqls.add("DROP COLUMN " + item.getColumn_name());
		}
		for (int i = 0; i < sqls.size(); i++) {
			if (i == sqls.size() - 1) {
				sql.append(sqls.get(i));
			} else {
				sql.append(sqls.get(i) + ",");
			}
		}
		jbaTemplate.executeSQL(sql.toString());
		LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() + "】语句：" + sql.toString());
	}

	/**
	 * 得到字段名+属性拼接
	 * 
	 * @param col
	 * @return
	 */
	private String columnPro(ColumnProperties col, String after) {
		StringBuilder temp = new StringBuilder();
		temp.append(col.colName() + " ");
		if (col.type().equals(int.class) || col.type().equals(Integer.class)) {
			temp.append("int");
		} else if (col.type().equals(String.class)) {
			temp.append("varchar");
			if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 0) {
				temp.append("(" + col.length() + ")");
			}
		} else if (col.type().equals(Double.class) || col.type().equals(double.class) || col.type().equals(float.class)
				|| col.type().equals(Float.class)) {
			temp.append("double");
			if (col.length() > 0 || col.precision() > 0 && col.length() >= col.precision()) {
				temp.append("(" + col.length() + "," + col.precision() + ")");
			}
		} else if (col.type().equals(BigDecimal.class)) {
			temp.append("decimal");
			if (col.length() < 65 && col.precision() >= 0 && col.length() >= col.precision()) {
				temp.append("(" + col.length() + "," + col.precision() + ")");
			}
		} else if (col.type().equals(Date.class)) {
			temp.append("datetime");
		} else {
			temp.append("varchar(255)");
		}
		if (CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
			temp.append(" PRIMARY KEY ");
			switch (col.policy()) {
			case NONE:
				break;
			case AUTO:
				temp.append(" AUTO_INCREMENT ");
				break;
			case GUID:

				break;
			case GUID_UP:

				break;
			default:
				break;
			}
		} else {
			if (CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
				temp.append(" NOT NULL ");
			}
		}
		if (CommonUtil.isNotNullEmpty(col.remark())) {
			temp.append(" COMMENT '" + col.remark() + "'");
		}
		if (CommonUtil.isNotNullEmpty(col.defaultValue())) {
			temp.append(" DEFAULT '" + col.defaultValue() + "'");
		}
		if (CommonUtil.isNotNullEmpty(after)) {
			temp.append(" AFTER " + after);
		} else {
			temp.append(" FIRST");
		}
		return temp.toString();
	}

}
