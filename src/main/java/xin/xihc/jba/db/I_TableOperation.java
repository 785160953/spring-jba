/**
 *
 */
package xin.xihc.jba.db;

import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.scan.tables.properties.TableProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leo.Xi
 * @date 2018年1月24日
 * @since
 */
public interface I_TableOperation {

	/**
	 * 判断表结构是否存在
	 *
	 * @param tblName 表名
	 * @return
	 */
	boolean isTableExists(String tblName);

	/**
	 * 创建表
	 *
	 * @param tbl 表属性
	 */
	void createTable(TableProperties tbl);

	/**
	 * 更新表结构
	 *
	 * @param tbl 表属性
	 */
	void updateTable(TableProperties tbl);

	/**
	 * 删除表结构
	 *
	 * @param tbl 表属性
	 */
	void dropTable(TableProperties tbl);

	/**
	 * 初始化数据
	 *
	 * @param data        数据
	 * @param jbaTemplate 执行者
	 */
	default void initData(Object[] data, JbaTemplate jbaTemplate) {
		// 初始化数据
		if (null != data) {
			if (data.length > 40) { // 大于40条数据则开启子线程执行
				Thread thread = new Thread(() -> {
					List<Object> temp = new ArrayList<>(40);
					for (int i = 0, j = data.length; i < j; i++) {
						temp.add(data[i]);
						if (temp.size() >= 40) {
							jbaTemplate.insertModels(temp.toArray(new Object[temp.size()]));
							temp.clear();
						}
					}
					if (temp.size() > 0) {
						jbaTemplate.insertModels(temp.toArray(new Object[temp.size()]));
					}
					temp.clear();
					temp = null;
				});
				thread.setName("InitTableData-" + data[0].getClass().getSimpleName());
				thread.start();
			} else {
				jbaTemplate.insertModels(data);
			}
		}
	}

}
