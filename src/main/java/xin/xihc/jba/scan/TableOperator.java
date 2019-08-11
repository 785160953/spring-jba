package xin.xihc.jba.scan;

import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.db.DB_MySql_Opera;
import xin.xihc.jba.db.I_TableOperation;
import xin.xihc.jba.scan.tables.properties.TableProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 表创建者、更新者
 *
 * @author Leo.Xi
 * @date 2018年1月21日
 */
public class TableOperator {

    /** 表构建模式 */
    private static Mode MODE = Mode.ALL;
    /** 创建的模式列表 */
    private static List<Mode> CREATE_MODES = Arrays.asList(Mode.ALL, Mode.CREATE, Mode.CREATE_DROP);
    /** 更新表的模式列表 */
    private static List<Mode> UPDATE_MODES = Arrays.asList(Mode.ALL, Mode.UPDATE);
    private I_TableOperation tableOperation = null;

    TableOperator(JbaTemplate jbaTemplate) {
        tableOperation = new DB_MySql_Opera(jbaTemplate);
    }

    /**
     * 初始化
     */
    void init(Mode mode) {
        Objects.requireNonNull(mode, "mode is null");
        TableOperator.MODE = mode;
        if (TableOperator.MODE == Mode.NONE) {
            return;
        }
        synchronized (TableOperator.class) {
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
        }
    }

    /**
     * 删除表操作
     *
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    void drop() {
        if (TableOperator.MODE != Mode.CREATE_DROP) {
            return;
        }
        synchronized (TableOperator.class) {
            for (TableProperties tblObj : TableManager.getTables()) {
                if (tblObj.isIgnore()) {// 忽略的,不处理
                    continue;
                }
                if (tableOperation.isTableExists(tblObj.getTableName())) {
                    tableOperation.dropTable(tblObj);
                }
            }
        }
    }

}
