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
import xin.xihc.jba.db.ColumnProperties;
import xin.xihc.jba.db.TableManager;
import xin.xihc.jba.db.TableProperties;
import xin.xihc.jba.db.TableUtils;

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
				tblP = TableUtils.addTable(obj.getClass().getSimpleName(), obj.getClass().getSimpleName());
			} else {
				tblP = TableUtils.addTable(obj.getClass().getSimpleName(), table.value());
			}
			Field[] fields = obj.getClass().getDeclaredFields();
			int keyCount = 0;
			for (Field field : fields) {
				Column column = field.getAnnotation(Column.class);
				ColumnProperties colP = new ColumnProperties();
				tblP.addColumn(field.getName(), colP);
				colP.type(field.getType());
				if (null == column) {
					colP.colName(field.getName());
				} else {
					colP.colName(field.getName()).defaultValue(column.defaultValue()).notNull(column.notNull())
							.unique(column.unique()).remark(column.remark());
					colP.length(0);
					if (column.length() > 0) {
						colP.length(column.length());
					}
					if (column.primary()) {
						keyCount++;
						if (keyCount > 1) {
							throw new RuntimeException("主键数量超过一个了.");
						}
						colP.primary(true);
						colP.policy(column.policy());
					}
				}
			}
		}
		// 执行表创建、字段更新
		System.err.println("i'm back!");
		TableManager tableManager = new TableManager();
		tableManager.init();
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

	}

}
