package xin.xihc.jba.annotation;

import java.lang.annotation.*;

/**
 * 创建表索引
 *
 * @Author Leo.Xi
 * @Date 2019/1/28 10:54
 * @Version 1.0
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
     *
     * @return
     */
    IndexType type() default IndexType.Normal;

    /**
     * 索引备注
     *
     * @return
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
