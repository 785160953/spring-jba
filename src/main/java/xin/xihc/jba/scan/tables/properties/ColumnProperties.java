/**
 *
 */
package xin.xihc.jba.scan.tables.properties;

import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.annotation.Column.Policy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 列属性
 *
 * @author Leo.Xi
 * @date 2018年1月16日
 * @since
 */
public class ColumnProperties {

    private Class<?> type = String.class;// 列类型
    private String colName;// 列名称
    private String defaultValue = "";// 列默认值
    private Boolean notNull = false;// 是否允许为空
    private Boolean primary = false;// 是否为主键
    private Policy policy = Policy.NONE;//值的生成策略
    private Integer length = 0;// 列长度
    private String remark = "";// 列备注
    private Integer precision = 4;// 列精度
    private int order = 0;// 列的顺序
    private Boolean onUpdateCurrentTimestamp = false;// 是否自动更新时间戳

    /**
     * 表的列的字符编码
     */
    private Column.TableCharset charset = Column.TableCharset.utf8;

    public Integer precision() {
        return precision;
    }

    public ColumnProperties precision(Integer precision) {
        if (null == precision || precision < 1) {
            return this;
        } else {
            this.precision = precision;
        }
        return this;
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

    public Policy policy() {
        return policy;
    }

    public ColumnProperties policy(Policy policy) {
        this.policy = policy;
        return this;
    }

    public Integer length() {
        return length;
    }

    public ColumnProperties length(Integer length) {
        if (null == length || length < 1) {
            if (type.equals(String.class) || type.isEnum()) {
                this.length = 255;
            } else if (type.equals(Integer.class)) {
                this.length = 10;
            } else if (type.equals(Long.class)) {
                this.length = 19;
            } else if (type.equals(BigDecimal.class)) {
                this.length = 20;
            } else if (type.equals(Double.class)) {
                this.length = 20;
            } else if (type.equals(Float.class)) {
                this.length = 12;
            } else if (type.equals(Byte.class) || type.equals(Boolean.class)) {
                this.length = 3;
            } else if (type.equals(Short.class)) {
                this.length = 5;
            }
        } else {
            this.length = length;
        }
        // 整形数字时，不能设置长度
        if (type.equals(Integer.class)) {
            this.length = 10;
        } else if (type.equals(Long.class)) {
            this.length = 19;
        } else if (type.equals(Byte.class) || type.equals(Boolean.class)) {
            this.length = 3;
        } else if (type.equals(Short.class)) {
            this.length = 5;
        }
        return this;
    }

    public String remark() {
        return remark;
    }

    public ColumnProperties remark(String remark) {
        this.remark = remark;
        return this;
    }

    public Column.TableCharset charset() {
        return charset;
    }

    public ColumnProperties charset(Column.TableCharset charset) {
        if (null == charset) {
            return this;
        }
        this.charset = charset;
        return this;
    }

    public Boolean onUpdateCurrentTimestamp() {
        return onUpdateCurrentTimestamp;
    }

    public ColumnProperties onUpdateCurrentTimestamp(Boolean onUpdateCurrentTimestamp) {
        this.onUpdateCurrentTimestamp = onUpdateCurrentTimestamp;
        return this;
    }

    public ColumnProperties order(int order) {
        this.order = order;
        return this;
    }

    public int order() {
        return this.order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ColumnProperties that = (ColumnProperties) o;
        boolean equals = Objects.equals(defaultValue, that.defaultValue) && Objects
                .equals(notNull, that.notNull) && Objects.equals(primary, that.primary) && Objects
                .equals(length, that.length) && Objects.equals(remark, that.remark) && Objects
                .equals(precision, that.precision) && Objects.equals(charset, that.charset) && Objects
                .equals(onUpdateCurrentTimestamp, that.onUpdateCurrentTimestamp) && Objects.equals(order, that.order);
        return equals;
    }
}
