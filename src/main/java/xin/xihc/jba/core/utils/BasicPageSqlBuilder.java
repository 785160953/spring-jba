package xin.xihc.jba.core.utils;

import xin.xihc.jba.core.PageInfo;

/**
 * 基本的分页sql生成、拼接
 *
 * @author Leo.Xi
 * @date 2019/9/27
 * @since 1.8
 **/
public class BasicPageSqlBuilder {

    /**
     * 构造分页SQL
     *
     * @param sql          原始查询sql语句
     * @param pageInfo     分页信息
     * @param databaseName 数据库产品名
     *                     "Apache Derby",
     *                     "DB2",
     *                     "MySQL",
     *                     "Microsoft SQL Server",
     *                     "Oracle",
     *                     "PostgreSQL"
     * @return 分页sql
     * @author Leo.Xi
     * @date 2019/9/27
     * @since 0.0.1
     */
    protected String build(final String sql, PageInfo pageInfo, String databaseName) {
        if ("MySQL".equals(databaseName)) {
            // 计算起始索引
            // 使用limit 0, 10分页 -- 索引从0开始
            return sql + " LIMIT " + pageInfo.getStart() + "," + pageInfo.getPageSize();
        }
        return toBuild(sql, pageInfo, databaseName);
    }

    /**
     * 抽象方法，可自定义基础分页
     *
     * @param sql          原始查询sql语句
     * @param pageInfo     分页信息
     * @param databaseName 数据库产品名
     *                     "Apache Derby",
     *                     "DB2",
     *                     "MySQL",
     *                     "Microsoft SQL Server",
     *                     "Oracle",
     *                     "PostgreSQL"
     * @return 分页sql
     * @author Leo.Xi
     * @date 2019/9/27
     * @since 0.0.1
     */
    public String toBuild(final String sql, PageInfo pageInfo, String databaseName) {
        return sql;
    }

}