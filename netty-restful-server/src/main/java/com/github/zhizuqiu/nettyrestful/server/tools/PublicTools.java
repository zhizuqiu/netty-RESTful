package com.github.zhizuqiu.nettyrestful.server.tools;

import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.handler.codec.http.HttpRequest;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicTools {
    /**
     * 从resources目录获取html
     *
     * @param path 路径
     * @return html string
     */
    public static String getHtmlFromResources(String path) {
        if (MethodData.getConfig().getStaticFilePath() == null) {
            return "";
        }

        if (path == null) {
            return "";
        }

        String pathOld = path;

        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }
        StringBuilder stringBuffer = new StringBuilder();
        File file = new File(MethodData.getConfig().getStaticFilePath() + "/" + path);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fileReader == null) {
            return stringBuffer.toString();
        }
        BufferedReader br = new BufferedReader(fileReader);
        String s;
        try {
            while ((s = br.readLine()) != null) {
                stringBuffer.append(s).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // @TemplateMap
        String value = stringBuffer.toString();
        value = MethodData.templateFromUrl(pathOld, value);
        return value;
    }

    /**
     * 从HttpRequest中获取cookie
     */
    public static Map<String, String> getCookie(HttpRequest req) {
        Map<String, String> cookies = new HashMap<>();
        List<Map.Entry<String, String>> entries = req.headers().entries();
        for (Map.Entry<String, String> entry : entries) {
            if ("Cookie".equals(entry.getKey())) {
                String value = entry.getValue();
                if (value != null) {
                    String[] values = value.split(";");
                    for (String v : values) {
                        String[] vs = v.split("=");
                        if (vs.length == 2) {
                            cookies.put(vs[0], vs[1]);
                        }
                    }
                }
            }
        }
        return cookies;
    }

    /**
     * 从HttpRequest中获取value
     */
    public static String getValue(HttpRequest req, String key) {
        String str = "";
        List<Map.Entry<String, String>> entries = req.headers().entries();
        for (Map.Entry<String, String> entry : entries) {
            if (key.equals(entry.getKey())) {
                str = entry.getValue();
            }
        }
        return str;
    }

    /**
     * 从HttpRequest中获取User-Agent
     */
    public static String getUserAgent(HttpRequest req) {
        return getValue(req, "User-Agent");
    }

    /**
     * 从HttpRequest中获取Connection
     */
    public static String getConnection(HttpRequest req) {
        return getValue(req, "Connection");
    }

    /**
     * 从HttpRequest中获取content-length
     */
    public static String getContentLength(HttpRequest req) {
        return getValue(req, "content-length");
    }

    /**
     * 从HttpRequest中获取Host
     */
    public static String getHost(HttpRequest req) {
        return getValue(req, "Host");
    }

    /**
     * 从HttpRequest中获取Accept
     */
    public static String getAccept(HttpRequest req) {
        return getValue(req, "Accept");
    }

    /**
     * 从HttpRequest中获取cache-control
     */
    public static String getCacheControl(HttpRequest req) {
        return getValue(req, "cache-control");
    }

}
