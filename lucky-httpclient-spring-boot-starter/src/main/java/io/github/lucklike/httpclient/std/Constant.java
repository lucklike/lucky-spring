package io.github.lucklike.httpclient.std;

public abstract class Constant {

    // 命名空间
    public static final String STD_HTTP = "::STD_HTTP::";

    // 方法名
    public static final String SIMPLE_FUNC_URL_GET = "__get_http_server_url__";
    public static final String SIMPLE_FUNC_MOCK_ENABLE = "__std_mock_enable__";
    public static final String SIMPLE_FUNC_MOCK_RESULT = "__std_mock_result__";
    public static final String SIMPLE_FUNC_RESP_META_TYPE_GET = "__get_response_meta_type__";
    public static final String SIMPLE_FUNC_RESULT_CONVERT = "__result_convert__";
    public static final String SIMPLE_FUNC_MDRCT = "__mandatory_designation_response_content_type__";


    // 完整方法名
    public static final String FUNC_URL_GET = STD_HTTP + "." + SIMPLE_FUNC_URL_GET;
    public static final String FUNC_FUNC_MOCK_RESULT = STD_HTTP + "." + SIMPLE_FUNC_MOCK_RESULT;
    public static final String FUNC_RESP_META_TYPE_GET = STD_HTTP + "." + SIMPLE_FUNC_RESP_META_TYPE_GET;
    public static final String FUNC_RESULT_CONVERT = STD_HTTP + "." + SIMPLE_FUNC_RESULT_CONVERT;


    // 方法调用
    public static final String CALL_FUNC_MOCK_ENABLE = "#{#__$Val$__['" + STD_HTTP + "']." + SIMPLE_FUNC_MOCK_ENABLE + "($mc$)}";
    public static final String CALL_FUNC_MDRCT = "#{#__$Val$__['" + STD_HTTP + "']." + SIMPLE_FUNC_MDRCT + "($mc$)}";

    // 存储标准客户端配置对象的变量名
    public static final String STANDARD_HTTP_CLIENT_CONFIG_NAME = "$StandardHttpClientConfiguration";
    // 存储标准API配置对象的变量名
    public static final String STANDARD_API_CONFIG_NAME = "$StandardApiConfiguration";
    // 存储生命周期管理器对象变量名
    public static final String LIFE_CYCLE_MANAGER_NAME = "$LifeCycleManager";
    // 存储标准客户端 Mock 相关配置的变量名
    public static final String STANDARD_MOCK_CONFIG = "$StandardMockConfiguration";

}
