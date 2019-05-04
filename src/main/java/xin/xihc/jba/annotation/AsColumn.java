/**
 *
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.*;

/**
 * 字段属性对应的列的别名
 *
 * @author Leo.Xi
 * @date 2019年5月4日
 * @since 1.7.6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface AsColumn {

    /**
     * 列名-别名
     *
     * @return
     */
    String[] value();


}
