package com.github.zhizuqiu.nettyrestful.client.test;

/**
 * 客户端端到端测试用 POJO：由 Gson 编解码往返。
 */
public class Item {
    private String name;
    private int value;

    public Item() {
    }

    public Item(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Item{name='" + name + "', value=" + value + "}";
    }
}
