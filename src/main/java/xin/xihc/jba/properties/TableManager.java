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
		/** 不操作 */
		NONE,
		/** 只创建 */
		CREATE,
		/** 只更新 */
		UPDATE,
		/** 即创建也更新 */
		ALL;
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
		return tbls.get(name);
	}

	public static String getTableName(Class<?> clazz) {
		if (tbls.containsKey(clazz.getSimpleName())) {
			return tbls.get(clazz.getSimpleName()).getTableName();
		} else {
			return null;
			// throw new RuntimeException(clazz.getSimpleName() + "不存在");
		}
	}

}
