/**
 * 
 */
package xin.xihc.jba.properties;

import xin.xihc.jba.annotation.Column.PrimaryPolicy;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月16日
 * @version
 * @since
 */
public class ColumnProperties {

	private Class<?> type = String.class;
	private String colName;
	private String defaultValue;
	private Boolean notNull = false;
	private Boolean primary = false;
	private PrimaryPolicy policy = PrimaryPolicy.NONE;
	private Integer length = 255;
	private String remark;
	private Integer precision = 0;

	public Integer precision() {
		return precision;
	}

	public void precision(Integer precision) {
		this.precision = precision;
	}

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
