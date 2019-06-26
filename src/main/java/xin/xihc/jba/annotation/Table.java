/**
 *
 */
package xin.xihc.jba.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 声明表对象
 *
 * @author Leo.Xi
 * @date 2018年1月12日
 * @since
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Table {

    /**
     * 表名
     */
    String value() default "";

    /**
     * 备注
     */
    String remark() default "";

    /**
     * 表创建or更新的顺序,默认9999
     *
     * @since 1.5.0
     */
    int order() default 9999;

    /**
     * 是否忽略这张表
     *
     * @since 1.5.0
     */
    boolean ignore() default false;

}
