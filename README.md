# spring-jba
`JavaBeanAccess`java对象访问


# 注解解释 #
1. `@EnableJBA` 开启spring-jba创建表功能
2. `@Table` 该注解是声明对象为表对象
3. `@Column` 声明字段属性包含如下：
```
1. value		列名-暂时无效
2. defaultValue        默认值
3. notNull		是否允许为空
4. primary		是否是主键
5. policy		主键生成策略
6. length		长度限制，小于1代表不限制
7. remark		备注
8. precision        精度
```
    

----------

