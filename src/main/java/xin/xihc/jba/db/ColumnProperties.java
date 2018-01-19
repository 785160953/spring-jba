/**
 * 
 */
package xin.xihc.jba.db;

import xin.xihc.jba.annotation.Column.PrimaryPolicy;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月16日
 * @version
 * @since
 */
public class ColumnProperties {

	private Class<?> type;
	private String colName;
	private String defaultValue;
	private Boolean notNull;
	private Boolean primary;
	private PrimaryPolicy policy;
	private Boolean unique;
	private Integer length;
	private String remark;

	public Class<?> type() {
		return type;
	}

	public ColumnProperties type(Class<?> type) {
		this.type = type;
		return this;
	}

	public String colName() {
		return colName;
	}

	public ColumnProperties colName(String colName) {
		this.colName = colName;
		return this;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public ColumnProperties defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public Boolean notNull() {
		return notNull;
	}

	public ColumnProperties notNull(Boolean notNull) {
		this.notNull = notNull;
		return this;
	}

	public Boolean primary() {
		return primary;
	}

	public ColumnProperties primary(Boolean primary) {
		this.primary = primary;
		return this;
	}

	public PrimaryPolicy policy() {
		return policy;
	}

	public ColumnProperties policy(PrimaryPolicy policy) {
		this.policy = policy;
		return this;
	}

	public Boolean unique() {
		return unique;
	}

	public ColumnProperties unique(Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Integer length() {
		return length;
	}

	public ColumnProperties length(Integer length) {
		this.length = length;
		return this;
	}

	public String remark() {
		return remark;
	}

	public ColumnProperties remark(String remark) {
		this.remark = remark;
		return this;
	}

}