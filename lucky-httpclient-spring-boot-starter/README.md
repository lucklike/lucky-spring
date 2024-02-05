# 🐰 与`SpringBoot`整合开发

## ⚙️ 安装

---

🪶 Maven  
在项目的`pom.xml`的`dependencies`中加入以下内容:
```xml
    <dependency>
        <groupId>io.github.lucklike</groupId>
        <artifactId>lucky-httpclient-spring-boot-starter</artifactId>
        <version>1.1.0</version>
    </dependency>
```

🐘 Gradle

```groovy
    implementation group: 'io.github.lucklike', name: 'lucky-httpclient-spring-boot-starter', version: '1.1.0'
```

## 🏄‍♂️  开始使用

---
### 一、在SpingBoot的启动类上添加`@EnableLuckyHttpClient`注解来开启`lucky-httpclient`的注解开发功能

```java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
    添加@EnableLuckyHttpClient注解来开启lucky-httpclient的注解开发的功能    
 */
@EnableLuckyHttpClient
@SpringBootApplication
public class SpringbootTestApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringbootTestApplication.class, args);
    }

}


//----------------------------------------------------------------
//               @EnableLuckyHttpClient注解的介绍
//----------------------------------------------------------------

/**
 * lucky-httpclient自动导入注解
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 01:45
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({LuckyHttpAutoConfiguration.class, LuckyHttpClientImportBeanDefinitionRegistrar.class})
public @interface EnableLuckyHttpClient {

    /**
     * 配置需要扫描的包，不做任何配置时默认扫描classpath下所有包中的class
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 配置需要扫描的包，不做任何配置时默认扫描classpath下所有包中的class
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 配置一些基本类，使用这些类的包名作为扫描包进行扫描
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Http接口代理对象是依赖{@link HttpClientProxyObjectFactory}来生成的，这里需要配置这个Bean的名称
     */
    String proxyFactoryName() default PROXY_FACTORY_BEAN_NAME;

    /**
     * 是否启用Cglib代码方法，默认使用Jdk的代码方式
     */
    boolean useCglibProxy() default false;
}


```

###  二、创建HTTP接口，并使用`@HttpClient`注解进行标注

(lucky底层会识别`@HttpClient`注解，并会为所有被注解的接口生成代理对象之后注入到Spring容器中，类似`Mybatis`的`Mapper`接口)

```java

/**
 * 高德开放平台API
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 05:32
 */
@PrintLog
@HttpClient("#{gaoDeApi}")
public interface GaoDeApi {

    /**
     * 高德开放平台API -- 天气查询
     * 
     * @param city 城市名称
     * @return 该城市的天气情况
     */
    @ResultSelect(key="@resp.lives", defaultValue = "#{new ArrayList()}")
    @Get("/{version}/weather/weatherInfo")
    Object queryWeather(String city);

    /**
     * 高德开放平台API -- 骑行路线查询
     * 
     * @param origin        出发地的高德坐标
     * @param destination   目的地的高德坐标
     * @return  出发地到目的地的骑行路线
     */
    @ResultSelect("@resp.data.paths")
    @Get("/v4/direction/bicycling")
    Object bicycling(String origin, String destination);

    /**
     * 高德开放平台API -- 将地址转化为高德坐标
     * 
     * @param address 地址
     * @return 该地址对应的高德坐标
     */
    @ResultSelect("@resp.geocodes[0].location")
    @Get("/{version}/geocode/geo")
    Future<String> getGeocode(String address);
    
}

```

### 三、在其他Spring组件中导入HTTP组件进行使用

```java
package com.springboot.testdemo.springboottest.controller;

import com.luckyframework.async.EnhanceFuture;
import com.luckyframework.async.EnhanceFutureFactory;
import com.luckyframework.common.StopWatch;
import com.springboot.testdemo.springboottest.api.GaoDeApi;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 05:46
 */
@AllArgsConstructor
@RestController
public class LuckyHttpClientController {
  private final EnhanceFutureFactory enhanceFutureFactory;
  private final GaoDeApi gaoDeApi;

  @GetMapping("weather")
  public Object call(String city) {
    StopWatch sw = new StopWatch();
    sw.start("proxy");
    Object result = gaoDeApi.queryWeather(city);
    sw.stopWatch();
    System.out.println(sw.prettyPrintMillis());
    return result;
  }

  @GetMapping("bicycling")
  public Object bicycling(String origin, String destination){
    EnhanceFuture<String> enhanceFuture = enhanceFutureFactory.create();
    enhanceFuture.addFuture(gaoDeApi.getGeocode(origin));
    enhanceFuture.addFuture( gaoDeApi.getGeocode(destination));
    return gaoDeApi.bicycling(enhanceFuture.getTaskResult(0), enhanceFuture.getTaskResult(1));
  }


}


```

