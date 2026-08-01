package com.github.zhizuqiu.nettyrestful.codec.gson;

import com.google.gson.annotations.Expose;

/**
 * Gson 编解码测试用例 fixture。
 * 仅 {@code name} 与 {@code age} 标注了 {@link Expose}，用于验证
 * excludeFieldsWithoutExposeAnnotation 策略。
 */
public class SampleMessage {

    @Expose
    private String name;

    @Expose
    private int age;

    private String secret;

    public SampleMessage() {
    }

    public SampleMessage(String name, int age, String secret) {
        this.name = name;
        this.age = age;
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "SampleMessage{name='" + name + "', age=" + age + ", secret='" + secret + "'}";
    }
}
