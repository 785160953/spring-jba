package xin.xihc.jba.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.tables.Mode;
import xin.xihc.jba.tables.TableManager;
import xin.xihc.jba.tables.properties.TableProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 表创建者、更新者
 *
 * @author Leo.Xi
 * @date 2018年1月21日
 */
public class TableOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableOperator.class);

    /** 表构建模式 */
    private static Mode MODE = Mode.ALL;

    I_TableOperation tableOperation = null;

    /** 创建的模式列表 */
    private List<Mode> CREATE_MODES = Arrays.asList(Mode.ALL, Mode.CREATE, Mode.CREATE_DROP);
    /** 更新表的模式列表 */
    private List<Mode> UPDATE_MODES = Arrays.asList(Mode.ALL, Mode.UPDATE);

    public TableOperator(JbaTemplate jbaTemplate) {
        tableOperation = new DB_MySql_Opera(jbaTemplate);
    }


    /**
     * 初始化
     */
    public void init(Mode mode) {
        Objects.requireNonNull(mode, "mode is null");
        TableOperator.MODE = mode;
        if (TableOperator.MODE == Mode.NONE) {
            return;
        }
        synchronized (TableOperator.class) {
            CompletableFuture.runAsync(() -> {
                // 创建类型不是NONE
                for (TableProperties tblObj : TableManager.getTables()) {
                    if (tblObj.isIgnore()) {// 忽略的,不处理
                        continue;
                    }
                    if (tableOperation.isTableExists(tblObj.getTableName())) {
                        if (UPDATE_MODES.contains(TableOperator.MODE)) {
                            tableOperation.updateTable(tblObj);
                        }
                    } else {
                        if (CREATE_MODES.contains(TableOperator.MODE)) {
                            tableOperation.createTable(tblObj);
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
        if (TableOperator.MODE != Mode.CREATE_DROP) {
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
