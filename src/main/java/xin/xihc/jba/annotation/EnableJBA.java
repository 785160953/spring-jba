/**
 * 
 */
package xin.xihc.jba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import xin.xihc.jba.AnnotationScannerConfigurer;
import xin.xihc.jba.db.MyOrmJdbcTemplate;
import xin.xihc.jba.db.TableManager;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月16日
 * @version
 * @since
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableTransactionManagement
@Component
@ComponentScan(basePackageClasses = { MyOrmJdbcTemplate.class, TableManager.class })
@Import({ AnnotationScannerConfigurer.class })
public @interface EnableJBA {

}
