package xin.xihc.jba.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 单个、复合、组合索引注解
 *
 * @author Leo.Xi
 * @date 2019/6/25
 * @since 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface GroupIndex {

    /**
     * 索引名,默认为idx_xxxx、uni_xxxx
     */
    String name() default "";

    /**
     * 索引的列名
     */
    String[] value();

    /**
     * 索引类型,默认为普通索引
     *
     * @return
     */
    Index.IndexType type() default Index.IndexType.Normal;

    /**
     * 索引备注
     *
     * @return
     */
    String remark() default "";

    /**
     * Defines several {@code @GroupIndex} annotations on the same element.
     */
    @Target(ElementType.TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        GroupIndex[] value();
    }


}
