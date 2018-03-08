package xin.xihc.jba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface JbaConfig {

	/**
	 * 不操作、只创建、只更新、即创建也更新
	 * 
	 * @author 席恒昌
	 * @Date 2018年3月8日
	 *
	 */
	public static enum DealMode {
		NONE, CREATE, UPDATE, ALL;
	};

	boolean debugger() default true;

	DealMode mode() default DealMode.ALL;

}
