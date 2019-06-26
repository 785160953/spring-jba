/**
 *
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.*;

/**
 * 是否自动更新时间戳
 *
 * @author Leo.Xi
 * @date 2018年2月15日
 * @since 1.6.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface OnUpdateCurrentTimestamp {

}
