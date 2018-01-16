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
	private LinkedHashMap<String, ColumnProperties> columns = new LinkedHashMap<>(16);

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ColumnProperties getColProperties(String key) {
		if (columns.containsKey(key)) {
			return columns.get(key);
		} else {
			throw new RuntimeException("【" + key + "】属性不存在");
		}
	}

	public void addColumn(String key, ColumnProperties val) {
		columns.put(key, val);
	}

}
