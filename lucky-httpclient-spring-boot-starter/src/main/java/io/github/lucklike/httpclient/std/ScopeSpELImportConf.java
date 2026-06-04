package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.proxy.configapi.SpELImportConf;

public class ScopeSpELImportConf {

    private SpELImportConf methodMetaSpringElImport;
    private SpELImportConf methodContentSpringElImport;

    public SpELImportConf getMethodMetaSpringElImport() {
        return methodMetaSpringElImport;
    }

    public void setMethodMetaSpringElImport(SpELImportConf methodMetaSpringElImport) {
        this.methodMetaSpringElImport = methodMetaSpringElImport;
    }

    public SpELImportConf getMethodContentSpringElImport() {
        return methodContentSpringElImport;
    }

    public void setMethodContentSpringElImport(SpELImportConf methodContentSpringElImport) {
        this.methodContentSpringElImport = methodContentSpringElImport;
    }
}
