/**
 *
 */
package xin.xihc.jba.db;

import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.annotation.Index;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.jba.db.bean.MysqlColumnInfo;
import xin.xihc.jba.db.bean.MysqlIndexInfo;
import xin.xihc.jba.scan.tables.InitDataInterface;
import xin.xihc.jba.scan.tables.properties.ColumnProperties;
import xin.xihc.jba.scan.tables.properties.IndexProperties;
import xin.xihc.jba.scan.tables.properties.TableProperties;
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
 * @code 增加:对java.sql.Date,Time,Timestamp字段的支持
 * @since 1.1.4
 */
public class DB_MySql_Opera implements I_TableOperation {

    /**
     * .@OnUpdateCurrentTimestamp注解适用的类型
     */
    public static final LinkedList<Class> ON_UPDATE_APPLIED = new LinkedList<>();

    /** java类型- mySQL类型 对应map */
    private static Map<Class, String> JAVA_CLASS_TO_MYSQL_FIELDNAME = new HashMap<>();

    /** char的最长的长度，超过后字段类型为varchar */
    private static final int CHAR_MAX_LENGTH = 64;
    /** varchar的最长的长度，超过字段类型为text */
    private static final int VARCHAR_MAX_LENGTH = 20000;

    static {
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(byte.class, "tinyint");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Byte.class, "tinyint");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(short.class, "smallint");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Short.class, "smallint");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(int.class, "int");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Integer.class, "int");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(long.class, "bigint");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Long.class, "bigint");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(String.class, "varchar"); // String为默认，不需要加上
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(double.class, "double");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Double.class, "double");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(float.class, "double");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Float.class, "double");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(BigDecimal.class, "decimal");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(java.util.Date.class, "datetime");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(java.sql.Timestamp.class, "timestamp");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(java.sql.Date.class, "date");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(java.sql.Time.class, "time");
        //		JAVA_CLASS_TO_MYSQL_FIELDNAME.put(boolean.class, "tinyint");
        JAVA_CLASS_TO_MYSQL_FIELDNAME.put(Boolean.class, "tinyint");

        ON_UPDATE_APPLIED.add(Date.class);
        ON_UPDATE_APPLIED.add(Timestamp.class);
    }

    /** 数据库schema */
    private String table_schema;

    /** ORM */
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
        String sql = "select count(1) FROM information_schema.TABLES WHERE table_name ='"
                + tblName + "' AND table_schema='" + this.table_schema + "'";
        Integer count = jbaTemplate.queryColumn(sql, null, Integer.class);
        if (count > 0) {
            res = true;
        }
        return res;
    }

    @Override
    public void createTable(TableProperties tbl) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + tbl.getTableName() + " ( ");
        for (ColumnProperties col : tbl.getColumns().values()) {
            sql.append(columnPro(col, null, true, null));
            sql.append(",");
        }
        // 更新索引
        String updateIndex = updateIndex(tbl, true);
        if (CommonUtil.isNotNullEmpty(updateIndex)) {
            sql.append(updateIndex);
            sql.append(",");
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

    }

    /**
     * 根据数据库列类型返回java数据类型
     *
     * @param dataType 列类型
     */
    private Class getClassByColumnDataType(String dataType) {
        Set<Class> keySet = JAVA_CLASS_TO_MYSQL_FIELDNAME.keySet();
        for (Class key : keySet) {
            if (JAVA_CLASS_TO_MYSQL_FIELDNAME.get(key).equals(dataType)) {
                return key;
            }
        }
        return String.class;
    }

    /**
     * 将表中列的属性转为ColumnProperties
     *
     * @param dbColumns 表的列属性列表
     */
    private List<ColumnProperties> convert2ColumnProperties(List<MysqlColumnInfo> dbColumns) {
        if (dbColumns.size() < 1) {
            return new ArrayList<>(0);
        }
        List<ColumnProperties> result = new ArrayList<>(dbColumns.size());
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
                if ("auto_increment".equalsIgnoreCase(item.getExtra())) { // 自增主键
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
            // 是否自动更新时间戳
            if ("on update CURRENT_TIMESTAMP".equalsIgnoreCase(item.getExtra())) {
                prop.onUpdateCurrentTimestamp(ON_UPDATE_APPLIED.contains(prop.type()));
            }
            result.add(prop);
        }
        return result;
    }

    @Override
    public void updateTable(TableProperties tbl) {
        List<MysqlColumnInfo> list = jbaTemplate.queryMixModelList(
                "select * from information_schema.columns where table_name = '" + tbl
                        .getTableName() + "' AND table_schema='" + this.table_schema + "'", null,
                MysqlColumnInfo.class, null);
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
                if (!Objects.equals(JAVA_CLASS_TO_MYSQL_FIELDNAME.get(col.type()),
                        JAVA_CLASS_TO_MYSQL_FIELDNAME.get(dbCol.type())) || !dbCol.equals(col)) {// 如果对应的数据库字段类型不一样
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
        // 更新索引
        String updateIndex = updateIndex(tbl, false);
        if (CommonUtil.isNotNullEmpty(updateIndex)) {
            sqls.add(updateIndex);
        }

        for (int i = 0; i < sqls.size(); i++) {
            sql.append(sqls.get(i) + ",");
        }
        sql.append(" COMMENT = '" + tbl.getRemark() + "'");
        jbaTemplate.executeSQL(sql.toString());
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
        if (JAVA_CLASS_TO_MYSQL_FIELDNAME.containsKey(col.type())) {
            temp.append(JAVA_CLASS_TO_MYSQL_FIELDNAME.get(col.type()));
            if (needPrecision(col.type())) { // 需要精度的
                temp.append("(" + col.length() + "," + col.precision() + ")");
            }
        } else {
            if (CommonUtil.isNotNullEmpty(col.length()) && col.length() > VARCHAR_MAX_LENGTH) {
                temp.append("text BINARY");
            } else {
                if (col.length() > 0 && col.length() <= CHAR_MAX_LENGTH) {
                    temp.append("char(" + col.length() + ") BINARY");
                } else {
                    temp.append("varchar(" + col.length() + ") BINARY");
                }
            }
            if (null == dbCol || !col.charset().equals(dbCol.charset())) {
                temp.append(" CHARACTER SET " + col.charset().name());
            }
        }
        // 允许为空
        if (CommonUtil.isNotNullEmpty(col.notNull()) && col.notNull()) {
            temp.append(" NOT NULL ");
        } else {
            temp.append(" NULL ");
        }

        // 主键
        if (CommonUtil.isNotNullEmpty(col.primary()) && col.primary()) {
            switch (col.policy()) {
                case AUTO:
                    temp.append(" AUTO_INCREMENT ");
                    break;
                default:
                    break;
            }
        }

        // 默认值
        if (CommonUtil.isNotNullEmpty(col.defaultValue())) {
            if (Number.class.isAssignableFrom(col.type())) {
                temp.append(" DEFAULT " + col.defaultValue());
            } else if (col.type().equals(Date.class) || col.type().equals(Timestamp.class)) {
                if ("CURRENT_TIMESTAMP".equalsIgnoreCase(col.defaultValue())) {
                    temp.append(" DEFAULT " + col.defaultValue());
                }
            } else {
                temp.append(" DEFAULT '" + col.defaultValue() + "'");
            }
        } else if (!col.notNull()) {
            temp.append(" DEFAULT null ");
        }

        // 是否自动更新时间戳
        if (ON_UPDATE_APPLIED.contains(col.type()) && col.onUpdateCurrentTimestamp()) {
            temp.append(" ON UPDATE CURRENT_TIMESTAMP ");
        }

        // 备注
        if (CommonUtil.isNotNullEmpty(col.remark())) {
            temp.append(" COMMENT '" + col.remark() + "'");
        }

        // 顺序
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
     *
     * @param tbl     表
     * @param created 是否是创建
     * @return
     */
    public String updateIndex(TableProperties tbl, boolean created) {
        StringJoiner sql = new StringJoiner(",");
        // 需要创建的主键
        String newPrimary = tbl.getColumns().values().stream().filter(ColumnProperties::primary)
                .sorted(Comparator.comparing(ColumnProperties::policy)).map(ColumnProperties::colName)
                .collect(Collectors.joining(","));
        List<MysqlIndexInfo> dbIndexs = new LinkedList<>();
        if (!created) {
            dbIndexs = jbaTemplate
                    .queryMixModelList("SHOW index FROM " + tbl.getTableName(), null,
                            MysqlIndexInfo.class, null);
        }
        String oldPrimary = dbIndexs.stream().filter(x -> "PRIMARY".equals(x.getKey_name()))
                .map(MysqlIndexInfo::getColumn_name).collect(Collectors.joining(","));
        // =---------------------------优先处理主键-----------------------=
        if (!newPrimary.equals(oldPrimary)) {
            if (CommonUtil.isNotNullEmpty(oldPrimary)) {
                sql.add("DROP PRIMARY KEY");
            }
            if (CommonUtil.isNotNullEmpty(newPrimary)) {
                String prefix = "";
                if (!created) {
                    prefix = "ADD";
                }
                sql.add(prefix + " PRIMARY KEY (" + newPrimary + ")");
            }
        }

        // 过滤掉主键索引
        Map<String, List<MysqlIndexInfo>> oldIndexs = dbIndexs.stream().filter(x -> !"PRIMARY".equals(x.getKey_name()))
                .collect(Collectors.groupingBy(MysqlIndexInfo::getKey_name));
        // 已经存在的索引名称列表
        List<String> alreadyExistsIndex = new LinkedList<>();
        List<IndexProperties> indexs = tbl.getIndexs();
        for (IndexProperties newIndex : indexs) {
            String indexName = newIndex.getIndexName();
            Index.IndexType type = newIndex.getType();
            String comment = newIndex.getRemark();
            String colNames = String.join(",", newIndex.getColumnNames());
            // 是否新增
            boolean add = false;
            // 存在则判断是否修改了
            if (oldIndexs.containsKey(indexName)) {
                alreadyExistsIndex.add(indexName);

                List<MysqlIndexInfo> mysqlIndexInfos = oldIndexs.get(indexName);
                Integer non_unique = Math.max(mysqlIndexInfos.get(0).getNon_unique(),
                        "FULLTEXT".equalsIgnoreCase(mysqlIndexInfos.get(0).getIndex_type()) ? 2 : 0);
                String oldColNames = mysqlIndexInfos.stream().map(MysqlIndexInfo::getColumn_name)
                        .collect(Collectors.joining(","));
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
                String prefix = "";
                if (!created) {
                    prefix = "ADD";
                }
                switch (type) {
                    case Unique:
                        sql.add(prefix + " UNIQUE INDEX " + indexName + " (" + colNames + ") COMMENT '" + comment + "'");
                        break;
                    case Normal:
                        sql.add(prefix + " INDEX " + indexName + " (" + colNames + ") COMMENT '" + comment + "'");
                        break;
                    case FullText:
                        sql.add(prefix + " FULLTEXT INDEX " + indexName + " (" + colNames + ") COMMENT '" + comment + "'");
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
        return sql.toString();
    }


}
