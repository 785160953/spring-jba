package xin.xihc.jba.scan.tables;

import xin.xihc.jba.core.JbaTemplate;

/**
 * 表在创建时初始化数据接口
 *
 * @author Leo.Xi
 * @date 2018年1月29日
 * @since 1.3.5
 */
public interface InitDataInterface {

	/**
	 * 在表创建的时候执行的操作<br>
	 * 默认会开子线程执行这个操作，故不需要再开线程单独处理
	 *
	 * @param jbaTemplate 操作数据库的对象
	 */
	void doInit(JbaTemplate jbaTemplate);

}
