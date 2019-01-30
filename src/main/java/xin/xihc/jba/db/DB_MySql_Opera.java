/**
 *
 */
package xin.xihc.jba.db;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.annotation.Index;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.db.bean.MysqlColumnInfo;
import xin.xihc.jba.db.bean.MysqlIndexInfo;
import xin.xihc.jba.tables.InitDataInterface;
import xin.xihc.jba.tables.properties.ColumnProperties;
import xin.xihc.jba.tables.properties.IndexProperties;
import xin.xihc.jba.tables.properties.TableProperties;
import xin.xihc.utils.common.CommonUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mysql数据库操作
 *
 * @author Leo.Xi
 * @version 1.3.0 不再支持java基本数据类型
 * @date 2018年1月24日
 * @desc 增加:对java.sql.Date,Time,Timestamp字段的支持
 * @since 1.1.4
 */
public class DB_MySql_Opera implements I_TableOperation {

    private static Map<Class, String> javaClassToMysqlFieldName = new HashMap<>();

    static {
        //		javaClassToMysqlFieldName.put(byte.class, "tinyint");
        javaClassToMysqlFieldName.put(Byte.class, "tinyint");
        //		javaClassToMysqlFieldName.put(short.class, "smallint");
        javaClassToMysqlFieldName.put(Short.class, "smallint");
        //		javaClassToMysqlFieldName.put(int.class, "int");
        javaClassToMysqlFieldName.put(Integer.class, "int");
        //		javaClassToMysqlFieldName.put(long.class, "bigint");
        javaClassToMysqlFieldName.put(Long.class, "bigint");
        //		javaClassToMysqlFieldName.put(String.class, "varchar"); // String为默认，不需要加上
        //		javaClassToMysqlFieldName.put(double.class, "double");
        javaClassToMysqlFieldName.put(Double.class, "double");
        //		javaClassToMysqlFieldName.put(float.class, "double");
        javaClassToMysqlFieldName.put(Float.class, "double");
        javaClassToMysqlFieldName.put(BigDecimal.class, "decimal");
        javaClassToMysqlFieldName.put(java.util.Date.class, "datetime");
        javaClassToMysqlFieldName.put(java.sql.Timestamp.class, "timestamp");
        javaClassToMysqlFieldName.put(java.sql.Date.class, "date");
        javaClassToMysqlFieldName.put(java.sql.Time.class, "time");
        //		javaClassToMysqlFieldName.put(boolean.class, "tinyint");
        javaClassToMysqlFieldName.put(Boolean.class, "tinyint");
    }

    private String table_schema; // 数据库schema
    private JbaTemplate jbaTemplate;

    public DB_MySql_Opera(JbaTemplate jbaTemplate) {
        this.jbaTemplate = jbaTemplate;
        table_schema = getSchema();
    }

    /**
     * 先获取当前数据库名
     *
     * @return
     */
    private String getSchema() {
        return jbaTemplate.queryColumn("select database()", null, String.class);
    }

    @Override
    public boolean isTableExists(final String tblName) {
        boolean res = false;
        String sql = "select count(1) FROM information_schema.TABLES WHERE table_name ='" + tblName + "' AND table_schema='" + this.table_schema + "'";
        Integer count = jbaTemplate.queryColumn(sql, null, Integer.class);
        if (count > 0) {
            res = true;
        }
        return res;
    }

    @Transactional(rollbackFor = DataAccessException.class)
    @Override
    public void createTable(TableProperties tbl) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
        for (ColumnProperties col : tbl.getColumns().values()) {
            sql.append(columnPro(col, null, true, null));
            sql.append(",");
        }
        String newPrimary = tbl.getColumns().values().stream().filter(x -> x.primary()).map(ColumnProperties::colName).sorted().collect(Collectors.joining(","));
        if (CommonUtil.isNotNullEmpty(newPrimary)) {
            sql.append("PRIMARY KEY (" + newPrimary + "),");
        }
        sql.deleteCharAt(sql.length() - 1)
                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '" + tbl.getRemark() + "';");
        jbaTemplate.executeSQL(sql.toString());

