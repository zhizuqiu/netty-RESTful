package com.github.zhizuqiu.nettyrestful.client.netty;

public class AnnotationItem {
    private int index;
    private String name;

    public AnnotationItem(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AnnotationItem{" +
                "index=" + index +
                ", name='" + name + '\'' +
                '}';
    }
}
