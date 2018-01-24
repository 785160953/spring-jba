/**
 * 
 */
package xin.xihc.jba.db;

import xin.xihc.jba.properties.TableProperties;

/**
 * oracle数据库操作
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
public class DB_Oracle_Opera implements I_TableOperation {

	@Override
	public boolean isTableExists(String tblName, JbaTemplate jbaTemplate) {
		return false;
	}

	@Override
	public void createTable(TableProperties tbl, JbaTemplate jbaTemplate) {

	}

	@Override
	public void updateTable(TableProperties tbl, JbaTemplate jbaTemplate) {

	}

}