## 🎱 SpEL功能增强
与SpringBoot整合后，原先所有支持SpEL表达式的地方现在均可以使用`${}`表达式直接获取到Spring环境变量中的配置值。  
例如，application.yaml中有如下配置：
```yaml
gaoDe: 
  url: https://restapi.amap.com
  weatherApi: /v3/weather/weatherInfo
```

那么可以使用`${}`直接将此配置引入：
```java
/**
 * 高德开放平台API
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 05:32
 */
@PrintLog
@HttpClient("${gaoDe.url}")
public interface GaoDeApi {

    /**
     * 高德开放平台API -- 天气查询
     * 
     * @param city 城市名称
     * @return 该城市的天气情况
     */
    @ResultSelect(key="@resp.lives", defaultValue = "#{new ArrayList()}")
    @Get("${gaoDe.weatherApi}")
    Object queryWeather(String city);
}
```

## 🪛 常用配置
- `spring.lucky.http-client.connection-timeout`  
  设置`连接超时时间`


- `spring.lucky.http-client.read-timeout`  
  设置`读超时时间`


- `spring.lucky.http-client.write-timeout`  
  设置`写超时时间`


- `spring.lucky.http-client.print-log-packages`  
  在如下包中的HTTP接口将会打印日志
  ```yaml
  spring:
    lucky:
      http-client:
        print-log-packages:
          - com.springboot.testdemo.springboottest.api.GaoDeApi
          - com.springboot.testdemo.springboottest.api2
          - com.springboot.testdemo.springboottest.api3
  ```

- `spring.lucky.http-client.enable-request-log`  
  开启请求日志


- `spring.lucky.http-client.enable-response-log`  
  开启响应日志


- `spring.lucky.http-client.allow-print-log-body-mime-types`  
  响应日志开启时，设置mime-types，只有响应的mime-types为配置值时才打印具体的响应体内容
  ```yaml
  spring:
    lucky:
      http-client:
        allow-print-log-body-mime-types:
          - application/json
          - application/xml
  ```

- `spring.lucky.http.client.allow-print-log-body-max-length`  
  响应日志开启时，设置最大响应体长度，超过该长度则不会打印响应体内容,值小于等于0时表示没有限制
  ```yaml
  spring:
    lucky:
      http-client:
        allow-print-log-body-max-length: 14500
  ```

- `spring.lucky.http-client.header-params`  
  设置公共`请求头`参数，支持给指定接口配置特有的参数
    ```yaml
    spring:
      lucky:
        http-client:
          #公共请求头参数
          header-params:
            # 使用"[全类名]"的写法可以为接口配置特有的参数
            "[com.springboot.testdemo.springboottest.api.GaoDeApi]":
                gaoDe-header: 高德API特有的参数
            # 所有HTTP接口公用的请求头参数
            Cookie:
              - c1=12345666
              - c2=token-uuidm
    ```

- `spring.lucky.http-client.query-params`  
  设置公共`URL`参数，支持给指定接口配置特有的参数


- `spring.lucky.http-client.path-params`  
  设置`公共URL占位符'{}'`参数，支持给指定接口配置特有的参数


- `spring.lucky.http-client.form-params`  
  设置公共`表单`参数，支持给指定接口配置特有的参数


- `spring.lucky.http-client.resource-param`  
  设置公共`文件资源`参数，支持给指定接口配置特有的参数


- `spring.lucky.http-client.thread-pool-param.core-pool-size`  
  设置`执行异步调用`的线程池参数：`核心线程数`


- `spring.lucky.http-client.thread-pool-param.maximum-pool-size`  
  设置`执行异步调用`的线程池参数：`最大线程数`


