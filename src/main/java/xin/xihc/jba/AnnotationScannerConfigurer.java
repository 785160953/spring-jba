/**
 * 
 */
package xin.xihc.jba;

import java.util.Map;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import xin.xihc.jba.annotation.Table;

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
			
		}
		System.err.println("i'm back!");
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

	}

}
