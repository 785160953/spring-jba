package xin.xihc.jba.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xin.xihc.jba.annotation.JbaConfig.DealMode;
import xin.xihc.jba.properties.TableManager;
import xin.xihc.jba.properties.TableProperties;
import xin.xihc.utils.logfile.LogFileUtil;

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
			break;
		default:
			throw new RuntimeException("【" + jbaTemplate.getDbType().name() + "】该数据库类型暂时不支持");
		}
		
		// 是否打印到控制台
		LogFileUtil.setDebugger(TableManager.debugger);
		for (TableProperties tblObj : TableManager.getTbls().values()) {
			if (tableOperation.isTableExists(tblObj.getTableName(), jbaTemplate)) {
				if (TableManager.mode == DealMode.ALL || TableManager.mode == DealMode.UPDATE) {
					tableOperation.updateTable(tblObj, jbaTemplate);
				}
			} else {
				if (TableManager.mode == DealMode.ALL || TableManager.mode == DealMode.CREATE) {
					tableOperation.createTable(tblObj, jbaTemplate);
				}
			}
		}
	}

}
