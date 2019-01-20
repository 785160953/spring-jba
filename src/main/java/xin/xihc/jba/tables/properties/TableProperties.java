/**
 *
 */
package xin.xihc.jba.tables.properties;

import java.util.LinkedHashMap;

/**
 * 表属性
 *
 * @author Leo.Xi
 * @date 2018年1月12日
 * @since
 */
public class TableProperties {

	/**
	 * 表名称
	 */
	private String tableName;// 表名称
	/**
	 * 表模型对象-获取初始化数据用
	 */
	private Object tableBean; // 表模型对象
	/**
	 * 表备注
	 */
	private String remark;//
	/**
	 * 表创建or更新的顺序,默认9999
	 */
	private int order;
	/**
	 * 是否忽略这张表
	 */
	private boolean ignore;


	/**
	 * 对象属性-表字段属性
	 */
	private LinkedHashMap<String, ColumnProperties> columns = new LinkedHashMap<>(16);// 表的列属性

	public Object getTableBean() {
		return tableBean;
	}

	public void setTableBean(Object tableBean) {
		this.tableBean = tableBean;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * 通过字段名称获取表的列属性
	 *
	 * @param fieldName 表对象字段名
	 * @return 列属性
	 */
	public ColumnProperties getColProperties(String fieldName) {
		if (columns.containsKey(fieldName)) {
			return columns.get(fieldName);
		} else {
			throw new RuntimeException("【" + fieldName + "】属性不存在");
		}
	}

	public void addColumn(String fieldName, ColumnProperties prop) {
		columns.put(fieldName, prop);
	}

}
