package com.github.zhizuqiu.example.template;

public class TemplateBody {
    private String p;

    public TemplateBody(String p) {
        this.p = p;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    @Override
    public String toString() {
        return "TemplateBody{" +
                "p='" + p + '\'' +
                '}';
    }
}
