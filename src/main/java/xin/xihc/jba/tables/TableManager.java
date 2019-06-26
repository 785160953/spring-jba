/**
 *
 */
package xin.xihc.jba.tables;

import xin.xihc.jba.annotation.*;
import xin.xihc.jba.db.DB_MySql_Opera;
import xin.xihc.jba.tables.properties.ColumnProperties;
import xin.xihc.jba.tables.properties.IndexProperties;
import xin.xihc.jba.tables.properties.TableProperties;
import xin.xihc.jba.utils.SQLUtils;
import xin.xihc.utils.common.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
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
        return tbls.values().stream().sorted(Comparator.comparing(TableProperties::getOrder)).collect(Collectors.toList());
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

    /**
     * 获取索引前缀
     *
     * @param type 索引类型
     * @return 获取索引前缀
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    private static String getIndexPrefix(Index.IndexType type) {
        String prefix = "idx_";
        switch (type) {
            case Unique:
                prefix = "uk_";
                break;
            case FullText:
                prefix = "full_";
                break;
            default:
                break;
        }
        return prefix;
    }

    /**
     * 转换成IndexProperties
     *
     * @param groupIndex
     * @return
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    private static IndexProperties convert2IndexProp(GroupIndex groupIndex) {
        IndexProperties idx = new IndexProperties();
        idx.setColumnNames(groupIndex.value());
        if (CommonUtil.isNullEmpty(groupIndex.name())) {
            // 只取每个列名的首个_
            String indexName = getIndexPrefix(groupIndex.type()) + Arrays.stream(groupIndex.value())
                    .map(x -> x.split("_")[0]).collect(Collectors.joining("_"));
            idx.setIndexName(indexName);
        } else {
            idx.setIndexName(groupIndex.name());
        }
        idx.setType(groupIndex.type());
        idx.setRemark(groupIndex.remark());

        return idx;
    }

    /**
     * 扫描表注解
     *
     * @param obj 表对象
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    public static TableProperties scanTableAnnotations(Object obj) {
        Table table = obj.getClass().getAnnotation(Table.class);
        TableProperties tblProp = null;
        if (table.value().matches("^_?[a-zA-Z]+\\w+")) { // 是字母或下划线开头的，为有效的表名
            tblProp = addTable(obj.getClass(), table.value());
        } else {
            tblProp = addTable(obj.getClass(), obj.getClass().getSimpleName());
        }
        // 获取表注释
        tblProp.setRemark(table.remark());
        tblProp.setTableBean(obj);
        tblProp.setIgnore(table.ignore());
        tblProp.setOrder(table.order());

        // 增加扫描@GroupIndex注解
        GroupIndex groupIndex = obj.getClass().getAnnotation(GroupIndex.class);
        if (null != groupIndex) {
            tblProp.addIndex(convert2IndexProp(groupIndex));
        }

        GroupIndex.List groupIndexList = obj.getClass().getAnnotation(GroupIndex.List.class);
        if (null != groupIndexList && groupIndexList.value().length > 0) {
            for (GroupIndex index : groupIndexList.value()) {
                if (null != index) {
                    tblProp.addIndex(convert2IndexProp(index));
                }
            }
        }

        return tblProp;
    }

    /**
     * 扫描字段注解
     *
     * @param tblProp 表属性
     * @param field   字段信息
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    public static void scanFieldAnnotations(TableProperties tblProp, Field field) {
        Column column = field.getAnnotation(Column.class);
        ColumnProperties colP = new ColumnProperties();
        colP.type(field.getType());
        if (null == column) {
            colP.colName(SQLUtils.underscoreName(field.getName()));

            // 没有的给默认值
            colP.length(0)
                    .precision(0)
                    .policy(Column.Policy.NONE)
                    .primary(false)
                    .notNull(false)
                    .defaultValue("")
                    .remark("")
                    .order(0);
        } else {
            colP.colName(SQLUtils.underscoreName(field.getName()))
                    .defaultValue(column.defaultValue())
                    .notNull(column.notNull())
                    .remark(column.remark()).charset(column.charset()).order(column.order());
            colP.policy(column.policy());
            colP.length(0);
            if (CommonUtil.isNotNullEmpty(column.value())) {
                colP.colName(column.value());
            }
            /** 如果是guid为主键长度默认为32 */
            if (colP.policy() == Column.Policy.GUID || colP.policy() == Column.Policy.GUID_UP) {
                colP.length(32);
            }
            if (column.length() > 0) {
                colP.length(column.length());
            }
            if (column.precision() > 0) {
                colP.precision(column.precision());
            }
            if (column.primary()) {
                colP.primary(true);
                colP.notNull(true);
            }

        }
        // 是否自动更新时间戳
        OnUpdateCurrentTimestamp onUpdateCurrentTimestamp = field.getAnnotation(OnUpdateCurrentTimestamp.class);
        if (null != onUpdateCurrentTimestamp) {
            colP.onUpdateCurrentTimestamp(DB_MySql_Opera.onUpdateApplied.contains(field.getType()));
        }

        // 添加到缓存
        tblProp.addColumn(field.getName(), colP);

        // 记录索引
        Index index = field.getAnnotation(Index.class);
        if (null != index) {
            IndexProperties idx = new IndexProperties();
            idx.setColumnNames(new String[]{colP.colName()});
            idx.setIndexName(CommonUtil.isNullEmpty(index.value()) ? getIndexPrefix(index.type()) +
                    colP.colName() : index.value());
            idx.setType(index.type());
            idx.setRemark(index.remark());

            tblProp.addFirstIndex(idx);
        }
    }

}
