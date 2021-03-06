# spring-jba
`JavaBeanAccess`java对象访问，基于JDK1.8+
```目前仅支持MySql  ```:grimacing:


# maven依赖
最新版本
<a target="_blank" href="https://search.maven.org/search?q=g:%22xin.xihc%22%20AND%20a:%22spring-jba%22">
    <img src="https://img.shields.io/maven-central/v/xin.xihc/spring-jba.svg?label=Maven%20Central">
</a>

`注意：从1.7.0开始与之前版本不兼容` `注意升级`

```
<dependency>
    <groupId>xin.xihc</groupId>
    <artifactId>spring-jba</artifactId>
    <version>${jba.version}</version>
</dependency>
```

## SpringJbaDemo演示Demo
https://gitee.com/leo_xi/SpringJbaDemo

# 注解解释 #
1. `@EnableJba` 开启spring-jba创建表功能
2. `@Table` 该注解是声明对象为表对象,属性包含如下：(不支持继承关系)
    ```
    1. value      1.2.0以后支持自定义表名
    2. remark     表的备注
    3. order      (1.5.0+新增)顺序，默认9999
    4. ignore     (1.5.0+新增)是否忽略表，默认false
    ```
3. `@Column` 声明字段属性包含如下：(支持继承关系,可以使用父类的属性)
    ```
    1. value           列名,1.7.6+支持自定义列名
    2. defaultValue    默认值
    3. notNull         是否允许为空(默认允许)
    4. primary         是否是主键(默认否)
    5. policy          主键生成策略(默认无)
    6. length          长度限制
    7. remark          备注
    8. precision       精度(小于length)(默认4)
    9. charset         (1.5.5+新增)设置表字符编码，默认utf8
    10. order          (1.7.8+新增)对应表的列的顺序，默认0
    ```
4. `@Index` (1.5.7+新增)该注解是声明(单列索引)表字段索引的,属性包含如下：(支持继承关系,可以使用父类的属性)
    ```
    1. value      索引名，用于分组,默认为idx_xxxx
    2. type       索引类型,默认为普通索引（Normal，Unique，FullText）
    3. remark     (1.5.8+新增)索引的备注
    ```
5. `@OnUpdateCurrentTimestamp` (1.6.1+新增)该注解是声明表字段是否自动更新时间戳(java.util.Date/java.sql.Timestamp)(支持继承关系,可以使用父类的属性)

6. `@Alias` 属性对应的列名(1.7.6+新增)主要用于查询时，列名与属性名不一致的问题。

7. `@GroupIndex` 分组索引、复合索引（1.7.8+）用于创建复合索引、目前没有继承关系。
----------
# 类型转换对应表
| `Java`类型(建议使用包装类) | `Mysql`类型                 | 
|---| --- |
|`String`            | (1-64)`char`(65-19999)`varchar`/length>20000的为`text`| 
|`Byte`              | `tinyint(3)`                      | 
|`Short`             | `smallint(5)`                     | 
|`Integer`           | `int(10)`                          | 
|`Long`              | `bigint(19)`                       | 
|`Double`            | `double(length,precision)`                       | 
|`Float`             | `double(length,precision)`                       | 
|`BigDecimal`        | `decimal(length,precision)`                      | 
|`Boolean`           | `tinyint(3)`                      |
|`java.util.Date`    | `datetime`                     | 
|`java.sql.Timestamp`| `Timestamp`                    | 
|`java.sql.Time`     | `Time`                         | 
|`枚举类型`            | `varchar(length)`              |
|`其他`               | `varchar(length)`                 |

# 使用教程
本项目为简易ORM，是基于spring-JdbcTemplate实现，支持事务管理，复杂操作则需要自己写SQL。目前表结构对象不支持自定义列名称，即`对象字段名为表结构中的列名。`请知悉。。

## 数据源连接池配置
```
#数据源配置
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/test
spring.datasource.username=root
spring.datasource.password=*****
#使用阿里巴巴druid数据库连接池
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource

#jba相关配置
#jba表结构更新模式NONE-不操作，CREATE-只创建表，UPDATE-只更新表结构，ALL-所有，CREATE_DROP-启动时创建关闭时删除
spring.jba.mode=NONE/ALL/CREATE/UPDATE/CREATE_DROP

#打印sql执行记录,日志采用上slf4j记录
logging.level.xin.xihc.jba.core.JbaTemplate=debug
```

## 初始化数据接口`InitDataInterface`
只需要实现如下方法：
```
void doInit(JbaTemplate jbaTemplate);
```

## 表对象的`增删改查`操作
实际使用：
首先主类入口添加注解`@EnableJba`<br />
再需要使用的地方引入：
```
@Autowired
private JbaTemplate jbaTemplate;
```

## 分页对象PageInfo中增加`needTotalCount`是否需要查询总数量

即可使用JbaTemplate的方法。。具体方法解析如下：
表对象(需要获取表名)-`model`，参数对象-`params`

`1、public boolean insertModel(Object model);` 插入单个对象INSERT INTO tblName (id,name) VALUES (:id,:name);

`2、public void insertModels(Object... models);` 批量插入多个相同表对象（建议一次性不超过50个）
 
`3、public boolean updateModel(Object model, String... fieldNames) throws RuntimeException;`  更新对象

`4、public boolean deleteModel(Object model) throws RuntimeException;` 删除对象

`5、public <T> T queryColumn(String sql, Object params, Class<T> clazz);` 只能查询某列的值,支持Map的params

`6、public int queryCount(String sql, Object params);` 查询数量,支持Map的params

`7、public int queryCount(Object model);` 查询某个对象的数量

`8、public <T> T queryModelOne(Object model, Class<T> clazz, String... orderBy);` 查询单个对象

`9、public <T> T queryMixModelOne(String sql, Object params, Class<T> clazz);` 查询单个混合(自定义)对象,支持Map的params

`10、public <T> List<T> queryModelList(Object model, Class<T> clazz, PageInfo pageInfo, String... orderBy);` 查询对象的列表
 
`11、public <T> List<T> queryMixModelList(String sql, Object params, Class<T> clazz, PageInfo pageInfo);` 查询混合对象的列表,支持Map的params

`12、public boolean executeSQL(final String sql);` 执行某个sql语句

`13、public boolean executeSQL(final String sql, Object params);` 执行某个带参数的sql语句,支持Map的params

`14、public NamedParameterJdbcTemplate getJdbcOperations();` 可以使用更多内部原生方法（例如：BOLB字段的处理、储存过程的调用）

`15、public void batchUpdate(final String sql, Map<String, ?>... params);` 批量执行sql，插入INSERT、UPDATE都可以

`16、public void batchUpdate(final String sql, Object[] models);` 批量执行sql，插入INSERT、UPDATE都可以

# 联系&交流
QQ群号：340654726
<a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=161c33ee05b20185424556f09f488ddefb55ef0599c3695c3d59d64f876d4ccd"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="Spring-Jba交流群" title="Spring-Jba交流群"></a>

`更新时间：2019-06-27`
