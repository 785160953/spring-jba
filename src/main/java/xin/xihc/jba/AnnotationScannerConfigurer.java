/**
 * 
 */
package xin.xihc.jba;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.annotation.Table;
import xin.xihc.jba.sql.TableProperties;
import xin.xihc.jba.sql.TableUtils;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月12日
 * @version
 * @since
 */
@Component
@Lazy(true)
public class AnnotationScannerConfigurer implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Map<String, Object> map = beanFactory.getBeansWithAnnotation(Table.class);
		for (Object obj : map.values()) {
			Table table = obj.getClass().getAnnotation(Table.class);
			TableProperties tblP = null;
			if ("".equals(table.value())) {
				tblP = TableUtils.addTable(obj.getClass(), "1");
			} else {
				tblP = TableUtils.addTable(obj.getClass(), table.value());
			}
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				Column column = field.getAnnotation(Column.class);
				if (null == column) {
					tblP.addColumn(field.getName(), "");
				}
			}
		}
		System.err.println("i'm back!");
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

	}

}
