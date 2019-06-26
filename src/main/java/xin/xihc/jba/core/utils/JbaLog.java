package xin.xihc.jba.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.xihc.jba.core.JbaTemplate;
import xin.xihc.utils.json.JsonUtil;
import xin.xihc.utils.logfile.LogFileUtil;

/**
 * spring-jba日志记录器
 *
 * @Author Leo.Xi
 * @Date 2019/2/19 15:52
 * @Version 1.0
 **/
public class JbaLog {

    /**
     * 日志文件夹名
     */
    public static final String jbaLogName = "JbaExecSqls";
    /**
     * 是否使用slf4j记录日志,默认使用
     */
    public static boolean useSlf4jLog = true;
    /**
     * 记录日志
     */
    private static Logger LOGGER = LoggerFactory.getLogger(JbaTemplate.class);

    private JbaLog() {

    }

    protected static void info(final String info) {
        if (useSlf4jLog) {
            LOGGER.debug("\r\n" + info);
        } else {
            LogFileUtil.info(jbaLogName, info);
        }
    }

    /**
     * 记录SQL语句、执行时间、参数
     *
     * @param sql    sql语句
     * @param params sql中的参数
     * @param start  开始执行时间戳（到毫秒）
     */
    public static void infoSql(final String sql, Object params, final long start) {
        long time = System.currentTimeMillis() - start;// 执行时间毫秒(ms)
        String log = String.format("【%6s】\t%s\r\n【%6s】\t%s\r\n【%6s】\t%,d(ms)", "sql", sql, "params",
                JsonUtil.toNoNullJsonStr(params, false), "time", time);
        if (useSlf4jLog) {
            LOGGER.debug("\r\n" + log);
        } else {
            LogFileUtil.info(jbaLogName, log);
        }
    }

    /**
     * 异常
     *
     * @param e
     */
    public static void error(Throwable e) {
        if (useSlf4jLog) {
            LOGGER.error("异常:", e);
        } else {
            LogFileUtil.exception(jbaLogName, e);
        }
    }

    /**
     * 异常
     *
     * @param error
     */
    public static void error(String error) {
        if (useSlf4jLog) {
            LOGGER.error(error);
        } else {
            LogFileUtil.error(jbaLogName, error);
        }
    }

}
