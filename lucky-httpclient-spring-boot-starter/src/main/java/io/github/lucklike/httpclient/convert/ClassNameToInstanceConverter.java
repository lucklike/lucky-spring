package io.github.lucklike.httpclient.convert;

import com.luckyframework.reflect.ClassUtils;
import org.springframework.core.convert.converter.Converter;

@SuppressWarnings("unchecked")
public class ClassNameToInstanceConverter<T> implements Converter<String, T> {

    @Override
    public T convert(String source) {
        return (T) ClassUtils.newObject(ClassUtils.getClass(source));
    }
}