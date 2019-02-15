/**
 *
 */
package xin.xihc.jba.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import xin.xihc.jba.AnnotationScan;
import xin.xihc.jba.SpringContextUtil;
import xin.xihc.jba.core.JbaTemplate;

import java.lang.annotation.*;

/**
 * 启用jba功能
 *
 * @author Leo.Xi
 * @date 2018年1月16日
 * @since
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Component
@Import({SpringContextUtil.class, JbaTemplate.class, AnnotationScan.class})
public @interface EnableJBA {

}
