/**
 *
 */
package xin.xihc.jba.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 声明表对象
 *
 * @author Leo.Xi
 * @date 2018年1月12日
 * @since
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Table {

	/**
	 * 表字符编码
	 */
	enum TableCharset {
		/**
		 * utf8编码默认
		 */
		utf8,

		/**
		 * utf8mb4编码-支持emoji表情
		 */
		utf8mb4;

		public static TableCharset toCharset(String charset){
			for (TableCharset tableCharset : TableCharset.values()) {
				if (tableCharset.name().equalsIgnoreCase(charset)){
					return tableCharset;
				}
			}
			return null;
		}
	}

	/**
	 * 表名
	 */
	String value() default "";

	/**
	 * 备注
	 */
	String remark() default "";

	/**
	 * 表创建or更新的顺序,默认9999
	 *
	 * @return
	 */
	int order() default 9999;

	/**
	 * 是否忽略这张表
	 *
	 * @return
	 */
	boolean ignore() default false;

	/**
	 * 设置表的字符编码
	 *
	 * @return
	 */
	TableCharset charset() default TableCharset.utf8;

}
