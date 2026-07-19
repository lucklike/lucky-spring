package io.github.lucklike.httpclient.dbclient.function;

import com.luckyframework.common.SnowflakeIdGeneratorCas;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.reflect.FieldUtils;
import io.github.lucklike.httpclient.dbclient.annotation.IdType;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;

import static com.luckyframework.httpclient.proxy.function.RandomFunctions.nanoid;
import static com.luckyframework.httpclient.proxy.function.RandomFunctions.uuid;

/**
 * ID 属性
 */
public class IdField {

    public static final IdField NULL = new IdField(null, null);

    private final Field field;
    private final IdType idType;

    private IdField(Field field, IdType idType) {
        this.field = field;
        this.idType = idType;
    }

    public static IdField of(Field field, IdType idType) {
        return new IdField(field, idType);
    }

    public Field getField() {
        return field;
    }

    public IdType getIdType() {
        return idType;
    }

    public boolean isAutoIncrement() {
        return IdType.AUTO_INCREMENT.equals(idType);
    }

    public boolean isUuid() {
        return IdType.UUID.equals(idType);
    }

    public boolean isNanoId() {
        return IdType.NANOID.equals(idType);
    }

    public boolean isSnowflakeId() {
        return IdType.SNOWFLAKE_ID.equals(idType);
    }

    public boolean isManualSettings() {
        return IdType.MANUAL_SETTINGS.equals(idType);
    }

    public void setId(Object entity) {
        Object value = FieldUtils.getValue(entity, field);
        if (value == null) {
            if (isNanoId()) {
                value = nanoid();
            } else if (isUuid()) {
                value = uuid();
            } else if (isSnowflakeId()) {
                value = SnowflakeIdGeneratorCas.INSTANCE.nextId();
            }
            if (value != null) {
                FieldUtils.setValue(entity, field, ConversionUtils.conversion(value, field.getType()));
            }
        }
    }

    public void setId(Object entity, KeyHolder keyHolder) {
        FieldUtils.setValue(entity, field, ConversionUtils.conversion(keyHolder.getKey(), field.getType()));
    }
}
