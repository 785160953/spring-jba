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
	
	/**
	 * 不操作、只创建、只更新、即创建也更新
	 * 
	 * @author 席恒昌
	 * @Date 2018年3月8日
	 *
	 */
	public static enum Mode {
		NONE, CREATE, UPDATE, ALL;
	};

	public static Mode mode = Mode.ALL;

	public static boolean debugger = true;

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
