package com.github.zhizuqiu.nettyrestful.template.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.zhizuqiu.nettyrestful.core.template.Template;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class MustacheTemplate implements Template {

    @Override
    public String parse(Object scopes, String text) throws Exception {
        if (text == null) {
            return null;
        }
        Writer writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(text), "test");
        mustache.execute(writer, scopes);
        return writer.toString();
    }

}
