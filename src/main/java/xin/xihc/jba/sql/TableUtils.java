/**
 * 
 */
package xin.xihc.jba.sql;

import java.util.LinkedHashMap;

import xin.xihc.jba.annotation.EnableJBA.DBType;

/**
 * 数据库表工具类
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @since
 */
public class TableUtils {

	private static LinkedHashMap<Class<?>, TableProperties> tbls = new LinkedHashMap<>(16);
	
	private static DBType dbType;

	public static LinkedHashMap<Class<?>, TableProperties> getTbls() {
		return tbls;
	}

	public static DBType dbType() {
		return dbType;
	}

	public static void dbType(DBType dbType) {
		TableUtils.dbType = dbType;
	}

	public static TableProperties addTable(Class<?> clazz, String tblName) {
		TableProperties pp = new TableProperties();
		pp.setTableName(tblName);
		tbls.put(clazz, pp);
		return pp;
	}

	public static TableProperties getTable(Class<?> clazz) {
		if (tbls.containsKey(clazz)) {
			return tbls.get(clazz);
		} else {
			throw new RuntimeException(clazz.getName() + "不存在");
		}
	}

	public static String getTableName(Class<?> clazz) {
		if (tbls.containsKey(clazz)) {
			return tbls.get(clazz).getTableName();
		} else {
			throw new RuntimeException(clazz.getName() + "不存在");
		}
	}

}
