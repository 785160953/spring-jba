/**
 *
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.*;

/**
 * 列属性
 *
 * @author Leo.Xi
 * @date 2018年1月12日
 * @since
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Column {

    /**
     * 列名
     *
     * @return
     */
    String value() default "";

    /**
     * 默认值
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * 是否允许为空
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * 是否是主键
     *
     * @return
     */
    boolean primary() default false;

    /**
     * 主键生成策略
     *
     * @return
     */
    Policy policy() default Policy.NONE;

    /**
     * 长度限制，小于1代表不限制
     *
     * @return
     */
    int length() default 0;

    /**
     * 备注
     *
     * @return
     */
    String remark() default "";

    /**
     * 精度
     *
     * @return
     */
    int precision() default 0;

    /**
     * 设置表的字符编码
     *
     * @return
     */
    TableCharset charset() default TableCharset.utf8;

    enum Policy {
        /**
         * NONE
         */
        NONE,
        /**
         * 自增主键
         */
        AUTO,
        /**
         * guid小写
         */
        GUID,
        /**
         * guid大写
         */
        GUID_UP;
    }

    /**
     * 表字符编码
     */
    enum TableCharset {
        /**
         * utf8编码默认
         */
        utf8,

        /**
         * utf8mb4编码-支持emoji表情
         */
        utf8mb4;

        public static TableCharset toCharset(String charset) {
            for (TableCharset tableCharset : TableCharset.values()) {
                if (tableCharset.name().equalsIgnoreCase(charset)) {
                    return tableCharset;
                }
            }
            return null;
        }
    }

}
