package io.github.lucklike.httpclient.dbclient;

import com.luckyframework.reflect.MethodUtils;
import io.github.lucklike.httpclient.config.LoggerConfiguration;
import io.github.lucklike.httpclient.dbclient.executor.SFunction;

import java.lang.reflect.Method;

/**
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/23 11:59
 */
public class Msin {

    // com.baomidou.mybatisplus.core.toolkit.LambdaUtils
    public static void main(String[] args) throws NoSuchMethodException {
        SFunction<LoggerConfiguration, Object> sFunction = LoggerConfiguration::getMaskers;

        Method writeReplace = sFunction.getClass().getDeclaredMethod("writeReplace");
        Object invoke = MethodUtils.invoke(sFunction, writeReplace);
        System.out.println(invoke);
    }
}
