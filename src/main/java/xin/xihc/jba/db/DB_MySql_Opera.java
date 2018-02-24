/**
 * 
 */
package xin.xihc.jba.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import xin.xihc.jba.db.bean.MysqlColumnInfo;
import xin.xihc.jba.properties.ColumnProperties;
import xin.xihc.jba.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

/**
 * mysql数据库操作
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
public class DB_MySql_Opera implements I_TableOperation {

	public final static String log_name = "DB_Update_MySql_Sql";

	public String table_schema = "";

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

	@Transactional
	@Override
	public void createTable(TableProperties tbl, final JbaTemplate jbaTemplate) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
		String after = "";
		for (ColumnProperties col : tbl.getColumns().values()) {
			sql.append(columnPro(col, after, true));
			sql.append(",");
			after = col.colName();
		}
		sql.deleteCharAt(sql.length() - 1).append(")DEFAULT CHARSET=utf8;");
		jbaTemplate.executeSQL(sql.toString());
		LogFileUtil.info(log_name, "创建表【" + tbl.getTableName() + "】语句：" + sql.toString());
		final Object[] initData = tbl.initData();
		// 初始化数据
		if (null != initData) {
			if (initData.length > 20) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						for (Object obj : initData) {
							jbaTemplate.insertModel(obj);
						}
					}
				});
				thread.setDaemon(true);
				thread.setName("InitTableData");
				thread.start();
//				final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
//				singleThreadExecutor.submit(new Runnable() {
//					@Override
//					public void run() {
//						for (Object obj : initData) {
//							jbaTemplate.insertModel(obj);
//						}
//						singleThreadExecutor.shutdown();
//					}
//				});
			} else {
				for (Object obj : initData) {
					jbaTemplate.insertModel(obj);
				}
			}
		}
	}

	@Transactional
	@Override
	public void updateTable(TableProperties tbl, JbaTemplate jbaTemplate) {
		// 先获取当前数据库名
		String tableSchema = jbaTemplate.queryColumn("select database()", null, String.class);
		// 先获取表结构信息
		List<MysqlColumnInfo> list = jbaTemplate
				.queryMixModelList("select * from information_schema.columns where table_name = '" + tbl.getTableName()
						+ "' AND table_schema='" + tableSchema + "'", null, MysqlColumnInfo.class, null);
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
					sqls.add("MODIFY " + columnPro(col, after, false));
					after = col.colName();

					list.remove(item);
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
	private String columnPro(ColumnProperties col, String after, boolean isCreate) {
		StringBuilder temp = new StringBuilder();
		temp.append(col.colName() + " ");
		if (col.type().equals(int.class) || col.type().equals(Integer.class)) {
			temp.append("int");
		} else if (col.type().equals(byte.class) || col.type().equals(Byte.class)) {
			temp.append("tinyint");
		} else if (col.type().equals(short.class) || col.type().equals(Short.class)) {
			temp.append("smallint");
		} else if (col.type().equals(long.class) || col.type().equals(Long.class)) {
			temp.append("bigint");
		} else if (col.type().equals(String.class)) {
			if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 2000) {
				temp.append("text");
			} else {
				temp.append("varchar");
				if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 0) {
					temp.append("(" + col.length() + ")");
				}
			}
		} else if (col.type().equals(Double.class) || col.type().equals(double.class) || col.type().equals(float.class)
				|| col.type().equals(Float.class)) {
			temp.append("double");
			if (col.length() > 0 || col.precision() >= 0 && col.length() >= col.precision()) {
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
		if (!isCreate) {
			if (CommonUtil.isNotNullEmpty(after)) {
				temp.append(" AFTER " + after);
			} else {
				temp.append(" FIRST");
			}
		}
		return temp.toString();
	}

}
