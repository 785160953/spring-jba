package xin.xihc.jba.db;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

import xin.xihc.utils.common.CommonUtil;

/**
 * 对JDBTemplate进行封装
 * 
 * @author 席恒昌
 * @Date 2017年7月16日
 * @Description 对NamedParameterJdbcTemplate和JdbcTemplate进行封装
 * @Version 2.1
 * @Modified 2017年11月26日
 */
@Component
public class MyOrmJdbcTemplate {

	/**
	 * 
	 */
	public MyOrmJdbcTemplate() {
		if (null != this.dataSource) {
			String driverClassName = this.dataSource.getDriverClassName();
			if (driverClassName.startsWith("jdbc:mysql://")) {
				this.dbType = DBType.MySql;
			} else if (driverClassName.startsWith("jdbc:oracle:")) {
				this.dbType = DBType.Oracle;
			}
		}
	}

	/**
	 * 数据源
	 */
	@Qualifier("dataSource")
	private DruidDataSource dataSource = null;

	private DBType dbType = DBType.MySql;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

	/**
	 * 初始化dataSource,需要手动关闭之前的dataSource
	 * 
	 * @param dataSource
	 * @param isInited
	 * @throws SQLException
	 */
	public void init(DruidDataSource dataSource, boolean isInited) throws Exception {
		if (null != this.dataSource && this.dataSource.isInited()) {
			this.dataSource.close();
		}
		if (isInited) {
			try {
				dataSource.init();
			} catch (SQLException e) {
				e.printStackTrace();
				dataSource.close();
				throw new Exception(e);
			}
		}
		this.dataSource = dataSource;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/*--------------------------------------------------------------------------------
	 * 
	 * 以下为【NamedParameterJdbcTemplate】对数据库操作
	 * 可以使用注解事务 - @Transactional
	 * 
	 * ------------------------------------------------------------------------------*/

	// ===================对表对象进行操作==========================
	/**
	 * 插入数据库 INSERT INTO tblName (id,name) VALUES (:id,:name);
	 * 
	 * @Descript :id后面的必须和model对象的属性名一致
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型
	 * @return 是否成功
	 */
	public boolean insertModel(String tblName, Object model) {
		boolean ret = false;
		String sql = "";
		sql = getNamedParmeterSql_Insert(tblName, model);
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 根据字段更新数据
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型
	 * @param fieldNames
	 *            where子句条件字段数组
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean updateModel(String tblName, Object model, String... fieldNames) throws RuntimeException {
		boolean ret = false;
		String sql = "";
		sql = getNamedParmeterSql_Update(tblName, model, fieldNames);
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 根据字段删除数据
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型
	 * @param fieldNames
	 *            where子句条件字段数组
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean deleteModel(String tblName, Object model, String... fieldNames) throws RuntimeException {
		boolean ret = false;
		String sql = "";
		sql = getNamedParmeterSql_Delete(tblName, model, fieldNames);
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 查询数据库 - 单个列值
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型-创建查询条件
	 * @return 是否成功
	 */
	public <T> T queryColumn(String sql, Object model, Class<T> clazz) {
		T ret = null;
		try {
			if (model != null) {
				ret = namedParameterJdbcTemplate.queryForObject(sql, new BeanPropertySqlParameterSource(model), clazz);
			} else {
				ret = namedParameterJdbcTemplate.queryForObject(sql, new BeanPropertySqlParameterSource(new Object()),
						clazz);
			}
		} catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	/**
	 * 
	 * 查询数据库 - 单个对象
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型-创建查询条件
	 * @return 是否成功
	 * 
	 */
	public <T> T queryModelOne(String tblName, Object model, Class<T> clazz) {
		T ret = null;
		try {
			String sql = "SELECT * FROM " + tblName;
			List<T> list = null;
			if (model != null) {
				StringBuilder where = new StringBuilder();
				for (Field field : model.getClass().getDeclaredFields()) {
					field.setAccessible(true);
					try {
						if (field.get(model) != null) {
							where.append(field.getName() + "=:" + field.getName() + " AND ");
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				if (where.length() > 0) {
					sql = sql + " WHERE " + where.toString().substring(0, where.toString().lastIndexOf(" AND "));
				}
				list = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(model),
						new BeanPropertyRowMapper<>(clazz));
			} else {
				list = namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz));
			}
			if (list == null || list.size() < 1) {
				ret = null;
			}
			ret = list.get(0);
		} catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	/**
	 * 
	 * 查询数据库 - 单个混合对象
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * 
	 * @param <T>
	 *            对象模型-创建查询条件
	 * @return 是否成功
	 * 
	 */
	public <T> T queryMixModelOne(String sql, Object model, Class<T> clazz) {
		T ret = null;
		try {
			List<T> list = null;
			if (model != null) {
				list = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(model),
						new BeanPropertyRowMapper<>(clazz));
			} else {
				list = namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz));
			}
			if (list == null || list.size() < 1) {
				ret = null;
			}
			ret = list.get(0);
		} catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询单表--列表
	 * 
	 * @param <T>
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型-创建查询条件
	 * @return 是否成功
	 */
	public <T> List<T> queryModelList(String tblName, Object model, Class<T> clazz, PageInfo pageInfo,
			String... orderBy) {
		List<T> ret = null;
		try {
			String sql = "SELECT * FROM " + tblName;
			String sql_final = sql;
			String sOrder = "";
			// 存在排序
			if (orderBy.length > 0) {
				for (int i = 0; i < orderBy.length; i++) {
					if (CommonUtil.isNotNullEmpty(orderBy[i])) {
						if (sOrder == "") {
							sOrder = "ORDER BY " + orderBy[i];
						} else {
							sOrder += "," + orderBy[i];
						}
					}
				}
			}
			// 存在where子句
			if (model != null) {
				StringBuilder where = new StringBuilder();
				for (Field field : model.getClass().getDeclaredFields()) {
					field.setAccessible(true);
					try {
						if (field.get(model) != null) {
							where.append(field.getName() + "=:" + field.getName() + " AND ");
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				if (where.length() > 0) {
					sql = sql + " WHERE " + where.toString().substring(0, where.toString().lastIndexOf(" AND "));
				}
				sql_final = sql + " " + sOrder;
				if (pageInfo != null) {
					sql_final = getNamedPageSql(sql_final, model, pageInfo);
				}
				ret = namedParameterJdbcTemplate.query(sql_final, new BeanPropertySqlParameterSource(model),
						new BeanPropertyRowMapper<>(clazz));
			} else {
				sql_final += " " + sOrder;
				if (pageInfo != null) {
					sql_final = getNamedPageSql(sql_final, model, pageInfo);
				}
				ret = namedParameterJdbcTemplate.query(sql_final, new BeanPropertyRowMapper<>(clazz));
			}
		} catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询数据库--混合对象列表
	 * 
	 * @param <T>
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param <T>
	 *            对象模型-创建查询条件
	 * @return 是否成功
	 */
	public <T> List<T> queryMixModelList(String sql, Object model, Class<T> clazz, PageInfo pageInfo) {
		List<T> ret = null;
		try {
			String sql_final = sql;
			if (pageInfo != null) {
				sql_final = getNamedPageSql(sql, model, pageInfo);
			}
			if (model != null) {
				ret = namedParameterJdbcTemplate.query(sql_final, new BeanPropertySqlParameterSource(model),
						new BeanPropertyRowMapper<>(clazz));
			} else {
				ret = namedParameterJdbcTemplate.query(sql_final, new BeanPropertyRowMapper<>(clazz));
			}
		} catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	// =============================组装sql============================
	/**
	 * insert语句拼接
	 * 
	 * @param <T>
	 * @param tblName
	 * @return
	 */
	public <T> String getNamedParmeterSql_Insert(String tblName, T model) {
		String res = "";
		if (model == null) {
			return res;
		}
		StringBuilder fieldList = new StringBuilder();
		StringBuilder valueList = new StringBuilder();
		res = "INSERT INTO " + tblName + "(";
		for (Field field : model.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					fieldList.append(field.getName() + ",");
					valueList.append(":" + field.getName() + ",");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (fieldList.length() < 1) {
			return res;
		}
		res = res + fieldList.toString().substring(0, fieldList.toString().length() - 1) + ") VALUES ("
				+ valueList.toString().substring(0, valueList.toString().length() - 1) + ")";
		System.err.println(res);
		return res;
	}

	/**
	 * Update语句拼接
	 * 
	 * @param <T>
	 * @param tblName
	 * @param where
	 * @return
	 * @throws Exception
	 */
	public <T> String getNamedParmeterSql_Update(String tblName, T model, String... fieldNames)
			throws RuntimeException {
		String res = "";
		if (model == null) {
			return res;
		}
		if (fieldNames.length < 1) {
			return res;
		}
		// 转小写
		for (int i = 0; i < fieldNames.length; i++) {
			fieldNames[i] = fieldNames[i].toLowerCase();
		}
		List<String> fieldLst = Arrays.asList(fieldNames);
		StringBuilder fieldList = new StringBuilder();
		StringBuilder where = new StringBuilder();
		res = "UPDATE " + tblName + " SET ";
		for (Field field : model.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					if (!fieldLst.contains(field.getName().toLowerCase())) {
						fieldList.append(field.getName() + "=:" + field.getName() + ",");
					} else {
						where.append(field.getName() + "=:" + field.getName() + " AND ");
					}
				} else if (fieldLst.contains(field.getName().toLowerCase())) {
					throw new RuntimeException("WHERE子句中存在字段【" + field.getName() + "】值为空");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (fieldList.length() < 1) {
			return res;
		}
		res = res + fieldList.toString().substring(0, fieldList.toString().trim().length() - 1) + " WHERE "
				+ where.toString().substring(0, where.toString().lastIndexOf(" AND "));
		System.err.println(res);
		return res;
	}

	/**
	 * Delete语句拼接
	 * 
	 * @param <T>
	 * @param tblName
	 * @param fieldNames
	 * @return
	 * @throws Exception
	 */
	public <T> String getNamedParmeterSql_Delete(String tblName, T model, String... fieldNames)
			throws RuntimeException {
		String res = "";
		if (model == null) {
			return res;
		}
		if (fieldNames.length < 1) {
			return res;
		}
		// 转小写
		for (int i = 0; i < fieldNames.length; i++) {
			fieldNames[i] = fieldNames[i].toLowerCase();
		}
		List<String> fieldLst = Arrays.asList(fieldNames);
		StringBuilder fieldList = new StringBuilder();
		StringBuilder where = new StringBuilder();
		res = "DELETE FROM" + tblName;
		for (Field field : model.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					if (!fieldLst.contains(field.getName().toLowerCase())) {
						fieldList.append(field.getName() + "=:" + field.getName() + ",");
					} else {
						where.append(field.getName() + "=:" + field.getName() + " AND ");
					}
				} else if (fieldLst.contains(field.getName().toLowerCase())) {
					throw new RuntimeException("WHERE子句中存在字段【" + field.getName() + "】值为空");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (fieldList.length() < 1) {
			return res;
		}
		res = res + fieldList.toString().substring(0, fieldList.toString().trim().length() - 1) + " WHERE "
				+ where.toString().substring(0, where.toString().lastIndexOf(" AND "));
		System.err.println(res);
		return res;
	}

	/*
	 * ======================================================================
	 * 分页信息
	 * ======================================================================
	 */

	/**
	 * 分页信息
	 * 
	 * @author 席恒昌
	 * @date 2018年1月19日
	 * @version
	 * @since
	 */
	public static class PageInfo {

		private Integer pageNo; // 当前页数
		private Integer pageSize;// 每页数量
		private Integer totalCount;// 总数量
		private Integer totalPage;// 总页数

		public Integer getTotalCount() {
			return totalCount;
		}

		public void setTotalCount(Integer totalCount) {
			totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
			this.totalCount = totalCount;
		}

		public Integer getTotalPage() {
			return totalPage;
		}

		public void setTotalPage(Integer totalPage) {
			this.totalPage = totalPage;
		}

		public Integer getPageNo() {
			return pageNo;
		}

		public void setPageNo(Integer pageNo) {
			this.pageNo = pageNo;
		}

		public Integer getPageSize() {
			return pageSize;
		}

		public void setPageSize(Integer pageSize) {
			this.pageSize = pageSize;
		}

	}

	/**
	 * 数据库类型
	 * 
	 * @author 席恒昌
	 * @date 2018年1月19日
	 * @version
	 * @since
	 */
	public enum DBType {
		MySql, Oracle
	}

	public <T> String getNamedPageSql(String sql, T model, PageInfo pageInfo) {
		String pageSql = "";
		// 先查询总数
		String sql_Count = "SELECT COUNT(1) FROM (" + sql + ") t_temp";
		Integer totalCount = queryColumn(sql_Count, model, Integer.class);
		if (null == totalCount) {
			totalCount = 0;
			pageInfo.setPageNo(1);
			return sql;
		}
		// 计算总页数
		pageInfo.setTotalCount(totalCount);
		if (pageInfo.getPageNo() < 1) {
			pageInfo.setPageNo(1);
		} else if (pageInfo.getPageNo() > pageInfo.getTotalPage()) {
			pageInfo.setPageNo(pageInfo.getTotalPage());
		}
		// 计算起始索引
		Integer iBegin = 0;
		switch (this.dbType) {
		case MySql:// 使用limit 0, 10分页 -- 索引从0开始
			iBegin = (pageInfo.getPageNo() - 1) * pageInfo.getPageSize();
			pageSql = sql + " LIMIT " + iBegin + "," + pageInfo.getPageSize();
			break;
		case Oracle:// 使用rownum分页
			iBegin = (pageInfo.getPageNo() - 1) * pageInfo.getPageSize() + 1;
			Integer iEnd = iBegin + pageInfo.getPageSize() - 1;
			pageSql = "SELECT * FROM (SELECT t_temp.*, ROWNUM as rowno FROM (" + sql + ") t_temp) WHERE rowno BETWEEN "
					+ iBegin + " AND " + iEnd;
			break;

		default:
			return sql;
		}
		return pageSql;
	}

}
