package xin.xihc.jba.db.bean;

/**
 * mysql索引对象
 *
 * @Author Leo.Xi
 * @Date 2019/1/28 13:28
 * @Version 1.0
 **/
public class MysqlIndexInfo {

    /**
     * 表的名称。
     */
    private String table;
    /**
     * 如果索引不能包括重复词，则为0。如果可以，则为1。
     */
    private Integer non_unique = 1;
    /**
     * 索引的名称。
     */
    private String key_name;
    /**
     * 索引中的列序列号，从1开始。
     */
    private Integer seq_in_index = 1;
    /**
     * 列名称。
     */
    private String column_name;
    /**
     * 列以什么方式存储在索引中。在MySQL中，有值‘A’（升序）或NULL（无分类）。
     */
    private String collation;
    /**
     * 索引中唯一值的数目的估计值,通过运行ANALYZE TABLE或myisamchk -a可以更新,基数根据被存储为整数的统计数据来计数,所以即使对于小型表,该值也没有必要是精确的,基数越大,当进行联合时,MySQL使用该索引的机会就越大.
     */
    private Integer cardinality;
    /**
     * 如果列只是被部分地编入索引,则为被编入索引的字符的数目,如果整列被编入索引,则为NULL.
     */
    private String sub_part;
    /**
     * 指示关键字如何被压缩,如果没有被压缩,则为NULL.
     */
    private String packed;
    /**
     * 用过的索引方法（BTREE,FULLTEXT,HASH,RTREE）.
     */
    private String index_type;
    /**
     * 更多评注.
     */
    private String comment;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Integer getNon_unique() {
        return non_unique;
    }

    public void setNon_unique(Integer non_unique) {
        this.non_unique = non_unique;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public Integer getSeq_in_index() {
        return seq_in_index;
    }

    public void setSeq_in_index(Integer seq_in_index) {
        this.seq_in_index = seq_in_index;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public Integer getCardinality() {
        return cardinality;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    public String getSub_part() {
        return sub_part;
    }

    public void setSub_part(String sub_part) {
        this.sub_part = sub_part;
    }

    public String getPacked() {
        return packed;
    }

    public void setPacked(String packed) {
        this.packed = packed;
    }

    public String getIndex_type() {
        return index_type;
    }

    public void setIndex_type(String index_type) {
        this.index_type = index_type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
