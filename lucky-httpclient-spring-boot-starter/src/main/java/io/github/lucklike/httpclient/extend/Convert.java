package io.github.lucklike.httpclient.extend;

import com.luckyframework.conversion.TargetField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 16:19
 */
public class Convert {

    private String result;
    private String exception;
    @TargetField("default-value")
    private String defaultValue;

    private List<Condition> condition = new ArrayList<>();

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<Condition> getCondition() {
        return condition;
    }

    public void setCondition(List<Condition> condition) {
        this.condition = condition;
    }
}
