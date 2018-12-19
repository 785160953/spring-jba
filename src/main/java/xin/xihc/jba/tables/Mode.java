package xin.xihc.jba.tables;

/**
 * 表结构、创建、更新操作类型
 *
 * @author Leo.Xi
 * @Date 2018年3月8日
 */
public enum Mode {

	/**
	 * 不操作
	 */
	NONE,
	/**
	 * 启动时创建关闭后删除表
	 */
	CREATE_DROP,
	/**
	 * 只创建
	 */
	CREATE,
	/**
	 * 只更新
	 */
	UPDATE,
	/**
	 * 既创建也更新
	 */
	ALL,;
}