- `spring.lucky.http-client.thread-pool-param.blocking-queue-size`  
  设置`执行异步调用`的线程池参数：`阻塞队列的长度`


- `spring.lucky.http-client.thread-pool-param.keep-alive-time`  
  设置`执行异步调用`的线程池参数：`保活时间，空闲等待时间`


- `spring.lucky.http-client.thread-pool-param.name-format`  
  设置`执行异步调用`的线程池参数：`线程名格式`


- `spring.lucky.http-client.thread-pool-param.blocking-queue-factory`  
  设置`执行异步调用`的线程池参数：`设置阻塞队列的工厂的全类名`


- `spring.lucky.http-client.thread-pool-param.rejected-execution-handler-factory`  
  设置`执行异步调用`的线程池参数：`设置拒绝策略的工厂的全类名`


- `spring.lucky.http-client.expression-params`  
  配置SpEL表达式参数，这里配置的参数可以在`lucky-httpclient`中支持`SpEL`表达式的注解中直接使用。
  ```yaml
    spring:
      lucky:
        http-client:
          #SpEL表达式参数，例如：在进行如下配置后使用@HttpClient("#{gaoDeApi}")，便可以直接获取到值 'https://restapi.amap.com'
          expression-params:
            userModel: http://localhost:8080/users
            gaoDeApi: https://restapi.amap.com
            mirrors: https://mirrors.sohu.com

  ```
- `spring.lucky.http-client.spring-el-package-imports`  
  向`SpEL运行时环境`中`导包`，导入后`在SpEL表达式`中使用包中的类时便可以不用使用全类名，直接使用类名即可.
    ```yaml
    spring:
      lucky:
        http-client:
          #向SpEL运行时环境导入的包，
          #导入前创建ArrayList实例(#{new java.util.ArrayList()})
          #导入后创建ArrayList实例(#{new ArrayList()})
          spring-el-package-imports:
            - java.util
   ```

- `spring.lucky.http-client.http-executor-factory`  
  设置HTTP执行器工厂的类的全类名
  ```yaml
    spring:
      lucky:
        http-client:
          # 设置SpEL运行时环境工厂的类的全类名
          http-executor-factory: io.github.lucklike.httpclient.config.impl.OkHttpExecutorFactory
  ```

- `spring.lucky.http-client.http-executor`  
  设置HTTP执行器
  ```yaml
    spring:
      lucky:
        http-client:
          # HTTP执行器，jdk、okhttp、http_client
          http-executor: okhttp
  ```

- `spring.lucky.http-client.object-creator-factory`  
  设置对象创建器工厂的类的全类名
  ```yaml
    spring:
      lucky:
        http-client:
          # 设置对象创建器工厂的类的全类名
          object-creator-factory: io.github.lucklike.httpclient.config.impl.BeanObjectCreatorFactory
  ```

- `spring.lucky.http-client.spring-el-runtime-factory`  
  设置SpEL运行时环境工厂的类的全类名
  ```yaml
    spring:
      lucky:
        http-client:
          # 设置SpEL运行时环境工厂的类的全类名
          spring-el-runtime-factory: io.github.lucklike.httpclient.config.impl.BeanSpELRuntimeFactoryFactory
  ```

- `spring.lucky.http-client.http-exception-handle-factory`  
  设置异常处理器工厂的类的全类名
  ```yaml
    spring:
      lucky:
        http-client:
          # 设置异常处理器工厂的类的全类名
          http-exception-handle-factory: io.github.lucklike.httpclient.config.impl.DefaultHttpExceptionHandleFactory
  ```


- `spring.lucky.http-client.request-interceptors`  
  设置请求拦截器
  ```yaml
    spring:
      lucky:
        http-client:
          # 请求拦截器实现类集合
          request-interceptors:
            - com.luckyframework.httpclient.proxy.impl.interceptor.PrintLogInterceptor

  ```

- `spring.lucky.http-client.response-interceptors`  
  设置响应拦截器
  ```yaml
    spring:
      lucky:
        http-client:
          # 响应拦截器实现类集合
          response-interceptors:
            - com.luckyframework.httpclient.proxy.impl.interceptor.PrintLogInterceptor
  ```
