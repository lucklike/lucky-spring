package io.github.lucklike.httpclient.injection;


import org.springframework.core.ResolvableType;

import java.lang.reflect.AnnotatedElement;

public class FieldInfo {

    private final AnnotatedElement element;
    private final ResolvableType type;

    private FieldInfo(AnnotatedElement element, ResolvableType type) {
        this.element = element;
        this.type = type;
    }

    public static FieldInfo of(AnnotatedElement element, ResolvableType type) {
        return new FieldInfo(element, type);
    }

    public AnnotatedElement getElement() {
        return element;
    }

    public ResolvableType getType() {
        return type;
    }
}
