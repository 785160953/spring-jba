# spring-jba
`JavaBeanAccess`java对象访问，基于JDK1.7+
```目前仅支持MySql数据库  ```:grimacing:

# maven依赖
```
<dependency>
    <groupId>xin.xihc</groupId>
    <artifactId>spring-jba</artifactId>
    <version>1.0.5</version>
</dependency>
```

# 注解解释 #
1. `@EnableJBA` 开启spring-jba创建表功能
2. `@Table` 该注解是声明对象为表对象
3. `@Column` 声明字段属性包含如下：
```
1. value           列名-暂时无效
2. defaultValue    默认值
3. notNull         是否允许为空
4. primary         是否是主键
5. policy          主键生成策略
6. length          长度限制，小于1代表不限制
7. remark          备注
8. precision       精度
```
----------
# 类型转换对应表
| `Java`类型 | `Mysql`类型 | `Oracle`类型 |
|---| --- | -- |
|`String`             | `varchar`/length>2000的为`text` | `-` |
|`byte`/`Byte`        | `tinyint` | `-` |
|`short`/`Short`      | `smallint` | `-` |
|`int`/`Integer`      | `int` | `-` |
|`long`/`Long`        | `bigint` | `-` |
|`double`/`Double`    | `double` | `-` |
|`float`/`Float`      | `double` | `-` |
|`BigDecimal`         | `decimal` | `-` |
|`Date`               | `datetime` | `-` |
|`其他`               | `varchar(255)` | `-` |

# 使用教程
本项目为简易ORM，是基于spring-JdbcTemplate实现，故不支持其他复杂操作，所以目前表结构对象不支持自定义名称，即`对象名为表名、对象字段名为表结构中的列名。`请知悉。。

## 数据源连接池配置-配置文件中，数据库类型根据spring.datasource.url的值判断（`必须要配置哦`）
```
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/test
spring.datasource.username=root
spring.datasource.password=*****
```

## 新增初始化数据接口`InitTableDataIntf<T>`
需要表对象类实现该接口并返回初始化对象即可在表创建时初始化数据
```
@Override
public User[] initModel() {
    User[] data = new User[1];
    data[0] = new User();
    data[0].age = 18;
    return data;
}
```

## 表对象的`增删改查`操作
实际使用：
直接添加
```
@Autowired
JbaTemplate jbaTemplate;
```
即可使用JbaTemplate的方法。。具体方法解析如下：

`1、public boolean insertModel(Object model);` 插入对象INSERT INTO tblName (id,name) VALUES (:id,:name);
 
`2、public boolean updateModel(Object model, String... fieldNames) throws RuntimeException;`  更新对象

`3、public boolean deleteModel(Object model) throws RuntimeException;` 删除对象

`4、public <T> T queryColumn(String sql, Object model, Class<T> clazz);` 只能查询某列的值

`5、public int queryCount(String sql, Object model);` 查询数量

`6、public int queryCount(Object model);` 查询某个对象的数量

`7、public <T> T queryModelOne(Object model, Class<T> clazz, String... orderBy);` 查询单个对象

`8、public <T> T queryMixModelOne(String sql, Object model, Class<T> clazz);` 查询单个混合(自定义)对象

`9、public <T> List<T> queryModelList(Object model, Class<T> clazz, PageInfo pageInfo, String... orderBy);` 查询对象的列表
 
`10、public <T> List<T> queryMixModelList(String sql, Object model, Class<T> clazz, PageInfo pageInfo);` 查询混合对象的列表

# 联系&交流
QQ群号：340654726
<a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=161c33ee05b20185424556f09f488ddefb55ef0599c3695c3d59d64f876d4ccd"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="Spring-Jba交流群" title="Spring-Jba交流群"></a>

`更新时间：2018-02-07`
