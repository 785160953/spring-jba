/**
 * 
 */
package xin.xihc.jba.sql;

import java.util.LinkedHashMap;

/**
 * 表属性
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @since
 */
public class TableProperties {

	private String tableName;
	private LinkedHashMap<String, String> columns = new LinkedHashMap<>(16);

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColName(String key) {
		String res = "";
		if (columns.containsKey(key)) {
			res = columns.get(key);
		}
		return res;
	}

	public void addColumn(String key, String val) {
		columns.put(key, val);
	}

}
