package com.github.zhizuqiu.nettyrestful.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author zhizuqiu
 * Http 请求各种参数的注解定义
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMap {
    enum ParamType {
        // 参数类型枚举
        JSON, FORM_DATA, MULTIPART_FORM_DATA, URL_DATA
    }

    enum ReturnType {
        // 返回类型枚举
        APPLICATION_JSON, TEXT_PLAIN, TEXT_HTML
    }

    enum Method {
        // 请求方法枚举
        GET, POST, PUT, DELETE, OTHER
    }

    enum GsonExcludeType {
        // Gson排除方式
        Default, Modifier, Expose, SkipFieldStartWith
    }

    // 请求url地址
    String path();

    // 参数类型
    ParamType paramType() default ParamType.URL_DATA;

    // 返回类型
    ReturnType returnType() default ReturnType.APPLICATION_JSON;

    // 请求方式
    Method method() default Method.GET;

    // Gson排除方式
    GsonExcludeType gsonExcludeType() default GsonExcludeType.Default;

    // Gson排除以此字段开头的变量
    String skipFieldStartWith() default "";

    // Gson排除带有此类型的变量
    int modifierType() default 1;
}
