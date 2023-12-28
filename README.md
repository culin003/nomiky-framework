# Nomiky Framework
A simple framework to build web application, Just write xml file!

### Why Nomiky Framework?
* Simple and fast to build web application
* No need to write Controller, Service, Dao, just write xml file, and then it works
* Base on spring framework, almost every java developer can be used

### Guides

The following guides illustrate how to use some features concretely:

### Install
Want to Integration Nomiky Framework?

Just use springboot `@Configuration` to define some beans:
```java
@Configuration
public class AutoConfiguration {

    @Bean
    public static DataSource dataSource(Environment environment){
        DataSourceProperties properties = Binder.get(environment).bind("spring.datasource", DataSourceProperties.class).get();
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public static JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public static FrameworkBeanProcessor frameworkBeanProcessor(JdbcTemplate jdbcTemplate) {
        return new FrameworkBeanProcessor(jdbcTemplate);
    }

}
```

Nomiky Framework will scan classpath dir, find `tableDefine.xml` and `*_controller.xml` and load them to build
`Controller` and `DaoExecutor`.

### Simple CURD service
In `tableDefine.xml`, you can define the database tables which your web application want to use.

This `tableDefine.xml` define a table: `statistic`, and its primary key will generator by SNOWFLAKE.
The framework will create a DaoExecutor spring bean named by `ocr.statistics`, and implement some default CURD method:
1. insert
2. deleteById
3. updateById
4. select
5. selectOne
6. exist
7. count
8. selectPage

The parameter of default method is `Map<String, Object>`, it will receive parameters from controller request.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<TableExecutors>
    <table schema="ocr" name="statistics" keyGenerator="SNOWFLAKE"/>
</TableExecutors>
```

In `*_controller.xml`, you can define `Controller` attributes.

The framework will use it to register request mapping, and the mapping handler will call DaoExecutor which
specify by `ref` attribute.

This `statistic_controller.xml` define a Controller with path `statistics`, method `GET`, and it will reference
DaoExecutor bean which name is `ocr.statistics`, and then call the method `select`. The method parameter will
get from `request.getParameter("appKey")`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<controller  path="statistics">
    <controller method="GET">
        <executor ref="ocr.statistics.select" params="param.appKey"/>
    </controller>
</controller>
```

### How to use transaction?
Want to use transaction in Framework? Just add an attribute in `<Controller>` tag.

This `statistic_controller.xml` define a controller use transaction to execute 3 executors.
```xml
<controller  path="statistics">
    <controller method="GET" useTransaction="true">
        <executor ref="ocr.statistics.select" params="param.appKey"/>
        <executor ref="ocr.statistics.insert" params="param.appKey,param.name,param.sex"/>
        <executor ref="ocr.statistics.deleteById" params="param.id"/>
    </controller>
</controller>
```

### Define a complex SQL statement?
Nomiky Framework can define complex sql statement use Groovy or JavaScript engine.

You can write Groovy script or JavaScript code to define a complex sql.

Like this:

```xml
<controller method="GET">
    <executor type="sql" engine="groovy">
        sqlResult += "select * from user"
        if (param.id){
            sqlResult += " where id = ?"
            sqlParams[sqlParams.length] = param.id
        }
    </executor>
</controller>
```
```xml
<controller method="GET">
    <executor type="sql" engine="JavaScript">
        sqlResult += "select * from user"
        if (param.id){
            sqlResult += " where id = ?"
            sqlParams[sqlParams.length] = param.id
        }
    </executor>
</controller>
```

When you code Groovy or JavaScript to define sql statement, you can get build-in parameters:
1. param: all parameters from request
2. bodyJson: the json object from requestBody
3. bodyString: the string value from requestBody
4. parent: the value from parent executor
5. header: all parameters from request headers

All these build-in parameters are used in xml tag `<executor params="">`.

Otherwise, you can assign value to variable `sqlResult` and `sqlParams`, framework will get it and
execute by jdbcTemplate.