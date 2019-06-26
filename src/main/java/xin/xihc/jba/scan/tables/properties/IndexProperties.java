package xin.xihc.jba.scan.tables.properties;

import xin.xihc.jba.annotation.Index;


/**
 * 索引属性
 *
 * @Author Leo.Xi
 * @Date 2019/1/28 11:18
 * @Version 1.0
 **/
public class IndexProperties {

    /**
     * 索引名
     */
    private String indexName;

    /**
     * 索引类型
     */
    private Index.IndexType type;

    /**
     * 索引的列名
     */
    private String[] columnNames;

    /**
     * 备注
     */
    private String remark = "";

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Index.IndexType getType() {
        return type;
    }

    public void setType(Index.IndexType type) {
        this.type = type;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
