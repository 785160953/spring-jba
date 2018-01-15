/**
 * 
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

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
@Component
public @interface Column {

	public enum PrimaryPolicy {
		NONE, AUTO, GUID/* 小写 */, GUID_UP/* 大写 */, SEQ;
	}

	/**
	 * 列名
	 * 
	 * @return
	 */
	public String value();

	/**
	 * 默认值
	 * 
	 * @return
	 */
	public String defaultValue() default "";

	/**
	 * 是否允许为空
	 * 
	 * @return
	 */
	public boolean notNull() default false;

	/**
	 * 是否是主键
	 * 
	 * @return
	 */
	public boolean primary() default false;

	/**
	 * 主键生成策略
	 * 
	 * @return
	 */
	public PrimaryPolicy policy() default PrimaryPolicy.AUTO;

	/**
	 * 是否唯一
	 * 
	 * @return
	 */
	public boolean unique() default false;

	/**
	 * 长度限制-1代表不限制
	 * 
	 * @return
	 */
	public int length() default -1;

	/**
	 * 备注
	 * 
	 * @return
	 */
	public String remark() default "";

}
