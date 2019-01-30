package xin.xihc.jba.tables.properties;

import xin.xihc.jba.annotation.Index;


/**
 * 索引属性
 *
 * @Author Leo.Xi
 * @Date 2019/1/28 11:18
 * @Version 1.0
 **/
public class IndexProperties {

	/**
	 * 索引名
	 */
	private String indexName;
	/**
	 * 索引类型
	 */
	private Index.IndexType type;

	/**
	 * 复合索引的顺序,默认0
	 */
	private int order = 0;
	/**
	 * 索引的列名
	 */
	private String columnName;

	/**
	 * 备注
	 *
	 * @return
	 */
	private String remark = "";

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Index.IndexType getType() {
		return type;
	}

	public void setType(Index.IndexType type) {
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
