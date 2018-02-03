/**
 * 
 */
package xin.xihc.jba.db.bean;

/**
 * 
 * @author 席恒昌
 * @date 2018年1月26日
 * @version
 * @since
 */
public class OracleColumnInfo {

	/**
	 * 表名
	 */
	private String table_name;
	/**
	 * 字段名
	 */
	private String column_name;
	private String data_type;
	private String data_type_mod;
	private String data_type_owner;
	private Integer data_length;
	private Integer data_precision;
	private Integer data_scale;
	private String nullAble;
	private Integer column_id;
	private Integer default_length;
	private String data_default;
	private String num_distinct;
	private String low_value;
	private String high_value;
	private String density;
	private String num_nulls;
	private String num_buckets;
	private String last_analyzed;
	private String sample_size;
	private String character_set_name;
	private Integer char_col_decl_length;
	private String global_status;
	private String user_status;
	private String avg_col_len;
	private Integer char_length;
	private String char_used;
	private String v80_fmt_image;
	private String data_upgraded;
	private String histogram;

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getColumn_name() {
		return column_name;
	}

	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}

	public String getData_type() {
		return data_type;
	}

	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	public String getData_type_mod() {
		return data_type_mod;
	}

	public void setData_type_mod(String data_type_mod) {
		this.data_type_mod = data_type_mod;
	}

	public String getData_type_owner() {
		return data_type_owner;
	}

	public void setData_type_owner(String data_type_owner) {
		this.data_type_owner = data_type_owner;
	}

	public Integer getData_length() {
		return data_length;
	}

	public void setData_length(Integer data_length) {
		this.data_length = data_length;
	}

	public Integer getData_precision() {
		return data_precision;
	}

	public void setData_precision(Integer data_precision) {
		this.data_precision = data_precision;
	}

	public Integer getData_scale() {
		return data_scale;
	}

	public void setData_scale(Integer data_scale) {
		this.data_scale = data_scale;
	}

	public String getNullAble() {
		return nullAble;
	}

	public void setNullAble(String nullAble) {
		this.nullAble = nullAble;
	}

	public Integer getColumn_id() {
		return column_id;
	}

	public void setColumn_id(Integer column_id) {
		this.column_id = column_id;
	}

	public Integer getDefault_length() {
		return default_length;
	}

	public void setDefault_length(Integer default_length) {
		this.default_length = default_length;
	}

	public String getData_default() {
		return data_default;
	}

	public void setData_default(String data_default) {
		this.data_default = data_default;
	}

	public String getNum_distinct() {
		return num_distinct;
	}

	public void setNum_distinct(String num_distinct) {
		this.num_distinct = num_distinct;
	}

	public String getLow_value() {
		return low_value;
	}

	public void setLow_value(String low_value) {
		this.low_value = low_value;
	}

	public String getHigh_value() {
		return high_value;
	}

	public void setHigh_value(String high_value) {
		this.high_value = high_value;
	}

	public String getDensity() {
		return density;
	}

	public void setDensity(String density) {
		this.density = density;
	}

	public String getNum_nulls() {
		return num_nulls;
	}

	public void setNum_nulls(String num_nulls) {
		this.num_nulls = num_nulls;
	}

	public String getNum_buckets() {
		return num_buckets;
	}

	public void setNum_buckets(String num_buckets) {
		this.num_buckets = num_buckets;
	}

	public String getLast_analyzed() {
		return last_analyzed;
	}

	public void setLast_analyzed(String last_analyzed) {
		this.last_analyzed = last_analyzed;
	}

	public String getSample_size() {
		return sample_size;
	}

	public void setSample_size(String sample_size) {
		this.sample_size = sample_size;
	}

	public String getCharacter_set_name() {
		return character_set_name;
	}

	public void setCharacter_set_name(String character_set_name) {
		this.character_set_name = character_set_name;
	}

	public Integer getChar_col_decl_length() {
		return char_col_decl_length;
	}

	public void setChar_col_decl_length(Integer char_col_decl_length) {
		this.char_col_decl_length = char_col_decl_length;
	}

	public String getGlobal_status() {
		return global_status;
	}

	public void setGlobal_status(String global_status) {
		this.global_status = global_status;
	}

	public String getUser_status() {
		return user_status;
	}

	public void setUser_status(String user_status) {
		this.user_status = user_status;
	}

	public String getAvg_col_len() {
		return avg_col_len;
	}

	public void setAvg_col_len(String avg_col_len) {
		this.avg_col_len = avg_col_len;
	}

	public Integer getChar_length() {
		return char_length;
	}

	public void setChar_length(Integer char_length) {
		this.char_length = char_length;
	}

	public String getChar_used() {
		return char_used;
	}

	public void setChar_used(String char_used) {
		this.char_used = char_used;
	}

	public String getV80_fmt_image() {
		return v80_fmt_image;
	}

	public void setV80_fmt_image(String v80_fmt_image) {
		this.v80_fmt_image = v80_fmt_image;
	}

	public String getData_upgraded() {
		return data_upgraded;
	}

	public void setData_upgraded(String data_upgraded) {
		this.data_upgraded = data_upgraded;
	}

	public String getHistogram() {
		return histogram;
	}

	public void setHistogram(String histogram) {
		this.histogram = histogram;
	}

}
