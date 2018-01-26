package xin.xihc.jba.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xin.xihc.jba.properties.TableManager;
import xin.xihc.jba.properties.TableProperties;

/**
 * 表管理
 * 
 * @author 席恒昌
 * @Date 2018年1月21日
 *
 */
@Component
public class TableOperator {

	@Autowired
	JbaTemplate jbaTemplate;

	/**
	 * 初始化
	 */
	@Transactional
	public void init() {
		I_TableOperation tableOperation = null;
		switch (jbaTemplate.getDbType()) {
		case MySql:
			tableOperation = new DB_MySql_Opera();
			break;
		case Oracle:
			tableOperation = new DB_Oracle_Opera();
//			break;
		default:
			throw new RuntimeException("【" + jbaTemplate.getDbType().name() + "】该数据库类型暂时不支持");
		}
		
		for (TableProperties tblObj : TableManager.getTbls().values()) {
			if (tableOperation.isTableExists(tblObj.getTableName(), jbaTemplate)) {
				tableOperation.updateTable(tblObj, jbaTemplate);
			} else {
				tableOperation.createTable(tblObj, jbaTemplate);
			}
		}
	}

}
