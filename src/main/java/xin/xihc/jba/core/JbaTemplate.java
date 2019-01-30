package xin.xihc.jba.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import xin.xihc.jba.tables.TableManager;
import xin.xihc.jba.utils.SQLUtils;
import xin.xihc.utils.common.CommonUtil;
import xin.xihc.utils.json.JsonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对JDBCTemplate进行封装
 *
 * @author Leo.Xi
 * @Date 2017年7月16日
 * @Description 对NamedParameterJdbcTemplate和JdbcTemplate进行封装
 * @Version 1.1.7
 * @Modified 2017年11月26日
 */
@Component
@EnableTransactionManagement
public class JbaTemplate {

	public static final String jbaLogName = "JbaExecSqls";

	/**
	 * 记录日志
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(JbaTemplate.class);

	/**
	 * 是否使用slf4j记录日志,默认使用
	 */
	private boolean useSlf4jLog = true;

	/**
	 * 开放出去,可以自己new实例,同时管理多个数据源
	 *
	 * @param dataSource 数据源
	 */
	@Autowired
	public JbaTemplate(DataSource dataSource) {
		super();
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public boolean isUseSlf4jLog() {
		return useSlf4jLog;
	}

	/**
	 * 设置是用什么记录日志
	 *
	 * @param useSlf4jLog
	 */
	public void setUseSlf4jLog(boolean useSlf4jLog) {
		this.useSlf4jLog = useSlf4jLog;
	}

	/**
	 * 获取当前的JDBCTemplate
	 *
	 * @return JdbcOperations
	 * @modified 1.3.3
	 * @since 1.1.7
	 */
	public NamedParameterJdbcTemplate getJdbcOperations() {
		return this.namedParameterJdbcTemplate;
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
	 * @param model 对象模型
	 * @return 是否成功
	 * @Descript :id后面的必须和model对象的属性名一致
	 */
	public boolean insertModel(Object model) {
		Objects.requireNonNull(model, "表对象model不允许为空");

		boolean ret = false;
		String sql = SQLUtils.getInsertSql(model);
		long start = System.currentTimeMillis();// 记录开始时间戳
		int t = namedParameterJdbcTemplate.update(sql, new JbaBeanProperty(model));
		infoSql(sql, model, start);
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 批量插入数据库<br>
	 * 以对象值最多的生成sql
	 *
	 * @param models
	 * @return
	 */
	public void insertModels(Object[] models) {
		Objects.requireNonNull(models, "表对象models不允许为空");
		if (models.length < 1) {// 为空暂时不处理
			return;
			//			throw new IllegalArgumentException("表对象models不允许为空");
		}

		String sql = "";
		String temp = "";
		JbaBeanProperty[] params = new JbaBeanProperty[models.length];
		for (int i = 0, l = models.length; i < l; i++) {
			temp = SQLUtils.getInsertSql(models[i]);
			if (sql.length() < temp.length()) { // 以参数最多的那个为准
				sql = temp;
			}
			params[i] = new JbaBeanProperty(models[i]);
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		namedParameterJdbcTemplate.batchUpdate(sql, params);
		infoSql(sql, models, start);
	}

	/**
	 * 根据字段更新数据
	 *
	 * @param model      对象模型
	 * @param fieldNames where子句条件字段数组
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean updateModel(Object model, final String... fieldNames) throws RuntimeException {
		Objects.requireNonNull(model, "表对象model不允许为空");

		boolean ret = false;
		String sql = SQLUtils.getUpdateSql(model, fieldNames);
		long start = System.currentTimeMillis();// 记录开始时间戳
		int t = namedParameterJdbcTemplate.update(sql, new JbaBeanProperty(model));
		infoSql(sql, model, start);
		if (t > 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 批量执行sql，插入INSERT、UPDATE都可以
	 *
	 * @param sql    sql语句
	 * @param params SQL语句中对应的参数
	 */
	public void batchUpdate(final String sql, Map<String, ?>[] params) {
		Objects.requireNonNull(params, "params不允许为空");
		if (params.length < 1) {// 为空暂时不处理
			return;
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		namedParameterJdbcTemplate.batchUpdate(sql, params);
		infoSql(sql, params, start);
	}

	/**
	 * 根据字段删除数据
	 *
	 * @param model 对象模型
	 * @return 是否成功
	 * @throws RuntimeException
	 */
	public boolean deleteModel(Object model) throws RuntimeException {
		Objects.requireNonNull(model, "表对象model不允许为空");

		boolean ret = false;
		String sql = SQLUtils.getDeleteSql(model);
		// 不允许通过这里清空表
		if (sql.toLowerCase().indexOf("where") < 1) {
			LogFileUtil.info(jbaLogName, "删除数据sql：" + sql + "\r\n不允许清空表数据,请使用sql清空");
			return false;
			// throw new RuntimeException("model对象为空对象");
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		int t = namedParameterJdbcTemplate.update(sql, new JbaBeanProperty(model));
		infoSql(sql, model, start);
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
	public boolean executeSQL(final String sql) {
		boolean res = false;
		long start = System.currentTimeMillis();// 记录开始时间戳
		int update = namedParameterJdbcTemplate.update(sql, new JbaBeanProperty(new Object()));
		infoSql(sql, null, start);
		if (update > 0) {
			res = true;
		}
		return res;
	}

	/**
	 * 执行sql语句
	 *
	 * @param sql    sql带有参数
	 * @param params 参数对象 支持map
	 * @return
	 */
	public boolean executeSQL(final String sql, Object params) {
		boolean res = false;
		if (null == params) {
			params = new Object();
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		int update;
		if (params instanceof Map) {
			update = namedParameterJdbcTemplate.update(sql, new JbaMapSqlSource((Map<String, ?>) params));
		} else {
			update = namedParameterJdbcTemplate.update(sql, new JbaBeanProperty(params));
		}
		infoSql(sql, params, start);
		if (update > 0) {
			res = true;
		}
		return res;
	}

	/**
	 * 查询数据库 - 单个列值
	 *
	 * @param sql    需要查询的sql
	 * @param params sql中参数值 支持map
	 * @param clazz  需要返回的类型
	 * @return 返回查询某个单列的值
	 */
	public <T> T queryColumn(final String sql, Object params, Class<T> clazz) {
		T ret = null;
		try {
			long start = System.currentTimeMillis();// 记录开始时间戳
			if (params != null) {
				if (params instanceof Map) {
					ret = namedParameterJdbcTemplate
							.queryForObject(sql, new JbaMapSqlSource((Map<String, ?>) params), clazz);
				} else {
					ret = namedParameterJdbcTemplate.queryForObject(sql, new JbaBeanProperty(params), clazz);
				}
			} else {
				ret = namedParameterJdbcTemplate.queryForObject(sql, new JbaBeanProperty(new Object()), clazz);
			}
			infoSql(sql, params, start);
		} catch (Exception e) {
			LogFileUtil.exception(jbaLogName, e);
			ret = null;
		}
		return ret;
	}

	/**
	 * 查询数量(会自动拼接select count(1)),查询混合对象时用
	 *
	 * @param sql    sql语句
	 * @param params 参数 支持map
	 * @return
	 */
	public int queryCount(final String sql, Object params) {
		int ret;
		String temp = "SELECT count(1) FROM(" + sql + ") t_temp_t";
		Integer count = queryColumn(temp, params, Integer.class);
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
	 * @param model 表对象
	 * @return 数量
	 */
	public int queryCount(Object model) {
		Objects.requireNonNull(model, "表对象model不允许为空");
		int ret;
		String sql = "SELECT COUNT(1) FROM " + TableManager.getTable(model.getClass()).getTableName();

		String where = SQLUtils.getWhereSql(model);
		if (where.length() > 0) {
			sql = sql + " WHERE " + where;
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
	 * 查询数据库 - 单个对象
	 *
	 * @param model   对象对应的表名
	 * @param clazz   返回的类型
	 * @param orderBy 排序字段
	 * @return 单个对象
	 */
	public <T> T queryModelOne(Object model, Class<T> clazz, final String... orderBy) {
		List<T> list = queryModelList(model, clazz, null, orderBy);
		if (list == null || list.size() < 1) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * 查询数据库 - 单个混合对象
	 *
	 * @param sql    sql语句
	 * @param params 对象模型-创建查询条件 支持map
	 * @param clazz  返回的类型
	 * @return 单个对象
	 */
	public <T> T queryMixModelOne(final String sql, Object params, Class<T> clazz) {
		List<T> list = queryMixModelList(sql, params, clazz, null);
		if (list == null || list.size() < 1) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * 查询单表--列表
	 *
	 * @param model    对象对应的表名
	 * @param clazz    返回的类型
	 * @param pageInfo 分页信息
	 * @param orderBy  排序字段
	 * @return 列表
	 */
	public <T> List<T> queryModelList(Object model, Class<T> clazz, PageInfo pageInfo, final String... orderBy) {
		Objects.requireNonNull(model, "表对象model不允许为空");

		List<T> ret;
		// 获取字段列表
		String fields = SQLUtils.getModelFields(model.getClass()).stream().map(Field::getName)
		                        .collect(Collectors.joining(","));
		String sql = "SELECT " + fields + " FROM " + TableManager.getTable(model.getClass()).getTableName();
		String sql_final;
		// 存在排序
		StringJoiner by = new StringJoiner(",");
		if (orderBy.length > 0) {
			for (int i = 0; i < orderBy.length; i++) {
				if (CommonUtil.isNotNullEmpty(orderBy[i])) {
					by.add(orderBy[i]);
				}
			}
		}
		// 存在where子句
		String where = SQLUtils.getWhereSql(model);
		if (where.length() > 0) {
			sql = sql + " WHERE " + where;
		}
		if (by.length() > 0) {
			sql = sql + " ORDER BY " + by.toString();
		}

		sql_final = getNamedPageSql(sql, model, pageInfo);
		if (null != pageInfo && pageInfo.getTotalCount() < 1) {
			return new ArrayList<>(0);
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		ret = namedParameterJdbcTemplate
				.query(sql_final, new JbaBeanProperty(model), new BeanPropertyRowMapper<>(clazz));
		infoSql(sql_final, model, start);
		return ret;
	}

	/**
	 * 查询数据库--混合对象列表
	 *
	 * @param sql      sql语句
	 * @param params   参数对应的对象 OR map
	 * @param clazz    返回的类型
	 * @param pageInfo 分页信息
	 * @return 列表
	 */
	public <T> List<T> queryMixModelList(final String sql, Object params, Class<T> clazz, PageInfo pageInfo) {
		List<T> ret;
		String sql_final = getNamedPageSql(sql, params, pageInfo);
		if (null != pageInfo && pageInfo.getTotalCount() < 1) {
			return new ArrayList<>(0);
		}
		long start = System.currentTimeMillis();// 记录开始时间戳
		if (params != null) {
			if (params instanceof Map) {
				ret = namedParameterJdbcTemplate.query(sql_final, new JbaMapSqlSource((Map<String, ?>) params),
						new BeanPropertyRowMapper<>(clazz));
			} else {
				ret = namedParameterJdbcTemplate
						.query(sql_final, new JbaBeanProperty(params), new BeanPropertyRowMapper<>(clazz));
			}
		} else {
			ret = namedParameterJdbcTemplate.query(sql_final, new BeanPropertyRowMapper<>(clazz));
		}
		infoSql(sql_final, params, start);
		return ret;
	}

	// ==============================记录SQL执行时间======================

	/**
	 * 记录SQL语句、执行时间、参数
	 *
	 * @param sql    sql语句
	 * @param params sql中的参数
	 * @param start  开始执行时间戳（到毫秒）
	 */
	private void infoSql(final String sql, Object params, final long start) {
		try {
			long time = System.currentTimeMillis() - start;// 执行时间毫秒(ms)
			String log = String.format("【%6s】\t%s\r\n【%6s】\t%s\r\n【%6s】\t%,d(ms)", "sql", sql, "params",
					JsonUtil.toNoNullJsonStr(params, false), "time", time);
			if (useSlf4jLog) {
				LOGGER.debug("\r\n" + log);
			} else {
				LogFileUtil.info(jbaLogName, log);
			}
		} catch (Exception e) {
			if (useSlf4jLog) {
				LOGGER.error("出现异常：", e);
			} else {
				LogFileUtil.exception(jbaLogName, e);
			}
		}
	}

	/**
	 * 获取分页sql
	 *
	 * @param sql      原始sql
	 * @param params   参数对象
	 * @param pageInfo 分页信息
	 * @param <T>      泛型
	 * @return 返回带分页的sql
	 */
	private <T> String getNamedPageSql(final String sql, T params, PageInfo pageInfo) {
		if (null != pageInfo) {
			// 先查询总数
			Integer totalCount = queryCount(sql, params);
			if (totalCount < 1) { // 总数=0
				pageInfo.setTotalCount(totalCount);
				return sql;
			}
			// 计算总页数
			pageInfo.setTotalCount(totalCount);
			return SQLUtils.getPageSql(sql, pageInfo);
		}
		return sql;
	}

}
