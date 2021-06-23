package com.github.zhizuqiu.nettyrestful.server.tools;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpMethod;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MethodTool {

    /**
     * 从netty的接口method 映射为 注解的接口method
     *
     * @param httpMethod netty的接口method
     * @return 注解的接口method
     */
    public static HttpMap.Method getMethod(HttpMethod httpMethod) {
        if (httpMethod == HttpMethod.GET) {
            return HttpMap.Method.GET;
        } else if (httpMethod == HttpMethod.POST) {
            return HttpMap.Method.POST;
        } else if (httpMethod == HttpMethod.PUT) {
            return HttpMap.Method.PUT;
        } else if (httpMethod == HttpMethod.DELETE) {
            return HttpMap.Method.DELETE;
        } else {
            return HttpMap.Method.OTHER;
        }
    }

    /**
     * 从http请求的header 映射为 注解的参数类型
     *
     * @param header http请求的header
     * @return 注解的参数类型
     */
    public static HttpMap.ParamType getParamTypeFromHeader(String header) {
        if (header == null) {
            return HttpMap.ParamType.URL_DATA;
        }
        if (header.contains("application/x-www-form-urlencoded")) {
            return HttpMap.ParamType.FORM_DATA;
        } else if (header.contains("multipart/form-data")) {
            return HttpMap.ParamType.MULTIPART_FORM_DATA;
        } else if (header.contains("application/json")) {
            return HttpMap.ParamType.JSON;
        } else {
            return HttpMap.ParamType.URL_DATA;
        }
    }

    /**
     * 从src和jar中获取class列表
     *
     * @param paths 包路径
     */
    public static List<Class> getClasses(List<String> paths) {
        List<Class> classList = new ArrayList<>();
        classList.addAll(getClassesFromSrc(paths));
        classList.addAll(getClassesFromJar(paths));
        return classList;
    }

    /**
     * 从src中获取class列表
     *
     * @param paths 包路径
     */
    private static List<Class> getClassesFromSrc(List<String> paths) {
        List<Class> classList = new ArrayList<>();
        for (String basePack : paths) {
            URL url = MethodTool.class.getResource("/");
            if (url == null) {
                return classList;
            }
            if (!"file".equals(url.getProtocol())) {
                return classList;
            }
            String classpath = url.getPath();
            basePack = basePack.replace(".", File.separator);
            String searchPath = classpath + basePack;
            List<String> classPaths = getClassPath(new File(searchPath));
            for (String s : classPaths) {
                if (!s.startsWith("/")) {
                    s = "/" + s;
                }
                s = s.replace("\\", "/").replace(classpath, "").replace("/", ".").replace(".class", "");
                Class cls = null;
                try {
                    cls = Class.forName(s);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                classList.add(cls);
            }
        }
        return classList;
    }

    /**
     * 该方法会得到所有的类，将类的绝对路径写入到classPaths中
     */
    private static List<String> getClassPath(File file) {
        List<String> classPaths = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f1 : files) {
                    classPaths.addAll(getClassPath(f1));
                }
            }
        } else {
            if (file.getName().endsWith(".class")) {
                classPaths.add(file.getPath());
            }
        }
        return classPaths;
    }

    /**
     * 从jar中获取class列表
     *
     * @param paths 包路径
     */
    private static List<Class> getClassesFromJar(List<String> paths) {
        List<Class> classList = new ArrayList<>();
        try {
            for (String basePack : paths) {
                Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(basePack.replace(".", "/"));
                while (urlEnumeration.hasMoreElements()) {
                    URL url = urlEnumeration.nextElement();
                    String protocol = url.getProtocol();
                    if ("jar".equalsIgnoreCase(protocol)) {
                        JarURLConnection connection = (JarURLConnection) url.openConnection();
                        if (connection != null) {
                            JarFile jarFile = connection.getJarFile();
                            if (jarFile != null) {
                                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                                while (jarEntryEnumeration.hasMoreElements()) {
                                    JarEntry entry = jarEntryEnumeration.nextElement();
                                    String jarEntryName = entry.getName();
                                    if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/", ".").startsWith(basePack)) {
                                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                        Class cls = Class.forName(className);
                                        classList.add(cls);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classList;
    }

    /**
     * 生成Gson实例，排除带有str字符的
     *
     * @param str 字符
     * @return Gson实例
     */
    public static Gson newGsonExcludeStartsWithStr(final String str) {
        ExclusionStrategy myExclusionStrategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fa) {
                return fa.getName().startsWith(str);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        return new GsonBuilder()
                .setExclusionStrategies(myExclusionStrategy)
                .create();
    }

    /**
     * 生成Gson实例，排除指定Modifier类型的字段
     *
     * @param modifier Modifier
     * @return Gson实例
     */
    public static Gson newGsonExcludeModifier(int modifier) {
        return new GsonBuilder()
                .excludeFieldsWithModifiers(modifier)
                .create();
    }

    /**
     * 生成Gson实例，排除没有带有Expose注解的
     *
     * @return Gson实例
     */
    public static Gson newGsonExcludeExpose() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    /**
     * 获取静态资源
     */
    public Map<String, String> getResources() {
        Map<String, String> resourcesMap = new HashMap<>();

        /*
        URL url = MethodTool.class.getResource("MethodTool.class");
        if (url.toString().startsWith("jar")) {
            // 如果以jar运行
            String jar = MethodTool.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            List<String> list = new LinkedList<>();
            JarFile jf = null;
            try {
                jf = new JarFile(jar);
                Enumeration<JarEntry> es = jf.entries();
                while (es.hasMoreElements()) {
                    String resname = es.nextElement().getName();
                    if (checkReoucesSuffix(resname)) {
                        list.add(resname);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (jf != null) {
                    try {
                        jf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String path : list) {
                InputStream is = this.getClass().getResourceAsStream("/".concat(path));
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(inputStreamReader);
                StringBuilder stringBuffer = new StringBuilder();
                String s1;
                try {
                    while ((s1 = br.readLine()) != null) {
                        stringBuffer.append(s1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                        inputStreamReader.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    resourcesMap.putRestMethod("/".concat(path).replaceAll("\\\\", "/"), new String(stringBuffer.toString().getBytes(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        */

        // 如果debug运行
        String prefix = MethodData.getConfig().getStaticFilePath();
        if (prefix == null) {
            return resourcesMap;
        }
        DirAndFile dirAndFile = new DirAndFile();
        dirAndFile.iteratorPath(prefix);
        List<String> paths = dirAndFile.getPathName();
        for (String path : paths) {
            if (!checkReoucesSuffix(path)) {
                continue;
            }
            File file = new File(path);
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fileReader == null) {
                continue;
            }
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder stringBuffer = new StringBuilder();
            String s;
            try {
                while ((s = br.readLine()) != null) {
                    stringBuffer.append(s).append('\n');
                }
            } catch (IOException e) {
                // ignore
            } finally {
                try {
                    br.close();
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            path = path.replaceAll("\\\\", "/");
            if (path.contains(prefix)) {
                path = path.substring(prefix.length(), path.length());
            }
            try {
                resourcesMap.put(path, new String(stringBuffer.toString().getBytes(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return resourcesMap;
    }

    /**
     * 判断是否为静态资源，只运行html/htm
     */
    private static boolean checkReoucesSuffix(String path) {
        if (path.startsWith("META-INF/")
                || path.startsWith("org.")
                || path.startsWith("io.")
                || path.startsWith("com.")) {
            return false;
        }
        if (path.endsWith(".class")
                || path.endsWith(".java")
                || path.endsWith("/")) {
            return false;
        }
        if (path.lastIndexOf("/") < path.lastIndexOf(".")) {
            if (path.endsWith(".html") || path.endsWith(".htm")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getSuffix(String path) {
        String suffix = null;

        if (path == null) {
            return null;
        }
        if (path.contains(".")) {
            suffix = path.substring(path.lastIndexOf("."), path.length());
        }
        return suffix;
    }

    public static String serializeString(HttpMap httpMap, Object re) {
        // 转json
        String result;
        if (httpMap.returnType() == HttpMap.ReturnType.APPLICATION_JSON) {
            switch (httpMap.gsonExcludeType()) {
                case Expose:
                    result = MethodTool.newGsonExcludeExpose().toJson(re);
                    break;
                case Modifier:
                    result = MethodTool.newGsonExcludeModifier(httpMap.modifierType()).toJson(re);
                    break;
                case SkipFieldStartWith:
                    result = MethodTool.newGsonExcludeStartsWithStr(httpMap.skipFieldStartWith()).toJson(re);
                    break;
                case Default:
                    result = new Gson().toJson(re);
                    break;
                default:
                    result = new Gson().toJson(re);
                    break;
            }
        } else {
            if (re != null) {
                result = re.toString();
            } else {
                result = null;
            }
        }
        return result;
    }

}
