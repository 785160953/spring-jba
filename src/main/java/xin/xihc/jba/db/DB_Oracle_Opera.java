/**
 * 
 */
package xin.xihc.jba.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xin.xihc.jba.db.bean.OracleColumnInfo;
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
			sql.append(columnPro(col, after, true));
			sql.append(",");
			after = col.colName();
		}
		sql.deleteCharAt(sql.length() - 1).append(")");
		jbaTemplate.executeSQL(sql.toString());
		LogFileUtil.info(log_name, "创建表【" + tbl.getTableName() + "】语句：" + sql.toString());
		// 添加备注
		for (ColumnProperties col : tbl.getColumns().values()) {
			String addComment = addComment(tbl.getTableName(), col);
			if (null == addComment) {
				continue;
			}
			jbaTemplate.executeSQL(addComment);
			LogFileUtil.info(log_name, "添加表【" + tbl.getTableName() + "】字段备注：" + addComment);
		}
		// 初始化数据
		Object[] initData = tbl.initData();
		if (null != initData) {
			for (Object obj : initData) {
				jbaTemplate.insertModel(obj);
			}
		}
	}

	@Override
	public void updateTable(TableProperties tbl, JbaTemplate jbaTemplate) {
		// 先获取表结构信息
		List<OracleColumnInfo> list = jbaTemplate.queryMixModelList(
				"select * from user_tab_columns where table_name = '" + tbl.getTableName().toUpperCase() + "'", null,
				OracleColumnInfo.class, null);
		ArrayList<String> sqls = new ArrayList<>(10);
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			boolean is2Add = true;

			for (OracleColumnInfo item : list) {
				if (col.colName().toLowerCase().equals(item.getColumn_name().toLowerCase())) {
					is2Add = false;
					// 首先删除主键,若主键是自增的先去掉自增
					/*
					 * if ("PRI".equals(item.getColumn_key())) { if
					 * ("auto_increment".equals(item.getExtra())) { String ss =
					 * "ALTER TABLE " + tbl.getTableName() + " MODIFY " +
					 * item.getColumn_name() + " int,DROP PRIMARY KEY";
					 * jbaTemplate.executeSQL(ss); LogFileUtil.info(log_name,
					 * "更新表【" + tbl.getTableName() + "】先删除主键、自增：" + ss); } else
					 * { String ss = "ALTER TABLE " + tbl.getTableName() +
					 * " DROP PRIMARY KEY"; jbaTemplate.executeSQL(ss);
					 * LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() +
					 * "】先删除主键：" + ss); } }
					 */
					sqls.add("ALTER TABLE " + tbl.getTableName() + " MODIFY (" + columnPro(col, after, false) + ")");
					after = col.colName();

					list.remove(item);
					break;
				}
			}
			// 是新增列
			if (is2Add) {
				sqls.add("ALTER TABLE " + tbl.getTableName() + " ADD (" + columnPro(col, after, false) + ")");
				after = col.colName();
			}
		}
		// 最后list剩余的则是需要删除的列
		for (OracleColumnInfo item : list) {
			sqls.add("ALTER TABLE " + tbl.getTableName() + " DROP COLUMN " + item.getColumn_name());
		}
		for (int i = 0; i < sqls.size(); i++) {
			jbaTemplate.executeSQL(sqls.get(i));
			LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() + "】语句：" + sqls.get(i));
		}
		// 添加备注
		for (ColumnProperties col : tbl.getColumns().values()) {
			String addComment = addComment(tbl.getTableName(), col);
			if (null == addComment) {
				continue;
			}
			jbaTemplate.executeSQL(addComment);
			LogFileUtil.info(log_name, "更新表【" + tbl.getTableName() + "】字段备注：" + addComment);
		}
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
		if (col.type().equals(int.class) || col.type().equals(Integer.class)) {
			temp.append("integer");
		} else if (col.type().equals(byte.class) || col.type().equals(Byte.class)) {
			temp.append("number(4)");
		} else if (col.type().equals(short.class) || col.type().equals(Short.class)) {
			temp.append("number(6)");
		} else if (col.type().equals(long.class) || col.type().equals(Long.class)) {
			temp.append("number(20)");
		} else if (col.type().equals(String.class)) {
			temp.append("varchar2");
			if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 0) {
				temp.append("(" + col.length() + ")");
			}
		} else if (col.type().equals(Double.class) || col.type().equals(double.class) || col.type().equals(float.class)
				|| col.type().equals(Float.class)) {
			temp.append("number");
			if (col.length() <= 38) {
				temp.append("(" + col.length() + "," + col.precision() + ")");
			}
		} else if (col.type().equals(BigDecimal.class)) {
			temp.append("number");
			if (col.length() <= 38) {
				temp.append("(" + col.length() + "," + col.precision() + ")");
			}
		} else if (col.type().equals(Date.class)) {
			temp.append("date");
		} else {
			temp.append("varchar2(255)");
		}
		if (isCreate && CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
			temp.append(" PRIMARY KEY ");
			switch (col.policy()) {
			case NONE:
				break;
			case AUTO:

				break;
			case GUID:

				break;
			case GUID_UP:

				break;
			case SEQ:

				break;
			default:
				break;
			}
		} else {
			if (isCreate && CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
				temp.append(" NOT NULL ");
			}
		}
		if (CommonUtil.isNotNullEmpty(col.defaultValue())) {
			if (col.type().equals(String.class)) {
				temp.append(" DEFAULT '" + col.defaultValue() + "'");
			} else {
				temp.append(" DEFAULT " + col.defaultValue() + "");
			}
		}
		/*
		 * if (!isCreate) { if (CommonUtil.isNotNullEmpty(after)) {
		 * temp.append(" AFTER " + after); } else { temp.append(" FIRST"); } }
		 */
		return temp.toString();
	}

	private String addComment(String tblName, ColumnProperties col) {
		StringBuilder temp = new StringBuilder();
		temp.append("comment on column " + tblName + "." + col.colName() + " is ");
		if (CommonUtil.isNotNullEmpty(col.remark())) {
			temp.append("'" + col.remark() + "'");
		} else {
			return null;
		}
		return temp.toString();
	}

}