        // 初始化数据
        if (tbl.getTableBean() instanceof InitDataInterface) {
            Thread thread = new Thread(() -> ((InitDataInterface) tbl.getTableBean()).doInit(jbaTemplate));
            thread.setName("initData-" + tbl.getTableName());
            thread.start();
        }

        // 更新索引
        updateIndex(tbl);
    }

    /**
     * 根据数据库列类型返回java数据类型
     *
     * @param dataType 列类型
     * @return
     */
    private Class getClassByColumnDataType(String dataType) {
        Set<Class> keySet = javaClassToMysqlFieldName.keySet();
        for (Class key : keySet) {
            if (javaClassToMysqlFieldName.get(key).equals(dataType)) {
                return key;
            }
        }
        return String.class;
    }

    /**
     * 将表中列的属性转为ColumnProperties
     *
     * @param dbColumns 表的列属性列表
     * @return
     */
    private List<ColumnProperties> convert2ColumnProperties(List<MysqlColumnInfo> dbColumns) {
        if (dbColumns.size() < 1) {
            return new ArrayList<>(0);
        }
        List<ColumnProperties> result = new ArrayList(dbColumns.size());
        ColumnProperties prop;
        for (MysqlColumnInfo item : dbColumns) {
            prop = new ColumnProperties();
            prop.colName(item.getColumn_name());
            prop.type(getClassByColumnDataType(item.getData_type()));
            prop.defaultValue(CommonUtil.isNullEmpty(item.getColumn_default()) ? "" : item.getColumn_default());
            prop.remark(CommonUtil.isNullEmpty(item.getColumn_comment()) ? "" : item.getColumn_comment());
            prop.notNull("NO".equals(item.getIs_nullable()));
            if ("PRI".equals(item.getColumn_key())) { // 是主键
                prop.primary(true);
                if ("auto_increment".equals(item.getExtra())) { // 自增主键
                    prop.policy(Column.Policy.AUTO);
                    prop.length(item.getNumeric_precision());
                    prop.precision(item.getNumeric_scale());
                } else if (("varchar".equals(item.getData_type()) || "char".equals(item.getData_type())) && item
                        .getCharacter_maximum_length() == 32) {
                    prop.policy(Column.Policy.GUID);
                    prop.length(item.getCharacter_maximum_length());
                }
            }
            if ("varchar".equals(item.getData_type()) || "char".equals(item.getData_type())) {
                prop.charset(Column.TableCharset.toCharset(item.getCharacter_set_name()));
                prop.length(item.getCharacter_maximum_length());
            } else if ("text".equals(item.getData_type())) {// text字段长度为65535
                prop.length(65535);
            } else if (Number.class.isAssignableFrom(prop.type())) { // 如果是数值的
                prop.length(item.getNumeric_precision());
                prop.precision(item.getNumeric_scale());
            }
            result.add(prop);
        }
        return result;
    }

    @Transactional(rollbackFor = DataAccessException.class)
    @Override
    public void updateTable(TableProperties tbl) {
        List<MysqlColumnInfo> list = jbaTemplate.queryMixModelList(
                "select * from information_schema.columns where table_name = '" + tbl
                        .getTableName() + "' AND table_schema='" + this.table_schema + "'", null, MysqlColumnInfo.class,
                null);
        // 先获取表结构信息
        List<ColumnProperties> dbColumnList = convert2ColumnProperties(list);

        ArrayList<String> sqls = new ArrayList<>();
        // 已经存在的列
        ArrayList<String> existsColumnsName = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE " + tbl.getTableName() + " ");
        String after = "";
        for (ColumnProperties col : tbl.getColumns().values()) {
            Optional<ColumnProperties> find = dbColumnList.stream()
                    .filter(t -> t.colName().equalsIgnoreCase(col.colName()))
                    .findFirst();
            //存在
            if (find.isPresent()) {
                ColumnProperties dbCol = find.get();
                existsColumnsName.add(dbCol.colName());
                if (!Objects.equals(javaClassToMysqlFieldName.get(col.type()),
                        javaClassToMysqlFieldName.get(dbCol.type())) || !dbCol.equals(col)) {// 如果对应的数据库字段类型不一样
                    sqls.add("MODIFY " + columnPro(col, after, false, dbCol));
                }
                existsColumnsName.add(dbCol.colName());
            } else {
                sqls.add("ADD COLUMN " + columnPro(col, after, false, null));
            }
            after = col.colName();
        }
        for (ColumnProperties item : dbColumnList) {
            // 不包含的则是需要删除的
            if (existsColumnsName.contains(item.colName())) {
                continue;
            }
            sqls.add("DROP COLUMN " + item.colName());
        }
        //  处理主键
        String oldPrimary = dbColumnList.stream().filter(x -> x.primary()).map(ColumnProperties::colName).sorted().collect(Collectors.joining(","));
        String newPrimary = tbl.getColumns().values().stream().filter(x -> x.primary()).map(ColumnProperties::colName).sorted().collect(Collectors.joining(","));
        if (!newPrimary.equals(oldPrimary)) {
            if (CommonUtil.isNotNullEmpty(oldPrimary)) {
                sqls.add("DROP PRIMARY KEY");
            }
            if (CommonUtil.isNotNullEmpty(newPrimary)) {
                sqls.add("ADD PRIMARY KEY (" + newPrimary + ")");
            }
        }
        for (int i = 0; i < sqls.size(); i++) {
            sql.append(sqls.get(i) + ",");
        }
        sql.append(" COMMENT = '" + tbl.getRemark() + "'");
        jbaTemplate.executeSQL(sql.toString());

        // 更新索引
        updateIndex(tbl);
    }

    /**
     * 需要设置精度
     *
     * @param clazz
     * @return
     */
    private boolean needPrecision(Class clazz) {
        if (clazz.equals(Double.class) || clazz.equals(double.class) || clazz.equals(float.class) || clazz
                .equals(Float.class)) {
            return true;
        }
        if (clazz.equals(BigDecimal.class)) {
            return true;
        }
        return false;
    }

    /**
     * 得到字段名+属性拼接
     *
     * @param col      要创建的列的属性
     * @param after    列在after之后
     * @param isCreate 是否是创建表操作
     * @param dbCol    数据库的列的属性，创建时没有
     * @return
     */
    private String columnPro(ColumnProperties col, String after, boolean isCreate, ColumnProperties dbCol) {
        StringBuilder temp = new StringBuilder();
        temp.append(col.colName() + " ");
        if (javaClassToMysqlFieldName.containsKey(col.type())) {
            temp.append(javaClassToMysqlFieldName.get(col.type()));
            if (needPrecision(col.type())) { // 需要精度的
                temp.append("(" + col.length() + "," + col.precision() + ")");
            }
        } else {
            if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > 20000) {
                temp.append("text BINARY");
            } else {
                if (col.length() > 0 && col.length() <= 32) {
                    temp.append("char(" + col.length() + ") BINARY");
                } else {
                    temp.append("varchar(" + col.length() + ") BINARY");
                }
            }
            if (null == dbCol || !col.charset().equals(dbCol.charset())) {
                temp.append(" CHARACTER SET " + col.charset().name());
            }
        }
        if (CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
            switch (col.policy()) {
                case AUTO:
                    temp.append(" AUTO_INCREMENT ");
                    break;
                default:
                    break;
            }
        } else {
            if (CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
                temp.append(" NOT NULL ");
            } else {
                temp.append(" NULL ");
            }
        }
        if (CommonUtil.isNotNullEmpty(col.remark())) {
            temp.append(" COMMENT '" + col.remark() + "'");
        }
        if (CommonUtil.isNotNullEmpty(col.defaultValue())) {
            if (col.type().equals(String.class)) {
                temp.append(" DEFAULT '" + col.defaultValue() + "'");
            } else if (col.type().isEnum()) {
                temp.append(" DEFAULT '" + col.defaultValue() + "'");
            } else if (Number.class.isAssignableFrom(col.type())) {
                temp.append(" DEFAULT " + col.defaultValue());
            } else if (col.type().equals(Timestamp.class)) {
                temp.append(" DEFAULT " + col.defaultValue());
            }
        } else if (!col.notNull()) {
            temp.append(" DEFAULT null ");
        }
        if (!isCreate) {
            if (CommonUtil.isNotNullEmpty(after)) {
                temp.append(" AFTER " + after);
            } else {
                temp.append(" FIRST");
            }
        }
        return temp.toString();
    }

    @Override
    public void dropTable(TableProperties tbl) {
        jbaTemplate.executeSQL("DROP TABLE " + tbl.getTableName());
    }

    /**
     * 更新索引
     */
    public void updateIndex(TableProperties tbl) {
        List<MysqlIndexInfo> dbIndexs = jbaTemplate.queryMixModelList("SHOW index FROM " + tbl.getTableName(), null, MysqlIndexInfo.class, null);
        // 过滤掉主键索引
        Map<String, List<MysqlIndexInfo>> oldIndexs = dbIndexs.stream().filter(x -> !"PRIMARY".equals(x.getKey_name())).collect(Collectors.groupingBy(MysqlIndexInfo::getKey_name));
        StringJoiner sql = new StringJoiner(",");
        // 已经存在的索引名称列表
        List<String> alreadyExistsIndex = new ArrayList<>();
        List<IndexProperties> indexs = tbl.getIndexs();
        Map<String, List<IndexProperties>> newIndexs = indexs.stream().collect(Collectors.groupingBy(IndexProperties::getIndexName));
        for (String indexName : newIndexs.keySet()) {
            List<IndexProperties> indexProperties = newIndexs.get(indexName);
            indexProperties.sort(Comparator.comparing(IndexProperties::getOrder));
            // 取第一个类型
            Index.IndexType type = indexProperties.get(0).getType();
            String colNames = indexProperties.stream().map(IndexProperties::getColumnName).collect(Collectors.joining(","));
            // 是否新增
            boolean add = false;
            // 存在则判断是否修改了
            if (oldIndexs.containsKey(indexName)) {
                alreadyExistsIndex.add(indexName);

                List<MysqlIndexInfo> mysqlIndexInfos = oldIndexs.get(indexName);
                Integer non_unique = mysqlIndexInfos.get(0).getNon_unique();
                String oldColNames = mysqlIndexInfos.stream().map(MysqlIndexInfo::getColumn_name).collect(Collectors.joining(","));
                /// 索引类型或者列不一致
                if (!non_unique.equals(type.ordinal()) || !oldColNames.equals(colNames)) {
                    sql.add("DROP INDEX " + indexName);
                    add = true;
                }
            } else {
                add = true;
            }
            // 添加
            if (add) {
                switch (type) {
                    case Unique:
                        sql.add("ADD UNIQUE INDEX " + indexName + " (" + colNames + ")");
                        break;
                    case Normal:
                        sql.add("ADD INDEX " + indexName + " (" + colNames + ")");
                        break;
                    default:
                        break;
                }
            }
        }
        // 删除剩余的
        for (String indexName : oldIndexs.keySet()) {
            // 不存在列表里的删除
            if (!alreadyExistsIndex.contains(indexName)) {
                sql.add("DROP INDEX " + indexName);
            }
        }
        if (sql.length() > 0) {
            jbaTemplate.executeSQL("ALTER TABLE " + tbl.getTableName() + " " + sql.toString() + ";");
        }
    }


}
