/**
 *
 */
package xin.xihc.jba.db;

import xin.xihc.jba.scan.tables.properties.TableProperties;

/**
 * @author Leo.Xi
 * @date 2018年1月24日
 */
public interface I_TableOperation {

    /**
     * 判断表结构是否存在
     *
     * @param tblName 表名
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

}
