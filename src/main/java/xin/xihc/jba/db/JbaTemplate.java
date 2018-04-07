package xin.xihc.jba.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import xin.xihc.utils.common.CommonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

/**
 * 对JDBCTemplate进行封装
 * 
 * @author 席恒昌
 * @Date 2017年7月16日
 * @Description 对NamedParameterJdbcTemplate和JdbcTemplate进行封装
 * @Version 2.1
 * @Modified 2017年11月26日
 */
@Component
@EnableTransactionManagement
public class JbaTemplate {

	private static final String jabLogName = "JbaExecSqls";

	private DBType dbType = DBType.MySql;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(String dbUrl) {
		if (dbUrl.startsWith("jdbc:mysql://")) {
			this.dbType = DBType.MySql;
		} else if (dbUrl.startsWith("jdbc:oracle:")) {
			this.dbType = DBType.Oracle;
		}
	}

	/**
	 * 获取该对象的所有字段(包含父类的)
	 * 
	 * @param clazz
	 * @return
	 */
	public List<Field> getAllFields(Class<?> clazz) {
		List<Field> res = new ArrayList<>(10);
		if (null == clazz) {
			return res;
		}
		while (!clazz.equals(Object.class)) {
			res.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return res;
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
	public boolean insertModel(Object model) {
		boolean ret = false;
		if (null == model) {
			return ret;
		}
		String sql = "";
		sql = getNamedParmeterSql_Insert(model.getClass().getSimpleName(), model);
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		LogFileUtil.info(jabLogName, "插入数据sql：" + sql + "\r\n参数为：" + CommonUtil.objToMap(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 根据字段更新数据
	 * 
	 * @param model
	 *            对象模型
	 * @param fieldNames
	 *            where子句条件字段数组
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean updateModel(Object model, String... fieldNames) throws RuntimeException {
		boolean ret = false;
		if (null == model) {
			return ret;
		}
		String sql = "";
		sql = getNamedParmeterSql_Update(model.getClass().getSimpleName(), model, fieldNames);
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		LogFileUtil.info(jabLogName, "更新数据sql：" + sql + "\r\n参数为：" + CommonUtil.objToMap(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @return
	 */
	public boolean executeSQL(String sql) {
		boolean res = false;
		int update = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(new Object()));
		LogFileUtil.info(jabLogName, "执行sql：" + sql);
		if (update > 0) {
			res = true;
		}
		return res;
	}

	/**
	 * 根据字段删除数据
	 * 
	 * @param model
	 *            对象模型
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean deleteModel(Object model) throws RuntimeException {
		boolean ret = false;
		String sql = "";
		sql = getNamedParmeterSql_Delete(model.getClass().getSimpleName(), model);
		// 不允许通过这里清空表
		if (sql.toLowerCase().indexOf("where") < 1) {
			LogFileUtil.info(jabLogName, "删除数据sql：" + sql + "\r\n不允许清空表数据,请使用sql清空");
			return false;
			// throw new RuntimeException("model对象为空对象");
		}
		int t = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
		LogFileUtil.info(jabLogName, "删除数据sql：" + sql + "\r\n参数为：" + CommonUtil.objToMap(model));
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 查询数据库 - 单个列值
	 * 
	 * @param sql
	 *            需要查询的sql
	 * @param model
	 *            sql中参数值
	 * @param clazz
	 *            需要返回的类型
	 * @return 返回查询某个单列的值
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
			LogFileUtil.info(jabLogName, "查询某列sql：" + sql + "\r\n参数为：" + CommonUtil.objToMap(model));
		} catch (Exception e) {
			LogFileUtil.exception(jabLogName, e);
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询数量
	 * 
	 * @param sql
	 *            sql语句
	 * @param model
	 *            参数
	 * @return
	 */
	public int queryCount(String sql, Object model) {
		int ret = 0;
		String temp = "SELECT　COUNT(1) FROM(" + sql + ")";
		Integer count = queryColumn(temp, model, Integer.class);
		if (null == count) {
			ret = 0;
		} else {
			ret = count.intValue();
		}
		return ret;
	}

	/**
	 * 查询单表的数量
	 * 
	 * @param model
	 *            表对象
	 * @return
	 */
	public int queryCount(Object model) {
		int ret = 0;
		String sql = "SELECT COUNT(1) FROM " + model.getClass().getSimpleName();
		StringBuilder where = new StringBuilder();
		for (Field field : getAllFields(model.getClass())) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					where.append(field.getName() + "=:" + field.getName() + " AND ");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogFileUtil.exception(jabLogName, e);
				e.printStackTrace();
			}
		}
		if (where.length() > 0) {
			sql = sql + " WHERE " + where.toString().substring(0, where.toString().lastIndexOf(" AND "));
		}
		Integer count = queryColumn(sql, model, Integer.class);
		if (null == count) {
			ret = 0;
		} else {
			ret = count.intValue();
		}
		return ret;
	}

	/**
	 * 
	 * 查询数据库 - 单个对象
	 * 
	 * @param tblName
	 *            对象对应的表名
	 * @param clazz
	 *            对象模型-创建查询条件
	 * @param orderBy
	 *            排序字段
	 * @return 单个对象
	 * 
	 */
	public <T> T queryModelOne(Object model, Class<T> clazz, String... orderBy) {
		T ret = null;
		List<T> list = queryModelList(model, clazz, null, orderBy);
		if (list == null || list.size() < 1) {
			return null;
		}
		ret = list.get(0);
		return ret;
	}

	/**
	 * 
	 * 查询数据库 - 单个混合对象
	 * 
	 * @param sql
	 *            sql语句
	 * 
	 * @param model
	 *            对象模型-创建查询条件
	 * @param clazz
	 *            返回的类型
	 * @return 单个对象
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
			LogFileUtil.info(jabLogName, "查询单个混合对象sql：" + sql + "\r\n参数为：" + CommonUtil.objToMap(model));
			if (list == null || list.size() < 1) {
				return null;
			}
			ret = list.get(0);
		} catch (Exception e) {
			LogFileUtil.exception(jabLogName, e);
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询单表--列表
	 * 
	 * @param model
	 *            对象对应的表名
	 * @param clazz
	 *            返回的类型
	 * @param pageInfo
	 *            分页信息
	 * @param orderBy
	 *            排序字段
	 * @return 列表
	 */
	public <T> List<T> queryModelList(Object model, Class<T> clazz, PageInfo pageInfo, String... orderBy) {
		List<T> ret = null;
		if (null == model) {
			return ret;
		}
		try {
			String sql = "SELECT * FROM " + model.getClass().getSimpleName();
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

			StringBuilder where = new StringBuilder();
			for (Field field : getAllFields(model.getClass())) {
				field.setAccessible(true);
				try {
					if (field.get(model) != null) {
						where.append(field.getName() + "=:" + field.getName() + " AND ");
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LogFileUtil.exception(jabLogName, e);
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
			LogFileUtil.info(jabLogName, "查询列表sql：" + sql_final + "\r\n参数为：" + CommonUtil.objToMap(model));
		} catch (Exception e) {
			LogFileUtil.exception(jabLogName, e);
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询数据库--混合对象列表
	 * 
	 * @param sql
	 *            sql语句
	 * 
	 * @param model
	 *            对象对应的表名
	 * @param clazz
	 *            返回的类型
	 * @param pageInfo
	 *            分页信息
	 * @return 列表
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
			LogFileUtil.info(jabLogName, "查询混合对象列表sql：" + sql_final + "\r\n参数为：" + CommonUtil.objToMap(model));
		} catch (Exception e) {
			LogFileUtil.exception(jabLogName, e);
			ret = null;
		}
		return ret;
	}

	// =============================组装sql============================
	/**
	 * insert语句拼接
	 * 
	 * @param tblName
	 *            表名
	 * @param model
	 *            对象参数
	 * @return
	 */
	private <T> String getNamedParmeterSql_Insert(String tblName, T model) {
		String res = "";
		if (model == null) {
			return res;
		}
		StringBuilder fieldList = new StringBuilder();
		StringBuilder valueList = new StringBuilder();
		res = "INSERT INTO " + tblName + "(";
		for (Field field : getAllFields(model.getClass())) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					fieldList.append(field.getName() + ",");
					valueList.append(":" + field.getName() + ",");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogFileUtil.exception(jabLogName, e);
				e.printStackTrace();
			}
		}
		if (fieldList.length() < 1) {
			return res;
		}
		res = res + fieldList.toString().substring(0, fieldList.toString().length() - 1) + ") VALUES ("
				+ valueList.toString().substring(0, valueList.toString().length() - 1) + ")";
		return res;
	}

	/**
	 * Update语句拼接
	 * 
	 * @param tblName
	 *            表名
	 * @param model
	 *            参数对象
	 * @param fieldNames
	 *            根据的字段
	 * @return
	 * @throws RuntimeException
	 */
	private <T> String getNamedParmeterSql_Update(String tblName, T model, String... fieldNames)
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
		for (Field field : getAllFields(model.getClass())) {
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
				LogFileUtil.exception(jabLogName, e);
				e.printStackTrace();
			}
		}
		if (fieldList.length() < 1) {
			return res;
		}
		res = res + fieldList.toString().substring(0, fieldList.toString().trim().length() - 1) + " WHERE "
				+ where.toString().substring(0, where.toString().lastIndexOf(" AND "));
		return res;
	}

	/**
	 * Delete语句拼接
	 * 
	 * @param tblName
	 *            表名
	 * @param model
	 *            参数对象
	 * @param fieldNames
	 *            根据的字段
	 * @return
	 * @throws RuntimeException
	 */
	private <T> String getNamedParmeterSql_Delete(String tblName, T model) throws RuntimeException {
		String res = "";
		if (model == null) {
			return res;
		}
		StringBuilder where = new StringBuilder();
		res = "DELETE FROM " + tblName;
		for (Field field : getAllFields(model.getClass())) {
			field.setAccessible(true);
			try {
				if (field.get(model) != null) {
					where.append(field.getName() + "=:" + field.getName() + " AND ");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogFileUtil.exception(jabLogName, e);
				e.printStackTrace();
			}
		}
		if (where.length() < 1) {
			return res;
		}
		res = res + " WHERE " + where.toString().substring(0, where.toString().lastIndexOf(" AND "));
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

		private Integer pageNo = 1; // 当前页数
		private Integer pageSize = 10;// 每页数量
		private Integer totalCount = 0;// 总数量
		private Integer totalPage = 0;// 总页数

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
			if (null == pageNo) {
				return;
			}
			this.pageNo = pageNo;
		}

		public Integer getPageSize() {
			return pageSize;
		}

		public void setPageSize(Integer pageSize) {
			if (null == pageSize) {
				return;
			}
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
		if (null == totalCount || totalCount < 1) {
			totalCount = 0;
			pageInfo.setTotalCount(0);
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
