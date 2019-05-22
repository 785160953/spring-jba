/**
 *
 */
package xin.xihc.jba.tables;

import xin.xihc.jba.tables.properties.TableProperties;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表工具类
 *
 * @author Leo.Xi
 * @version 1.2.0
 * @date 2018年1月12日
 * @since 1.0.0
 */
public class TableManager {

    private static LinkedHashMap<Class<?>, TableProperties> tbls = new LinkedHashMap<>(16);

    /**
     * 获取表的列表
     *
     * @return
     */
    public static List<TableProperties> getTables() {
        return tbls.values().stream().sorted(Comparator.comparing(t -> t.getOrder())).collect(Collectors.toList());
    }

    /**
     * 获取对应的Class对应的表属性
     *
     * @return
     */
    public static LinkedHashMap<Class<?>, TableProperties> getTablesMap() {
        return tbls;
    }

    /**
     * 添加表数据
     *
     * @param clazz
     * @param tblName
     * @return
     */
    public static TableProperties addTable(Class<?> clazz, String tblName) {
        TableProperties pp = new TableProperties();
        pp.setTableName(tblName);
        tbls.put(clazz, pp);
        return pp;
    }

    /**
     * @param clazz
     * @return
     */
    public static TableProperties getTable(Class<?> clazz) {
        if (tbls.containsKey(clazz)) {
            return tbls.get(clazz);
        } else {
            throw new RuntimeException(String.format("【%s】对应的表不存在", clazz.getName()));
        }
    }

}
