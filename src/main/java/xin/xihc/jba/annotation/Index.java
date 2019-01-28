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
     * 复合索引的顺序,默认0
     *
     * @return
     */
    int order() default 0;


    /**
     * 索引类型
     */
    enum IndexType {
        /**
         * 普通索引
         */
        Normal,

        /**
         * 唯一索引
         */
        Unique;
    }

}