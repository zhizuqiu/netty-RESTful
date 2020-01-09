package com.github.zhizuqiu.example.template;

public class TemplateObject {
    private String title;

    private TemplateBody body;

    public TemplateObject(String title, TemplateBody body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TemplateBody getBody() {
        return body;
    }

    public void setBody(TemplateBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "TemplateObject{" +
                "title='" + title + '\'' +
                ", body=" + body +
                '}';
    }
}
