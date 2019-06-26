/**
 *
 */
package xin.xihc.jba.tables.properties;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 表属性
 *
 * @author Leo.Xi
 * @date 2018年1月12日
 * @since
 */
public class TableProperties {

    /**
     * 对象属性-表字段属性
     */
    private final LinkedHashMap<String, ColumnProperties> columns = new LinkedHashMap<>(16);// 表的列属性
    /**
     * 表名称
     */
    private String tableName;// 表名称
    /**
     * 表模型对象-获取初始化数据用
     */
    private Object tableBean; // 表模型对象
    /**
     * 表备注
     */
    private String remark;//
    /**
     * 表创建or更新的顺序,默认9999
     */
    private int order;
    /**
     * 是否忽略这张表
     */
    private boolean ignore;
    /**
     * 表的索引
     */
    private LinkedList<IndexProperties> indexs = new LinkedList<>();

    public Object getTableBean() {
        return tableBean;
    }

    public void setTableBean(Object tableBean) {
        this.tableBean = tableBean;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LinkedHashMap<String, ColumnProperties> getColumns() {
        return columns;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    /**
     * 通过字段名称获取表的列属性
     *
     * @param fieldName 表对象字段名
     * @return 列属性
     */
    public ColumnProperties getColProperties(String fieldName) {
        if (columns.containsKey(fieldName)) {
            return columns.get(fieldName);
        } else {
            throw new RuntimeException("【" + fieldName + "】属性不存在");
        }
    }

    /**
     * 每次添加时进行排序
     *
     * @param fieldName 对象属性名
     * @param prop      表的列属性信息
     * @author Leo.Xi
     * @date 2019/6/26
     * @since 0.0.1
     */
    public void addColumn(String fieldName, ColumnProperties prop) {
        // 临时的表的列属性
        LinkedHashMap<String, ColumnProperties> temp = new LinkedHashMap<>(columns.size() + 1);
        for (String key : this.columns.keySet()) {
            ColumnProperties columnProperties = this.columns.get(key);
            if (columnProperties.order() > prop.order()) { // 小于或者等于时保留原来顺序
                if (!temp.containsKey(fieldName)) {
                    temp.put(fieldName, prop);
                }
            }
            temp.put(key, columnProperties);
        }
        // 都小于时,最后追加
        if (!temp.containsKey(fieldName)) {
            temp.put(fieldName, prop);
        }
        this.columns.clear();
        this.columns.putAll(temp);
        temp = null;
    }

    public List<IndexProperties> getIndexs() {
        return indexs;
    }

    /**
     * 添加索引
     *
     * @param index
     */
    public void addIndex(IndexProperties index) {
        this.indexs.add(index);
    }

    /**
     * 添加最前面索引
     *
     * @param index
     */
    public void addFirstIndex(IndexProperties index) {
        this.indexs.addFirst(index);
    }

}
