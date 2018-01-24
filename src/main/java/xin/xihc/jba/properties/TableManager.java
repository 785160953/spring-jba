/**
 * 
 */
package xin.xihc.jba.properties;

import java.util.LinkedHashMap;

/**
 * 数据库表工具类
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @since
 */
public class TableManager {

	private static LinkedHashMap<String, TableProperties> tbls = new LinkedHashMap<>(16);

	public static LinkedHashMap<String, TableProperties> getTbls() {
		return tbls;
	}

	public static TableProperties addTable(String name, String tblName) {
		TableProperties pp = new TableProperties();
		pp.setTableName(tblName);
		tbls.put(name, pp);
		return pp;
	}

	public static TableProperties getTable(String name) {
		if (tbls.containsKey(name)) {
			return tbls.get(name);
		} else {
			throw new RuntimeException(name + "不存在");
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
