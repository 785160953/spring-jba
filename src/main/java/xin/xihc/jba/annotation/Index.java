package xin.xihc.jba.annotation;

import java.lang.annotation.*;

/**
 * 单列索引注解
 *
 * @author Leo.Xi
 * @date 2019/1/28 10:54
 * @since 1.5.7
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Index {

    /**
     * 索引名，用于分组,默认为idx_xxxx
     */
    String value() default "";

    /**
     * 索引类型,默认为普通索引
     */
    IndexType type() default IndexType.Normal;

    /**
     * 索引备注
     *
     * @since 1.5.8
     */
    String remark() default "";


    /**
     * 索引类型
     */
    enum IndexType {
        /**
         * 唯一索引
         */
        Unique,

        /**
         * 普通索引
         */
        Normal,

        /**
         * 全文索引
         */
        FullText;
    }

}
