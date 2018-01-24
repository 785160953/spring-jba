/**
 * 
 */
package xin.xihc.jba.db;

import xin.xihc.jba.properties.ColumnProperties;
import xin.xihc.jba.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;

/**
 * mysql数据库操作
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
public class DB_MySql_Opera implements I_TableOperation {

	@Override
	public boolean isTableExists(String tblName, JbaTemplate jbaTemplate) {
		boolean res = false;
		String sql = "select count(1) FROM information_schema.TABLES WHERE table_name ='" + tblName + "'";
		Integer count = jbaTemplate.queryColumn(sql, null, Integer.class);
		if (count > 0) {
			res = true;
		}
		return res;
	}

	@Override
	public void createTable(TableProperties tbl, JbaTemplate jbaTemplate) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
		for (ColumnProperties col : tbl.getColumns().values()) {
			sql.append(col.colName() + " ");
			if (col.type().equals(int.class) || col.type().equals(Integer.class)) {
				sql.append("int");
			} else if (col.type().equals(String.class)) {
				sql.append("varchar");
			} else if (col.type().equals(Double.class) || equals(col.type().equals(double.class))) {
				sql.append("double");
			}
			if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 0) {
				sql.append("(" + col.length() + ")");
			}

			if (CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
				sql.append(" NOT NULL ");
			}
			if (CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
				sql.append(" PRIMARY KEY ");
			}
			if (CommonUtil.isNotNullEmpty(col.unique()) && col.unique()) {
				sql.append(" UNIQUE ");
			}
			sql.append(",");
		}
		sql.deleteCharAt(sql.length() - 1).append(")");
		jbaTemplate.executeSQL(sql.toString());
	}

	@Override
	public void updateTable(TableProperties tbl, JbaTemplate jbaTemplate) {

	}

}
