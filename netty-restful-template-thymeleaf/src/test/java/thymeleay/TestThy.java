package thymeleay;

import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.HashMap;
import java.util.Map;

public class TestThy {
    @Test
    public void testClassLoader() {

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("name", "Thomas");

        String html = templateEngine.process("template", context);

        System.out.println(html);
    }

    @Test
    public void testThy() {

        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode("HTML");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Map<String, Object> value = new HashMap<>();
        value.put("greeting", "Abel");
        value.put("last_name", "Lincon");

        Context context = new Context();
        context.setVariable("name", value);

        String html = templateEngine.process("<h1>Hi! My name is [[${name.greeting}]]!</h1>", context);

        System.out.println(html);
    }

}
