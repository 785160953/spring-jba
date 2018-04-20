/**
 * 
 */
package xin.xihc.jba.properties;

import java.util.LinkedHashMap;

/**
 * 表属性
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @param <T>
 * @since
 */
public class TableProperties {

	private String tableName;
	private String remark;
	private Object[] initData;
	private LinkedHashMap<String, ColumnProperties> columns = new LinkedHashMap<>(16);

	public Object[] initData() {
		return initData;
	}

	public void initData(Object[] initData) {
		this.initData = initData;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public LinkedHashMap<String, ColumnProperties> getColumns() {
		return columns;
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
