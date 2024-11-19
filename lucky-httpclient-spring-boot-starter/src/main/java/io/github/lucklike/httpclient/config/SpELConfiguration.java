package io.github.lucklike.httpclient.config;

import com.luckyframework.common.ConfigurationMap;
import com.luckyframework.httpclient.proxy.spel.ClassStaticElement;
import com.luckyframework.httpclient.proxy.spel.StaticMethodEntry;
import io.github.lucklike.httpclient.annotation.SpELFunction;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.luckyframework.httpclient.proxy.spel.SpELConvert.DEFAULT_NEST_EXPRESSION_PREFIX;
import static com.luckyframework.httpclient.proxy.spel.SpELConvert.DEFAULT_NEST_EXPRESSION_SUFFIX;

/**
 * SpEL相关的配置
 */
public class SpELConfiguration {

    /**
     * SpEL运行时环境工厂
     */
    private SpELRuntimeFactory runtimeFactory;

    /**
     * 嵌套解析表达式前缀，默认值：``
     */
    private String nestExpressionPrefix = DEFAULT_NEST_EXPRESSION_PREFIX;

    /**
     * 嵌套解析表达式后缀，默认值：``
     */
    private String nestExpressionSuffix = DEFAULT_NEST_EXPRESSION_SUFFIX;

    /**
     * 向SpEL运行时环境导入的包
     */
    private List<String> importPackages;

    /**
     * SpEL表达式Root对象参数（通过name引入）
     */
    private ConfigurationMap rootVariables = new ConfigurationMap();

    /**
     * SpEL表达式普通对象参数（通过#name引入）
     */
    private ConfigurationMap variables = new ConfigurationMap();

    /**
     * SpEL表达式函数工具类自动扫描的包
     */
    private Set<String> functionPackages = new HashSet<>();

    /**
     * SpEL表达式函数工具类自动扫描时，会通过该注解来判定是否自动注册
     */
    private Class<? extends Annotation> functionAnnotation = SpELFunction.class;

    /**
     * SpEL表达式函数，此处导入的函数可以在支持SpEL表达式的地方通过变量的方式调用<br/>
     * 例如：#{#toInt('9527')}、#{#format('hello {}', 'Jack')}
     */
    private StaticMethodEntry[] functions;

    /**
     * SpEL表达式函数工具类, 此处导入的函数可以在支持SpEL表达式的地方通过变量的方式调用<br/>
     * 例如：#{#toInt('9527')}、#{#format('hello {}', 'Jack')}
     */
    private ClassStaticElement[] functionClasses;


    /**
     * 设置{@link SpELRuntimeFactory SpEL运行时环境工厂}
     *
     * @param runtimeFactory SpEL运行时环境工厂
     */
    public void setRuntimeFactory(SpELRuntimeFactory runtimeFactory) {
        this.runtimeFactory = runtimeFactory;
    }

    /**
     * 设置嵌套解析表达式前缀，默认值：``
     *
     * @param nestExpressionPrefix 嵌套解析表达式前缀
     */
    public void setNestExpressionPrefix(String nestExpressionPrefix) {
        this.nestExpressionPrefix = nestExpressionPrefix;
    }

    /**
     * 设置嵌套解析表达式后缀，默认值：``
     *
     * @param nestExpressionSuffix 嵌套解析表达式后缀
     */
    public void setNestExpressionSuffix(String nestExpressionSuffix) {
        this.nestExpressionSuffix = nestExpressionSuffix;
    }

    /**
     * 设置自定义SpEL表达式Root参数
     *
     * @param rootVariables 自定义Root参数
     */
    public void setRootVariables(ConfigurationMap rootVariables) {
        this.rootVariables = rootVariables;
    }

    /**
     * 设置自定义SpEL表达式普通参数
     *
     * @param variables 自定义普通参数
     */
    public void setVariables(ConfigurationMap variables) {
        this.variables = variables;
    }

    /**
     * 设置SpEL表达式函数工具类自动扫描的包
     *
     * @param functionPackages SpEL表达式函数工具类自动扫描的包
     */
    public void setFunctionPackages(Set<String> functionPackages) {
        this.functionPackages = functionPackages;
    }

    /**
     * 设置SpEL表达式函数工具类自动扫描时检测的标志注解
     *
     * @param functionAnnotation SpEL表达式函数工具类自动扫描时检测的标志注解类型
     */
    public void setFunctionAnnotation(Class<? extends Annotation> functionAnnotation) {
        this.functionAnnotation = functionAnnotation;
    }

    /**
     * 设置自定义SpEL表达式函数
     *
     * @param functions SpEL表达式函数
     */
    public void setFunctions(StaticMethodEntry[] functions) {
        this.functions = functions;
    }

    /**
     * 设置自定义SpEL表达式函数类
     *
     * @param functionClasses 自定义SpEL表达式函数类
     */
    public void setFunctionClasses(ClassStaticElement[] functionClasses) {
        this.functionClasses = functionClasses;
    }

    /**
     * 向SpEL运行时环境导入的包
     *
     * @param importPackages 向SpEL运行时环境导入的包
     */
    public void setImportPackages(List<String> importPackages) {
        this.importPackages = importPackages;
    }

    /**
     * 获取{@link SpELRuntimeFactory SpEL运行时环境工厂}
     *
     * @return SpEL运行时环境工厂
     */
    public SpELRuntimeFactory getRuntimeFactory() {
        return runtimeFactory;
    }

    /**
     * 获取嵌套解析表达式前缀，默认值：``
     *
     * @return 嵌套解析表达式前缀
     */
    public String getNestExpressionPrefix() {
        return nestExpressionPrefix;
    }

    /**
     * 获取嵌套解析表达式后缀，默认值：``
     *
     * @return 嵌套解析表达式后缀
     */
    public String getNestExpressionSuffix() {
        return nestExpressionSuffix;
    }

    /**
     * 获取自定义SpEL表达式Root参数
     *
     * @return 自定义SpEL表达式Root参数
     */
    public ConfigurationMap getRootVariables() {
        return rootVariables;
    }

    /**
     * 获取自定义SpEL表达式普通参数
     *
     * @return 自定义SpEL表达式普通参数
     */
    public ConfigurationMap getVariables() {
        return variables;
    }


    /**
     * 获取SpEL表达式函数工具类自动扫描的包
     *
     * @return SpEL表达式函数工具类自动扫描的包
     */
    public Set<String> getFunctionPackages() {
        return functionPackages;
    }

    /**
     * 获取SpEL表达式函数工具类自动扫描时检测的标志注解类型
     *
     * @return SpEL表达式函数工具类自动扫描时检测的标志注解类型
     */
    public Class<? extends Annotation> getFunctionAnnotation() {
        return functionAnnotation;
    }

    /**
     * 获取自定义SpEL表达式函数
     *
     * @return 自定义SpEL表达式函数
     */
    public StaticMethodEntry[] getFunctions() {
        return functions;
    }

    /**
     * 获取自定义SpEL表达式函数类
     *
     * @return 自定义SpEL表达式函数类
     */
    public ClassStaticElement[] getFunctionClasses() {
        return functionClasses;
    }

    /**
     * 向SpEL运行时环境导入的包
     *
     * @return 向SpEL运行时环境导入的包
     */
    public List<String> getImportPackages() {
        return importPackages;
    }

}
