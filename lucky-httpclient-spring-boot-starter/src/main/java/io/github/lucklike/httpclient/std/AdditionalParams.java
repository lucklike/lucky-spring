package io.github.lucklike.httpclient.std;

import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.context.Context;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.serializable.SerializationTypeToken;
import org.springframework.core.ResolvableType;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 额外参数集
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/13 00:56
 */
public class AdditionalParams extends LinkedHashMap<String, Object> {

    /**
     * 将当前参数整体绑定到指定类型的结果，绑定过程支持 SpEL 计算
     *
     * @param context 上下文对象
     * @param clazz   结果类型
     * @param <T>     结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T spelBindTo(Context context, Class<T> clazz) {
        return CommonFunctions.spelConvert(context, clazz, this);
    }

    /**
     * 将当前参数整体绑定到指定类型的结果
     *
     * @param clazz 结果类型
     * @param <T>   结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T bindTo(Class<T> clazz) {
        return ConversionUtils.looseBind(clazz, this);
    }

    /**
     * 将当前参数整体绑定到指定类型的结果
     *
     * @param resolvableType 结果类型
     * @param <T>            结果类型泛型
     * @return 对应结果类型的值
     */
    @SuppressWarnings("unchecked")
    public <T> T bindTo(ResolvableType resolvableType) {
        return (T) ConversionUtils.looseBind(resolvableType, this);
    }

    /**
     * 将当前参数整体绑定到指定类型的结果
     *
     * @param typeToken 结果类型
     * @param <T>       结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T bindTo(SerializationTypeToken<T> typeToken) {
        return ConversionUtils.looseBind(typeToken, this);
    }

    /**
     * 将当前参数整体转化为指定类型的结果
     *
     * @param clazz 结果类型
     * @param <T>   结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T convertTo(Class<T> clazz) {
        return ConversionUtils.conversion(this, clazz);
    }

    /**
     * 将当前参数整体转化为指定类型的结果
     *
     * @param resolvableType 结果类型
     * @param <T>            结果类型泛型
     * @return 对应结果类型的值
     */
    @SuppressWarnings("unchecked")
    public <T> T convertTo(ResolvableType resolvableType) {
        return (T) ConversionUtils.conversion(this, resolvableType);
    }

    /**
     * 将当前参数整体转化为指定类型的结果
     *
     * @param typeToken 结果类型
     * @param <T>       结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T convertTo(SerializationTypeToken<T> typeToken) {
        return ConversionUtils.conversion(this, typeToken);
    }

    /**
     * 获取参数值，并将其绑定到指定的类型，绑定过程支持 SpEL 表达式计算
     *
     * @param context 上下文对象
     * @param key     参数名
     * @param clazz   结果类型
     * @param <T>     结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T spelBindValue(Context context, String key, Class<T> clazz) {
        return CommonFunctions.spelConvert(context, clazz, get(key));
    }

    /**
     * 获取参数值，并将其绑定到指定的类型
     *
     * @param key   参数名
     * @param clazz 结果类型
     * @param <T>   结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T bindValue(String key, Class<T> clazz) {
        return ConversionUtils.looseBind(clazz, get(key));
    }

    /**
     * 获取参数值，并将其绑定到指定的类型
     *
     * @param key            参数名
     * @param resolvableType 结果类型
     * @param <T>            结果类型泛型
     * @return 对应结果类型的值
     */
    @SuppressWarnings("unchecked")
    public <T> T bindValue(String key, ResolvableType resolvableType) {
        return (T) ConversionUtils.looseBind(resolvableType, get(key));
    }

    /**
     * 获取参数值，并将其绑定到指定的类型
     *
     * @param key       参数名
     * @param typeToken 结果类型
     * @param <T>       结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T bindValue(String key, SerializationTypeToken<T> typeToken) {
        return ConversionUtils.looseBind(typeToken, get(key));
    }

    /**
     * 获取参数值，并将其转化为指定的类型
     *
     * @param key   参数名
     * @param clazz 结果类型
     * @param <T>   结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T getValue(String key, Class<T> clazz) {
        return ConversionUtils.conversion(get(key), clazz);
    }

    /**
     * 获取参数值，并将其转化为指定的类型
     *
     * @param key            参数名
     * @param resolvableType 结果类型
     * @param <T>            结果类型泛型
     * @return 对应结果类型的值
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, ResolvableType resolvableType) {
        return (T) ConversionUtils.conversion(get(key), resolvableType);
    }

    /**
     * 获取参数值，并将其转化为指定的类型
     *
     * @param key       参数名
     * @param typeToken 结果类型
     * @param <T>       结果类型泛型
     * @return 对应结果类型的值
     */
    public <T> T getValue(String key, SerializationTypeToken<T> typeToken) {
        return ConversionUtils.conversion(get(key), typeToken);
    }

    //----------------------------------------------------------
    //                    Basic Types
    //----------------------------------------------------------


    public String getString(String key) {
        return getValue(key, String.class);
    }

    public Integer getInt(String key) {
        return getValue(key, Integer.class);
    }

    public Long getLong(String key) {
        return getValue(key, Long.class);
    }

    public Double getDouble(String key) {
        return getValue(key, Double.class);
    }

    public Boolean getBoolean(String key) {
        return getValue(key, Boolean.class);
    }

    public Float getFloat(String key) {
        return getValue(key, Float.class);
    }

    public Short getShort(String key) {
        return getValue(key, Short.class);
    }

    public Byte getByte(String key) {
        return getValue(key, Byte.class);
    }

    public Character getChar(String key) {
        return getValue(key, Character.class);
    }

    //----------------------------------------------------------
    //                    Basic Types Array
    //----------------------------------------------------------

    public String[] getStringArray(String key) {
        return getValue(key, String[].class);
    }

    public int[] getIntArray(String key) {
        return getValue(key, int[].class);
    }

    public long[] getLongArray(String key) {
        return getValue(key, long[].class);
    }

    public double[] getDoubleArray(String key) {
        return getValue(key, double[].class);
    }

    public boolean[] getBooleanArray(String key) {
        return getValue(key, boolean[].class);
    }

    public float[] getFloatArray(String key) {
        return getValue(key, float[].class);
    }

    public short[] getShortArray(String key) {
        return getValue(key, short[].class);
    }

    public byte[] getByteArray(String key) {
        return getValue(key, byte[].class);
    }

    public char[] getCharArray(String key) {
        return getValue(key, char[].class);
    }


    //----------------------------------------------------------
    //                    Basic Types List
    //----------------------------------------------------------

    public <E> List<E> getList(String key, Class<E> elementClass) {
        return getValue(key, ResolvableType.forClassWithGenerics(List.class, elementClass));
    }

    public List<String> getStringList(String key) {
        return getList(key, String.class);
    }

    public List<Integer> getIntList(String key) {
        return getList(key, Integer.class);
    }

    public List<Long> getLongList(String key) {
        return getList(key, Long.class);
    }

    public List<Double> getDoubleList(String key) {
        return getList(key, Double.class);
    }

    public List<Boolean> getBooleanList(String key) {
        return getList(key, Boolean.class);
    }

    public List<Float> getFloatList(String key) {
        return getList(key, Float.class);
    }

    public List<Short> getShortList(String key) {
        return getList(key, Short.class);
    }

    public List<Byte> getByteList(String key) {
        return getList(key, Byte.class);
    }

    public List<Character> getCharList(String key) {
        return getList(key, Character.class);
    }
}
