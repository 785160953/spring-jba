package xin.xihc.jba.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 单个、复合、组合索引注解
 *
 * @author Leo.Xi
 * @date 2019/6/25
 * @since 1.7.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
     */
    Index.IndexType type() default Index.IndexType.Normal;

    /**
     * 索引备注
     */
    String remark() default "";

    /**
     * 分组索引列表注解-用于多个复合索引
     */
    @Target(ElementType.TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        GroupIndex[] value();
    }


}
