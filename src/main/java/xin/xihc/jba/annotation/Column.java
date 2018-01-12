/**
 * 
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @since
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

	public enum PrimaryPolicy {
		NONE, AUTO, GUID/* 小写 */, GUID_UP/* 大写 */, SEQ;
	}

	public String value();

	public boolean primary() default false;

	public PrimaryPolicy policy() default PrimaryPolicy.AUTO;

	public boolean unique() default false;

	public int length();

}
