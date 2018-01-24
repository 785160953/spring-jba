/**
 * 
 */
package xin.xihc.jba.db;

import xin.xihc.jba.properties.TableProperties;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
public interface I_TableOperation {

	/**
	 * 判断表结构是否存在
	 * 
	 * @param tblName
	 * @return
	 */
	boolean isTableExists(String tblName, JbaTemplate jbaTemplate);

	/**
	 * 创建表
	 * 
	 * @param tbl
	 * @param jbaTemplate
	 */
	void createTable(TableProperties tbl, JbaTemplate jbaTemplate);

	/**
	 * 更新表结构
	 * 
	 * @param tbl
	 * @param jbaTemplate
	 */
	void updateTable(TableProperties tbl, JbaTemplate jbaTemplate);

}
