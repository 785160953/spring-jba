package xin.xihc.jba.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表管理
 * 
 * @author 席恒昌
 * @Date 2018年1月21日
 *
 */
@Component
public class TableManager {

	@Autowired
	MyOrmJdbcTemplate jdbcTemplate;

	public boolean isTableExists(String tblName) {
		boolean res = false;
		String sql = "";
		switch (jdbcTemplate.getDbType()) {
		case MySql:
			sql = "select count(1) FROM information_schema.TABLES WHERE table_name ='" + tblName + "'";
			Integer count = jdbcTemplate.queryColumn(sql, null, Integer.class);
			if (count > 0) {
				res = true;
			}
			break;
		case Oracle:

			break;
		default:
			break;
		}
		return res;
	}

	/**
	 * 初始化表结构
	 * 
	 * @param tblName
	 */
	public void initTable(TableProperties tbl) {
		StringBuilder sql = new StringBuilder();
		// 表存在则更新字段
		if (isTableExists(tbl.getTableName())) {

		} else {
			sql.append("CREATE TABLE " + tbl.getTableName() + " (");
			for (ColumnProperties col : tbl.getColumns().values()) {
				sql.append(col.colName() + " ");
				if (col.type().equals(int.class) || col.type().equals(Integer.class)) {
					sql.append("int");
				} else if (col.type().equals(String.class)) {
					sql.append("varchar");
				}
				if (col.length() > 0) {
					sql.append("(" + col.length() + ")");
				}

				if (col.notNull()) {
					sql.append(" NOT NULL ");
				}
				if (col.primary()) {
					sql.append(" PRIMARY KEY ");
				}
				if (col.unique()) {
					sql.append(" UNIQUE ");
				}
				sql.append(",");
			}
			sql.append(")");
		}
		jdbcTemplate.executeSQL(sql.toString());
	}

	/**
	 * 初始化
	 */
	@Transactional
	public void init() {
		for (TableProperties tblObj : TableUtils.getTbls().values()) {
			initTable(tblObj);
		}
	}

}
