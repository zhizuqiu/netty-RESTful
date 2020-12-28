package com.github.zhizuqiu.nettyrestfulcore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author zhizuqiu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateMap {
    /**
     * 请求url地址
     */
    String path();
}
