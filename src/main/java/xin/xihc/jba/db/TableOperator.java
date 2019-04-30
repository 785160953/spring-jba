package xin.xihc.jba.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.tables.Mode;
import xin.xihc.jba.tables.TableManager;
import xin.xihc.jba.tables.properties.TableProperties;

import java.util.concurrent.CompletableFuture;

/**
 * 表创建者、更新者
 *
 * @author Leo.Xi
 * @date 2018年1月21日
 */
public class TableOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableOperator.class);

    I_TableOperation tableOperation = null;

    public TableOperator(JbaTemplate jbaTemplate) {
        tableOperation = new DB_MySql_Opera(jbaTemplate);
    }


    /**
     * 初始化
     */
    public void init() {
        synchronized (TableOperator.class) {
            CompletableFuture.runAsync(() -> {
                // 创建类型不是NONE
                if (TableManager.mode != Mode.NONE) {
                    for (TableProperties tblObj : TableManager.getTables()) {
                        if (tblObj.isIgnore()) {// 忽略的,不处理
                            continue;
                        }
                        if (tableOperation.isTableExists(tblObj.getTableName())) {
                            if (TableManager.mode == Mode.ALL || TableManager.mode == Mode.UPDATE) {
                                tableOperation.updateTable(tblObj);
                            }
                        } else {
                            if (TableManager.mode == Mode.ALL || TableManager.mode == Mode.CREATE || TableManager.mode == Mode.CREATE_DROP) {
                                tableOperation.createTable(tblObj);
                            }
                        }
                    }
                }
            }).exceptionally(ex -> {
                LOGGER.error("维护表异常：", ex);
                System.exit(0);
                return null;
            });
        }
    }

    public void drop() {
        if (TableManager.mode != Mode.CREATE_DROP) {
            return;
        }
        synchronized (TableOperator.class) {
            for (TableProperties tblObj : TableManager.getTables()) {
                if (tblObj.isIgnore()) {// 忽略的,不处理
                    continue;
                }
                tableOperation.dropTable(tblObj);
            }
        }
    }

}
