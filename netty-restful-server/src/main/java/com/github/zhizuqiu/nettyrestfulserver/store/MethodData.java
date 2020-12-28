package com.github.zhizuqiu.nettyrestfulserver.store;


import com.github.zhizuqiu.nettyrestfulserver.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestfulserver.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestfulserver.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestfulserver.tools.PublicTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MethodData {

    /**
     * 存储restful method
     */
    private static Map<RestMethodKey, RestMethodValue> restMethodMap = new HashMap<>();
    /**
     * 存储template method
     */
    private static Map<String, TemplateMethodValue> templateMethodMap = new HashMap<>();
    /**
     * 存储静态资源
     */
    private static Map<RestMethodKey, ResourceValue> resourcesMap = new HashMap<>();
    /**
     * 存储拦截器
     */
    private static List<AbstractInterceptor> interceptorList = new ArrayList<>();
    /**
     * 存储文件后缀与ContentType的对应关系
     */
    public static Map<String, String> suffixMapContentType = new HashMap<>();
    /**
     * 存储配置
     */
    private static Config config = new Config();

    public static RestMethodValue putRestMethod(RestMethodKey key, RestMethodValue value) {
        return restMethodMap.put(key, value);
    }

    public static RestMethodValue getRestMethod(RestMethodKey key) {
        return restMethodMap.get(key);
    }

    public static Set<RestMethodKey> restMethodKeySet() {
        return restMethodMap.keySet();
    }

    public static Boolean restMethodIsEmpty() {
        return restMethodMap.isEmpty();
    }

    public static String toStr() {
        return restMethodMap.toString();
    }

    public static TemplateMethodValue putTemplateMethod(String key, TemplateMethodValue value) {
        return templateMethodMap.put(key, value);
    }

    public static TemplateMethodValue getTemplateMethod(String key) {
        return templateMethodMap.get(key);
    }

    public static Set<String> templateMethodKeySet() {
        return templateMethodMap.keySet();
    }

    public static Boolean templateMethodIsEmpty() {
        return templateMethodMap.isEmpty();
    }

    public static ResourceValue getResource(RestMethodKey key) {
        return resourcesMap.get(key);
    }

    public static ResourceValue putResource(RestMethodKey key, ResourceValue resource) {
        return resourcesMap.put(key, resource);
    }

    public static Set<RestMethodKey> resourcesKeySet() {
        return resourcesMap.keySet();
    }

    public static List<AbstractInterceptor> getInterceptorList() {
        return interceptorList;
    }

    public static void setInterceptorList(List<AbstractInterceptor> interceptorList) {
        MethodData.interceptorList = interceptorList;
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        MethodData.config = config;
    }

    /**
     * 从resourcesMap获取html，并且进行模板填充
     */
    public static String getResourceAndTemplate(RestMethodKey key) {
        ResourceValue resourceValue = resourcesMap.get(key);
        if (resourceValue == null) {
            return null;
        }
        String value = resourceValue.getValue();
        value = template(key.getUrl(), value);
        return value;
    }

    /**
     * 根据path从templateMethodMap中获取模板参数，并填充到value中
     */
    public static String template(String path, String value) {
        TemplateMethodValue templateMethodValue = getTemplateMethod(path);
        if (templateMethodValue != null) {
            Object re = null;
            Method method = templateMethodValue.getMethod();
            try {
                Object restHandler = templateMethodValue.getInstance();
                re = method.invoke(restHandler);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (re != null) {
                value = PublicTools.templateMustache(re, value);
            }
        }
        return value;
    }

    static {
        suffixMapContentType.put(".load", "text/html");
        suffixMapContentType.put(".123", "application/vnd.lotus-1-2-3");
        suffixMapContentType.put(".3ds", "image/x-3ds");
        suffixMapContentType.put(".3g2", "video/3gpp");
        suffixMapContentType.put(".3ga", "video/3gpp");
        suffixMapContentType.put(".3gp", "video/3gpp");
        suffixMapContentType.put(".3gpp", "video/3gpp");
        suffixMapContentType.put(".602", "application/x-t602");
        suffixMapContentType.put(".669", "audio/x-mod");
        suffixMapContentType.put(".7z", "application/x-7z-compressed");
        suffixMapContentType.put(".a", "application/x-archive");
        suffixMapContentType.put(".aac", "audio/mp4");
        suffixMapContentType.put(".abw", "application/x-abiword");
        suffixMapContentType.put(".abw.crashed", "application/x-abiword");
        suffixMapContentType.put(".abw.gz", "application/x-abiword");
        suffixMapContentType.put(".ac3", "audio/ac3");
        suffixMapContentType.put(".ace", "application/x-ace");
        suffixMapContentType.put(".adb", "text/x-adasrc");
        suffixMapContentType.put(".ads", "text/x-adasrc");
        suffixMapContentType.put(".afm", "application/x-font-afm");
        suffixMapContentType.put(".ag", "image/x-applix-graphics");
        suffixMapContentType.put(".ai", "application/illustrator");
        suffixMapContentType.put(".aif", "audio/x-aiff");
        suffixMapContentType.put(".aifc", "audio/x-aiff");
        suffixMapContentType.put(".aiff", "audio/x-aiff");
        suffixMapContentType.put(".al", "application/x-perl");
        suffixMapContentType.put(".alz", "application/x-alz");
        suffixMapContentType.put(".amr", "audio/amr");
        suffixMapContentType.put(".ani", "application/x-navi-animation");
        suffixMapContentType.put(".anim[1-9j]", "video/x-anim");
        suffixMapContentType.put(".anx", "application/annodex");
        suffixMapContentType.put(".ape", "audio/x-ape");
        suffixMapContentType.put(".arj", "application/x-arj");
        suffixMapContentType.put(".arw", "image/x-sony-arw");
        suffixMapContentType.put(".as", "application/x-applix-spreadsheet");
        suffixMapContentType.put(".asc", "text/plain");
        suffixMapContentType.put(".asf", "video/x-ms-asf");
        suffixMapContentType.put(".asp", "application/x-asp");
        suffixMapContentType.put(".ass", "text/x-ssa");
        suffixMapContentType.put(".asx", "audio/x-ms-asx");
        suffixMapContentType.put(".atom", "application/atom+xml");
        suffixMapContentType.put(".au", "audio/basic");
        suffixMapContentType.put(".avi", "video/x-msvideo");
        suffixMapContentType.put(".aw", "application/x-applix-word");
        suffixMapContentType.put(".awb", "audio/amr-wb");
        suffixMapContentType.put(".awk", "application/x-awk");
        suffixMapContentType.put(".axa", "audio/annodex");
        suffixMapContentType.put(".axv", "video/annodex");
        suffixMapContentType.put(".bak", "application/x-trash");
        suffixMapContentType.put(".bcpio", "application/x-bcpio");
        suffixMapContentType.put(".bdf", "application/x-font-bdf");
        suffixMapContentType.put(".bib", "text/x-bibtex");
        suffixMapContentType.put(".bin", "application/octet-stream");
        suffixMapContentType.put(".blend", "application/x-blender");
        suffixMapContentType.put(".blender", "application/x-blender");
        suffixMapContentType.put(".bmp", "image/bmp");
        suffixMapContentType.put(".bz", "application/x-bzip");
        suffixMapContentType.put(".bz2", "application/x-bzip");
        suffixMapContentType.put(".c", "text/x-csrc");
        suffixMapContentType.put(".c++", "text/x-c++src");
        suffixMapContentType.put(".cab", "application/vnd.ms-cab-compressed");
        suffixMapContentType.put(".cb7", "application/x-cb7");
        suffixMapContentType.put(".cbr", "application/x-cbr");
        suffixMapContentType.put(".cbt", "application/x-cbt");
        suffixMapContentType.put(".cbz", "application/x-cbz");
        suffixMapContentType.put(".cc", "text/x-c++src");
        suffixMapContentType.put(".cdf", "application/x-netcdf");
        suffixMapContentType.put(".cdr", "application/vnd.corel-draw");
        suffixMapContentType.put(".cer", "application/x-x509-ca-cert");
        suffixMapContentType.put(".cert", "application/x-x509-ca-cert");
        suffixMapContentType.put(".cgm", "image/cgm");
        suffixMapContentType.put(".chm", "application/x-chm");
        suffixMapContentType.put(".chrt", "application/x-kchart");
        suffixMapContentType.put(".class", "application/x-java");
        suffixMapContentType.put(".cls", "text/x-tex");
        suffixMapContentType.put(".cmake", "text/x-cmake");
        suffixMapContentType.put(".cpio", "application/x-cpio");
        suffixMapContentType.put(".cpio.gz", "application/x-cpio-compressed");
        suffixMapContentType.put(".cpp", "text/x-c++src");
        suffixMapContentType.put(".cr2", "image/x-canon-cr2");
        suffixMapContentType.put(".crt", "application/x-x509-ca-cert");
        suffixMapContentType.put(".crw", "image/x-canon-crw");
        suffixMapContentType.put(".cs", "text/x-csharp");
        suffixMapContentType.put(".csh", "application/x-csh");
        suffixMapContentType.put(".css", "text/css");
        suffixMapContentType.put(".cssl", "text/css");
        suffixMapContentType.put(".csv", "text/csv");
        suffixMapContentType.put(".cue", "application/x-cue");
        suffixMapContentType.put(".cur", "image/x-win-bitsuffixMapContentType");
        suffixMapContentType.put(".cxx", "text/x-c++src");
        suffixMapContentType.put(".d", "text/x-dsrc");
        suffixMapContentType.put(".dar", "application/x-dar");
        suffixMapContentType.put(".dbf", "application/x-dbf");
        suffixMapContentType.put(".dc", "application/x-dc-rom");
        suffixMapContentType.put(".dcl", "text/x-dcl");
        suffixMapContentType.put(".dcm", "application/dicom");
        suffixMapContentType.put(".dcr", "image/x-kodak-dcr");
        suffixMapContentType.put(".dds", "image/x-dds");
        suffixMapContentType.put(".deb", "application/x-deb");
        suffixMapContentType.put(".der", "application/x-x509-ca-cert");
        suffixMapContentType.put(".desktop", "application/x-desktop");
        suffixMapContentType.put(".dia", "application/x-dia-diagram");
        suffixMapContentType.put(".diff", "text/x-patch");
        suffixMapContentType.put(".divx", "video/x-msvideo");
        suffixMapContentType.put(".djv", "image/vnd.djvu");
        suffixMapContentType.put(".djvu", "image/vnd.djvu");
        suffixMapContentType.put(".dng", "image/x-adobe-dng");
        suffixMapContentType.put(".doc", "application/msword");
        suffixMapContentType.put(".docbook", "application/docbook+xml");
        suffixMapContentType.put(".docm", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        suffixMapContentType.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        suffixMapContentType.put(".dot", "text/vnd.graphviz");
        suffixMapContentType.put(".dsl", "text/x-dsl");
        suffixMapContentType.put(".dtd", "application/xml-dtd");
        suffixMapContentType.put(".dtx", "text/x-tex");
        suffixMapContentType.put(".dv", "video/dv");
        suffixMapContentType.put(".dvi", "application/x-dvi");
        suffixMapContentType.put(".dvi.bz2", "application/x-bzdvi");
        suffixMapContentType.put(".dvi.gz", "application/x-gzdvi");
        suffixMapContentType.put(".dwg", "image/vnd.dwg");
        suffixMapContentType.put(".dxf", "image/vnd.dxf");
        suffixMapContentType.put(".e", "text/x-eiffel");
        suffixMapContentType.put(".egon", "application/x-egon");
        suffixMapContentType.put(".eif", "text/x-eiffel");
        suffixMapContentType.put(".el", "text/x-emacs-lisp");
        suffixMapContentType.put(".emf", "image/x-emf");
        suffixMapContentType.put(".emp", "application/vnd.emusic-emusic_package");
        suffixMapContentType.put(".ent", "application/xml-external-parsed-entity");
        suffixMapContentType.put(".eps", "image/x-eps");
        suffixMapContentType.put(".eps.bz2", "image/x-bzeps");
        suffixMapContentType.put(".eps.gz", "image/x-gzeps");
        suffixMapContentType.put(".epsf", "image/x-eps");
        suffixMapContentType.put(".epsf.bz2", "image/x-bzeps");
        suffixMapContentType.put(".epsf.gz", "image/x-gzeps");
        suffixMapContentType.put(".epsi", "image/x-eps");
        suffixMapContentType.put(".epsi.bz2", "image/x-bzeps");
        suffixMapContentType.put(".epsi.gz", "image/x-gzeps");
        suffixMapContentType.put(".epub", "application/epub+zip");
        suffixMapContentType.put(".erl", "text/x-erlang");
        suffixMapContentType.put(".es", "application/ecmascript");
        suffixMapContentType.put(".etheme", "application/x-e-theme");
        suffixMapContentType.put(".etx", "text/x-setext");
        suffixMapContentType.put(".exe", "application/x-ms-dos-executable");
        suffixMapContentType.put(".exr", "image/x-exr");
        suffixMapContentType.put(".ez", "application/andrew-inset");
        suffixMapContentType.put(".f", "text/x-fortran");
        suffixMapContentType.put(".f90", "text/x-fortran");
        suffixMapContentType.put(".f95", "text/x-fortran");
        suffixMapContentType.put(".fb2", "application/x-fictionbook+xml");
        suffixMapContentType.put(".fig", "image/x-xfig");
        suffixMapContentType.put(".fits", "image/fits");
        suffixMapContentType.put(".fl", "application/x-fluid");
        suffixMapContentType.put(".flac", "audio/x-flac");
        suffixMapContentType.put(".flc", "video/x-flic");
        suffixMapContentType.put(".fli", "video/x-flic");
        suffixMapContentType.put(".flv", "video/x-flv");
        suffixMapContentType.put(".flw", "application/x-kivio");
        suffixMapContentType.put(".fo", "text/x-xslfo");
        suffixMapContentType.put(".for", "text/x-fortran");
        suffixMapContentType.put(".g3", "image/fax-g3");
        suffixMapContentType.put(".gb", "application/x-gameboy-rom");
        suffixMapContentType.put(".gba", "application/x-gba-rom");
        suffixMapContentType.put(".gcrd", "text/directory");
        suffixMapContentType.put(".ged", "application/x-gedcom");
        suffixMapContentType.put(".gedcom", "application/x-gedcom");
        suffixMapContentType.put(".gen", "application/x-genesis-rom");
        suffixMapContentType.put(".gf", "application/x-tex-gf");
        suffixMapContentType.put(".gg", "application/x-sms-rom");
        suffixMapContentType.put(".gif", "image/gif");
        suffixMapContentType.put(".glade", "application/x-glade");
        suffixMapContentType.put(".gmo", "application/x-gettext-translation");
        suffixMapContentType.put(".gnc", "application/x-gnucash");
        suffixMapContentType.put(".gnd", "application/gnunet-directory");
        suffixMapContentType.put(".gnucash", "application/x-gnucash");
        suffixMapContentType.put(".gnumeric", "application/x-gnumeric");
        suffixMapContentType.put(".gnuplot", "application/x-gnuplot");
        suffixMapContentType.put(".gp", "application/x-gnuplot");
        suffixMapContentType.put(".gpg", "application/pgp-encrypted");
        suffixMapContentType.put(".gplt", "application/x-gnuplot");
        suffixMapContentType.put(".gra", "application/x-graphite");
        suffixMapContentType.put(".gsf", "application/x-font-type1");
        suffixMapContentType.put(".gsm", "audio/x-gsm");
        suffixMapContentType.put(".gtar", "application/x-tar");
        suffixMapContentType.put(".gv", "text/vnd.graphviz");
        suffixMapContentType.put(".gvp", "text/x-google-video-pointer");
        suffixMapContentType.put(".gz", "application/x-gzip");
        suffixMapContentType.put(".h", "text/x-chdr");
        suffixMapContentType.put(".h++", "text/x-c++hdr");
        suffixMapContentType.put(".hdf", "application/x-hdf");
        suffixMapContentType.put(".hh", "text/x-c++hdr");
        suffixMapContentType.put(".hp", "text/x-c++hdr");
        suffixMapContentType.put(".hpgl", "application/vnd.hp-hpgl");
        suffixMapContentType.put(".hpp", "text/x-c++hdr");
        suffixMapContentType.put(".hs", "text/x-haskell");
        suffixMapContentType.put(".htm", "text/html");
        suffixMapContentType.put(".html", "text/html");
        suffixMapContentType.put(".hwp", "application/x-hwp");
        suffixMapContentType.put(".hwt", "application/x-hwt");
        suffixMapContentType.put(".hxx", "text/x-c++hdr");
        suffixMapContentType.put(".ica", "application/x-ica");
        suffixMapContentType.put(".icb", "image/x-tga");
        suffixMapContentType.put(".icns", "image/x-icns");
        suffixMapContentType.put(".ico", "image/vnd.microsoft.icon");
        suffixMapContentType.put(".ics", "text/calendar");
        suffixMapContentType.put(".idl", "text/x-idl");
        suffixMapContentType.put(".ief", "image/ief");
        suffixMapContentType.put(".iff", "image/x-iff");
        suffixMapContentType.put(".ilbm", "image/x-ilbm");
        suffixMapContentType.put(".ime", "text/x-imelody");
        suffixMapContentType.put(".imy", "text/x-imelody");
        suffixMapContentType.put(".ins", "text/x-tex");
        suffixMapContentType.put(".iptables", "text/x-iptables");
        suffixMapContentType.put(".iso", "application/x-cd-image");
        suffixMapContentType.put(".iso9660", "application/x-cd-image");
        suffixMapContentType.put(".it", "audio/x-it");
        suffixMapContentType.put(".j2k", "image/jp2");
        suffixMapContentType.put(".jad", "text/vnd.sun.j2me.app-descriptor");
        suffixMapContentType.put(".jar", "application/x-java-archive");
        suffixMapContentType.put(".java", "text/x-java");
        suffixMapContentType.put(".jng", "image/x-jng");
        suffixMapContentType.put(".jnlp", "application/x-java-jnlp-file");
        suffixMapContentType.put(".jp2", "image/jp2");
        suffixMapContentType.put(".jpc", "image/jp2");
        suffixMapContentType.put(".jpe", "image/jpeg");
        suffixMapContentType.put(".jpeg", "image/jpeg");
        suffixMapContentType.put(".jpf", "image/jp2");
        suffixMapContentType.put(".jpg", "image/jpeg");
        suffixMapContentType.put(".jpr", "application/x-jbuilder-project");
        suffixMapContentType.put(".jpx", "image/jp2");
        suffixMapContentType.put(".js", "application/javascript");
        suffixMapContentType.put(".json", "application/json");
        suffixMapContentType.put(".jsonp", "application/jsonp");
        suffixMapContentType.put(".k25", "image/x-kodak-k25");
        suffixMapContentType.put(".kar", "audio/midi");
        suffixMapContentType.put(".karbon", "application/x-karbon");
        suffixMapContentType.put(".kdc", "image/x-kodak-kdc");
        suffixMapContentType.put(".kdelnk", "application/x-desktop");
        suffixMapContentType.put(".kexi", "application/x-kexiproject-sqlite3");
        suffixMapContentType.put(".kexic", "application/x-kexi-connectiondata");
        suffixMapContentType.put(".kexis", "application/x-kexiproject-shortcut");
        suffixMapContentType.put(".kfo", "application/x-kformula");
        suffixMapContentType.put(".kil", "application/x-killustrator");
        suffixMapContentType.put(".kino", "application/smil");
        suffixMapContentType.put(".kml", "application/vnd.google-earth.kml+xml");
        suffixMapContentType.put(".kmz", "application/vnd.google-earth.kmz");
        suffixMapContentType.put(".kon", "application/x-kontour");
        suffixMapContentType.put(".kpm", "application/x-kpovmodeler");
        suffixMapContentType.put(".kpr", "application/x-kpresenter");
        suffixMapContentType.put(".kpt", "application/x-kpresenter");
        suffixMapContentType.put(".kra", "application/x-krita");
        suffixMapContentType.put(".ksp", "application/x-kspread");
        suffixMapContentType.put(".kud", "application/x-kugar");
        suffixMapContentType.put(".kwd", "application/x-kword");
        suffixMapContentType.put(".kwt", "application/x-kword");
        suffixMapContentType.put(".la", "application/x-shared-library-la");
        suffixMapContentType.put(".latex", "text/x-tex");
        suffixMapContentType.put(".ldif", "text/x-ldif");
        suffixMapContentType.put(".lha", "application/x-lha");
        suffixMapContentType.put(".lhs", "text/x-literate-haskell");
        suffixMapContentType.put(".lhz", "application/x-lhz");
        suffixMapContentType.put(".log", "text/x-log");
        suffixMapContentType.put(".ltx", "text/x-tex");
        suffixMapContentType.put(".lua", "text/x-lua");
        suffixMapContentType.put(".lwo", "image/x-lwo");
        suffixMapContentType.put(".lwob", "image/x-lwo");
        suffixMapContentType.put(".lws", "image/x-lws");
        suffixMapContentType.put(".ly", "text/x-lilypond");
        suffixMapContentType.put(".lyx", "application/x-lyx");
        suffixMapContentType.put(".lz", "application/x-lzip");
        suffixMapContentType.put(".lzh", "application/x-lha");
        suffixMapContentType.put(".lzma", "application/x-lzma");
        suffixMapContentType.put(".lzo", "application/x-lzop");
        suffixMapContentType.put(".m", "text/x-matlab");
        suffixMapContentType.put(".m15", "audio/x-mod");
        suffixMapContentType.put(".m2t", "video/mpeg");
        suffixMapContentType.put(".m3u", "audio/x-mpegurl");
        suffixMapContentType.put(".m3u8", "audio/x-mpegurl");
        suffixMapContentType.put(".m4", "application/x-m4");
        suffixMapContentType.put(".m4a", "audio/mp4");
        suffixMapContentType.put(".m4b", "audio/x-m4b");
        suffixMapContentType.put(".m4v", "video/mp4");
        suffixMapContentType.put(".mab", "application/x-markaby");
        suffixMapContentType.put(".man", "application/x-troff-man");
        suffixMapContentType.put(".mbox", "application/mbox");
        suffixMapContentType.put(".md", "application/x-genesis-rom");
        suffixMapContentType.put(".mdb", "application/vnd.ms-access");
        suffixMapContentType.put(".mdi", "image/vnd.ms-modi");
        suffixMapContentType.put(".me", "text/x-troff-me");
        suffixMapContentType.put(".med", "audio/x-mod");
        suffixMapContentType.put(".metalink", "application/metalink+xml");
        suffixMapContentType.put(".mgp", "application/x-magicpoint");
        suffixMapContentType.put(".mid", "audio/midi");
        suffixMapContentType.put(".midi", "audio/midi");
        suffixMapContentType.put(".mif", "application/x-mif");
        suffixMapContentType.put(".minipsf", "audio/x-minipsf");
        suffixMapContentType.put(".mka", "audio/x-matroska");
        suffixMapContentType.put(".mkv", "video/x-matroska");
        suffixMapContentType.put(".ml", "text/x-ocaml");
        suffixMapContentType.put(".mli", "text/x-ocaml");
        suffixMapContentType.put(".mm", "text/x-troff-mm");
        suffixMapContentType.put(".mmf", "application/x-smaf");
        suffixMapContentType.put(".mml", "text/mathml");
        suffixMapContentType.put(".mng", "video/x-mng");
        suffixMapContentType.put(".mo", "application/x-gettext-translation");
        suffixMapContentType.put(".mo3", "audio/x-mo3");
        suffixMapContentType.put(".moc", "text/x-moc");
        suffixMapContentType.put(".mod", "audio/x-mod");
        suffixMapContentType.put(".mof", "text/x-mof");
        suffixMapContentType.put(".moov", "video/quicktime");
        suffixMapContentType.put(".mov", "video/quicktime");
        suffixMapContentType.put(".movie", "video/x-sgi-movie");
        suffixMapContentType.put(".mp+", "audio/x-musepack");
        suffixMapContentType.put(".mp2", "video/mpeg");
        suffixMapContentType.put(".mp3", "audio/mpeg");
        suffixMapContentType.put(".mp4", "video/mp4");
        suffixMapContentType.put(".mpc", "audio/x-musepack");
        suffixMapContentType.put(".mpe", "video/mpeg");
        suffixMapContentType.put(".mpeg", "video/mpeg");
        suffixMapContentType.put(".mpg", "video/mpeg");
        suffixMapContentType.put(".mpga", "audio/mpeg");
        suffixMapContentType.put(".mpp", "audio/x-musepack");
        suffixMapContentType.put(".mrl", "text/x-mrml");
        suffixMapContentType.put(".mrml", "text/x-mrml");
        suffixMapContentType.put(".mrw", "image/x-minolta-mrw");
        suffixMapContentType.put(".ms", "text/x-troff-ms");
        suffixMapContentType.put(".msi", "application/x-msi");
        suffixMapContentType.put(".msod", "image/x-msod");
        suffixMapContentType.put(".msx", "application/x-msx-rom");
        suffixMapContentType.put(".mtm", "audio/x-mod");
        suffixMapContentType.put(".mup", "text/x-mup");
        suffixMapContentType.put(".mxf", "application/mxf");
        suffixMapContentType.put(".n64", "application/x-n64-rom");
        suffixMapContentType.put(".nb", "application/mathematica");
        suffixMapContentType.put(".nc", "application/x-netcdf");
        suffixMapContentType.put(".nds", "application/x-nintendo-ds-rom");
        suffixMapContentType.put(".nef", "image/x-nikon-nef");
        suffixMapContentType.put(".nes", "application/x-nes-rom");
        suffixMapContentType.put(".nfo", "text/x-nfo");
        suffixMapContentType.put(".not", "text/x-mup");
        suffixMapContentType.put(".nsc", "application/x-netshow-channel");
        suffixMapContentType.put(".nsv", "video/x-nsv");
        suffixMapContentType.put(".o", "application/x-object");
        suffixMapContentType.put(".obj", "application/x-tgif");
        suffixMapContentType.put(".ocl", "text/x-ocl");
        suffixMapContentType.put(".oda", "application/oda");
        suffixMapContentType.put(".odb", "application/vnd.oasis.opendocument.database");
        suffixMapContentType.put(".odc", "application/vnd.oasis.opendocument.chart");
        suffixMapContentType.put(".odf", "application/vnd.oasis.opendocument.formula");
        suffixMapContentType.put(".odg", "application/vnd.oasis.opendocument.graphics");
        suffixMapContentType.put(".odi", "application/vnd.oasis.opendocument.image");
        suffixMapContentType.put(".odm", "application/vnd.oasis.opendocument.text-master");
        suffixMapContentType.put(".odp", "application/vnd.oasis.opendocument.presentation");
        suffixMapContentType.put(".ods", "application/vnd.oasis.opendocument.spreadsheet");
        suffixMapContentType.put(".odt", "application/vnd.oasis.opendocument.text");
        suffixMapContentType.put(".oga", "audio/ogg");
        suffixMapContentType.put(".ogg", "video/x-theora+ogg");
        suffixMapContentType.put(".ogm", "video/x-ogm+ogg");
        suffixMapContentType.put(".ogv", "video/ogg");
        suffixMapContentType.put(".ogx", "application/ogg");
        suffixMapContentType.put(".old", "application/x-trash");
        suffixMapContentType.put(".oleo", "application/x-oleo");
        suffixMapContentType.put(".opml", "text/x-opml+xml");
        suffixMapContentType.put(".ora", "image/openraster");
        suffixMapContentType.put(".orf", "image/x-olympus-orf");
        suffixMapContentType.put(".otc", "application/vnd.oasis.opendocument.chart-template");
        suffixMapContentType.put(".otf", "application/x-font-otf");
        suffixMapContentType.put(".otg", "application/vnd.oasis.opendocument.graphics-template");
        suffixMapContentType.put(".oth", "application/vnd.oasis.opendocument.text-web");
        suffixMapContentType.put(".otp", "application/vnd.oasis.opendocument.presentation-template");
        suffixMapContentType.put(".ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        suffixMapContentType.put(".ott", "application/vnd.oasis.opendocument.text-template");
        suffixMapContentType.put(".owl", "application/rdf+xml");
        suffixMapContentType.put(".oxt", "application/vnd.openofficeorg.extension");
        suffixMapContentType.put(".p", "text/x-pascal");
        suffixMapContentType.put(".p10", "application/pkcs10");
        suffixMapContentType.put(".p12", "application/x-pkcs12");
        suffixMapContentType.put(".p7b", "application/x-pkcs7-certificates");
        suffixMapContentType.put(".p7s", "application/pkcs7-signature");
        suffixMapContentType.put(".pack", "application/x-java-pack200");
        suffixMapContentType.put(".pak", "application/x-pak");
        suffixMapContentType.put(".par2", "application/x-par2");
        suffixMapContentType.put(".pas", "text/x-pascal");
        suffixMapContentType.put(".patch", "text/x-patch");
        suffixMapContentType.put(".pbm", "image/x-portable-bitsuffixMapContentType");
        suffixMapContentType.put(".pcd", "image/x-photo-cd");
        suffixMapContentType.put(".pcf", "application/x-cisco-vpn-settings");
        suffixMapContentType.put(".pcf.gz", "application/x-font-pcf");
        suffixMapContentType.put(".pcf.z", "application/x-font-pcf");
        suffixMapContentType.put(".pcl", "application/vnd.hp-pcl");
        suffixMapContentType.put(".pcx", "image/x-pcx");
        suffixMapContentType.put(".pdb", "chemical/x-pdb");
        suffixMapContentType.put(".pdc", "application/x-aportisdoc");
        suffixMapContentType.put(".pdf", "application/pdf");
        suffixMapContentType.put(".pdf.bz2", "application/x-bzpdf");
        suffixMapContentType.put(".pdf.gz", "application/x-gzpdf");
        suffixMapContentType.put(".pef", "image/x-pentax-pef");
        suffixMapContentType.put(".pem", "application/x-x509-ca-cert");
        suffixMapContentType.put(".perl", "application/x-perl");
        suffixMapContentType.put(".pfa", "application/x-font-type1");
        suffixMapContentType.put(".pfb", "application/x-font-type1");
        suffixMapContentType.put(".pfx", "application/x-pkcs12");
        suffixMapContentType.put(".pgm", "image/x-portable-graysuffixMapContentType");
        suffixMapContentType.put(".pgn", "application/x-chess-pgn");
        suffixMapContentType.put(".pgp", "application/pgp-encrypted");
        suffixMapContentType.put(".php", "application/x-php");
        suffixMapContentType.put(".php3", "application/x-php");
        suffixMapContentType.put(".php4", "application/x-php");
        suffixMapContentType.put(".pict", "image/x-pict");
        suffixMapContentType.put(".pict1", "image/x-pict");
        suffixMapContentType.put(".pict2", "image/x-pict");
        suffixMapContentType.put(".pickle", "application/python-pickle");
        suffixMapContentType.put(".pk", "application/x-tex-pk");
        suffixMapContentType.put(".pkipath", "application/pkix-pkipath");
        suffixMapContentType.put(".pkr", "application/pgp-keys");
        suffixMapContentType.put(".pl", "application/x-perl");
        suffixMapContentType.put(".pla", "audio/x-iriver-pla");
        suffixMapContentType.put(".pln", "application/x-planperfect");
        suffixMapContentType.put(".pls", "audio/x-scpls");
        suffixMapContentType.put(".pm", "application/x-perl");
        suffixMapContentType.put(".png", "image/png");
        suffixMapContentType.put(".pnm", "image/x-portable-anysuffixMapContentType");
        suffixMapContentType.put(".pntg", "image/x-macpaint");
        suffixMapContentType.put(".po", "text/x-gettext-translation");
        suffixMapContentType.put(".por", "application/x-spss-por");
        suffixMapContentType.put(".pot", "text/x-gettext-translation-template");
        suffixMapContentType.put(".ppm", "image/x-portable-pixsuffixMapContentType");
        suffixMapContentType.put(".pps", "application/vnd.ms-powerpoint");
        suffixMapContentType.put(".ppt", "application/vnd.ms-powerpoint");
        suffixMapContentType.put(".pptm", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        suffixMapContentType.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        suffixMapContentType.put(".ppz", "application/vnd.ms-powerpoint");
        suffixMapContentType.put(".prc", "application/x-palm-database");
        suffixMapContentType.put(".ps", "application/postscript");
        suffixMapContentType.put(".ps.bz2", "application/x-bzpostscript");
        suffixMapContentType.put(".ps.gz", "application/x-gzpostscript");
        suffixMapContentType.put(".psd", "image/vnd.adobe.photoshop");
        suffixMapContentType.put(".psf", "audio/x-psf");
        suffixMapContentType.put(".psf.gz", "application/x-gz-font-linux-psf");
        suffixMapContentType.put(".psflib", "audio/x-psflib");
        suffixMapContentType.put(".psid", "audio/prs.sid");
        suffixMapContentType.put(".psw", "application/x-pocket-word");
        suffixMapContentType.put(".pw", "application/x-pw");
        suffixMapContentType.put(".py", "text/x-python");
        suffixMapContentType.put(".pyc", "application/x-python-bytecode");
        suffixMapContentType.put(".pyo", "application/x-python-bytecode");
        suffixMapContentType.put(".qif", "image/x-quicktime");
        suffixMapContentType.put(".qt", "video/quicktime");
        suffixMapContentType.put(".qtif", "image/x-quicktime");
        suffixMapContentType.put(".qtl", "application/x-quicktime-media-link");
        suffixMapContentType.put(".qtvr", "video/quicktime");
        suffixMapContentType.put(".ra", "audio/vnd.rn-realaudio");
        suffixMapContentType.put(".raf", "image/x-fuji-raf");
        suffixMapContentType.put(".ram", "application/ram");
        suffixMapContentType.put(".rar", "application/x-rar");
        suffixMapContentType.put(".ras", "image/x-cmu-raster");
        suffixMapContentType.put(".raw", "image/x-panasonic-raw");
        suffixMapContentType.put(".rax", "audio/vnd.rn-realaudio");
        suffixMapContentType.put(".rb", "application/x-ruby");
        suffixMapContentType.put(".rdf", "application/rdf+xml");
        suffixMapContentType.put(".rdfs", "application/rdf+xml");
        suffixMapContentType.put(".reg", "text/x-ms-regedit");
        suffixMapContentType.put(".rej", "application/x-reject");
        suffixMapContentType.put(".rgb", "image/x-rgb");
        suffixMapContentType.put(".rle", "image/rle");
        suffixMapContentType.put(".rm", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".rmj", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".rmm", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".rms", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".rmvb", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".rmx", "application/vnd.rn-realmedia");
        suffixMapContentType.put(".roff", "text/troff");
        suffixMapContentType.put(".rp", "image/vnd.rn-realpix");
        suffixMapContentType.put(".rpm", "application/x-rpm");
        suffixMapContentType.put(".rss", "application/rss+xml");
        suffixMapContentType.put(".rt", "text/vnd.rn-realtext");
        suffixMapContentType.put(".rtf", "application/rtf");
        suffixMapContentType.put(".rtx", "text/richtext");
        suffixMapContentType.put(".rv", "video/vnd.rn-realvideo");
        suffixMapContentType.put(".rvx", "video/vnd.rn-realvideo");
        suffixMapContentType.put(".s3m", "audio/x-s3m");
        suffixMapContentType.put(".sam", "application/x-amipro");
        suffixMapContentType.put(".sami", "application/x-sami");
        suffixMapContentType.put(".sav", "application/x-spss-sav");
        suffixMapContentType.put(".scm", "text/x-scheme");
        suffixMapContentType.put(".sda", "application/vnd.stardivision.draw");
        suffixMapContentType.put(".sdc", "application/vnd.stardivision.calc");
        suffixMapContentType.put(".sdd", "application/vnd.stardivision.impress");
        suffixMapContentType.put(".sdp", "application/sdp");
        suffixMapContentType.put(".sds", "application/vnd.stardivision.chart");
        suffixMapContentType.put(".sdw", "application/vnd.stardivision.writer");
        suffixMapContentType.put(".sgf", "application/x-go-sgf");
        suffixMapContentType.put(".sgi", "image/x-sgi");
        suffixMapContentType.put(".sgl", "application/vnd.stardivision.writer");
        suffixMapContentType.put(".sgm", "text/sgml");
        suffixMapContentType.put(".sgml", "text/sgml");
        suffixMapContentType.put(".sh", "application/x-shellscript");
        suffixMapContentType.put(".shar", "application/x-shar");
        suffixMapContentType.put(".shn", "application/x-shorten");
        suffixMapContentType.put(".siag", "application/x-siag");
        suffixMapContentType.put(".sid", "audio/prs.sid");
        suffixMapContentType.put(".sik", "application/x-trash");
        suffixMapContentType.put(".sis", "application/vnd.symbian.install");
        suffixMapContentType.put(".sisx", "x-epoc/x-sisx-app");
        suffixMapContentType.put(".sit", "application/x-stuffit");
        suffixMapContentType.put(".siv", "application/sieve");
        suffixMapContentType.put(".sk", "image/x-skencil");
        suffixMapContentType.put(".sk1", "image/x-skencil");
        suffixMapContentType.put(".skr", "application/pgp-keys");
        suffixMapContentType.put(".slk", "text/spreadsheet");
        suffixMapContentType.put(".smaf", "application/x-smaf");
        suffixMapContentType.put(".smc", "application/x-snes-rom");
        suffixMapContentType.put(".smd", "application/vnd.stardivision.mail");
        suffixMapContentType.put(".smf", "application/vnd.stardivision.math");
        suffixMapContentType.put(".smi", "application/x-sami");
        suffixMapContentType.put(".smil", "application/smil");
        suffixMapContentType.put(".sml", "application/smil");
        suffixMapContentType.put(".sms", "application/x-sms-rom");
        suffixMapContentType.put(".snd", "audio/basic");
        suffixMapContentType.put(".so", "application/x-sharedlib");
        suffixMapContentType.put(".spc", "application/x-pkcs7-certificates");
        suffixMapContentType.put(".spd", "application/x-font-speedo");
        suffixMapContentType.put(".spec", "text/x-rpm-spec");
        suffixMapContentType.put(".spl", "application/x-shockwave-flash");
        suffixMapContentType.put(".spx", "audio/x-speex");
        suffixMapContentType.put(".sql", "text/x-sql");
        suffixMapContentType.put(".sr2", "image/x-sony-sr2");
        suffixMapContentType.put(".src", "application/x-wais-source");
        suffixMapContentType.put(".srf", "image/x-sony-srf");
        suffixMapContentType.put(".srt", "application/x-subrip");
        suffixMapContentType.put(".ssa", "text/x-ssa");
        suffixMapContentType.put(".stc", "application/vnd.sun.xml.calc.template");
        suffixMapContentType.put(".std", "application/vnd.sun.xml.draw.template");
        suffixMapContentType.put(".sti", "application/vnd.sun.xml.impress.template");
        suffixMapContentType.put(".stm", "audio/x-stm");
        suffixMapContentType.put(".stw", "application/vnd.sun.xml.writer.template");
        suffixMapContentType.put(".sty", "text/x-tex");
        suffixMapContentType.put(".sub", "text/x-subviewer");
        suffixMapContentType.put(".sun", "image/x-sun-raster");
        suffixMapContentType.put(".sv4cpio", "application/x-sv4cpio");
        suffixMapContentType.put(".sv4crc", "application/x-sv4crc");
        suffixMapContentType.put(".svg", "image/svg+xml");
        suffixMapContentType.put(".svgz", "image/svg+xml-compressed");
        suffixMapContentType.put(".swf", "application/x-shockwave-flash");
        suffixMapContentType.put(".sxc", "application/vnd.sun.xml.calc");
        suffixMapContentType.put(".sxd", "application/vnd.sun.xml.draw");
        suffixMapContentType.put(".sxg", "application/vnd.sun.xml.writer.global");
        suffixMapContentType.put(".sxi", "application/vnd.sun.xml.impress");
        suffixMapContentType.put(".sxm", "application/vnd.sun.xml.math");
        suffixMapContentType.put(".sxw", "application/vnd.sun.xml.writer");
        suffixMapContentType.put(".sylk", "text/spreadsheet");
        suffixMapContentType.put(".t", "text/troff");
        suffixMapContentType.put(".t2t", "text/x-txt2tags");
        suffixMapContentType.put(".tar", "application/x-tar");
        suffixMapContentType.put(".tar.bz", "application/x-bzip-compressed-tar");
        suffixMapContentType.put(".tar.bz2", "application/x-bzip-compressed-tar");
        suffixMapContentType.put(".tar.gz", "application/x-compressed-tar");
        suffixMapContentType.put(".tar.lzma", "application/x-lzma-compressed-tar");
        suffixMapContentType.put(".tar.lzo", "application/x-tzo");
        suffixMapContentType.put(".tar.xz", "application/x-xz-compressed-tar");
        suffixMapContentType.put(".tar.z", "application/x-tarz");
        suffixMapContentType.put(".tbz", "application/x-bzip-compressed-tar");
        suffixMapContentType.put(".tbz2", "application/x-bzip-compressed-tar");
        suffixMapContentType.put(".tcl", "text/x-tcl");
        suffixMapContentType.put(".tex", "text/x-tex");
        suffixMapContentType.put(".texi", "text/x-texinfo");
        suffixMapContentType.put(".texinfo", "text/x-texinfo");
        suffixMapContentType.put(".tga", "image/x-tga");
        suffixMapContentType.put(".tgz", "application/x-compressed-tar");
        suffixMapContentType.put(".theme", "application/x-theme");
        suffixMapContentType.put(".themepack", "application/x-windows-themepack");
        suffixMapContentType.put(".tif", "image/tiff");
        suffixMapContentType.put(".tiff", "image/tiff");
        suffixMapContentType.put(".tk", "text/x-tcl");
        suffixMapContentType.put(".tlz", "application/x-lzma-compressed-tar");
        suffixMapContentType.put(".tnef", "application/vnd.ms-tnef");
        suffixMapContentType.put(".tnf", "application/vnd.ms-tnef");
        suffixMapContentType.put(".toc", "application/x-cdrdao-toc");
        suffixMapContentType.put(".torrent", "application/x-bittorrent");
        suffixMapContentType.put(".tpic", "image/x-tga");
        suffixMapContentType.put(".tr", "text/troff");
        suffixMapContentType.put(".ts", "application/x-linguist");
        suffixMapContentType.put(".tsv", "text/tab-separated-values");
        suffixMapContentType.put(".tta", "audio/x-tta");
        suffixMapContentType.put(".ttc", "application/x-font-ttf");
        suffixMapContentType.put(".ttf", "application/x-font-ttf");
        suffixMapContentType.put(".ttx", "application/x-font-ttx");
        suffixMapContentType.put(".txt", "text/plain");
        suffixMapContentType.put(".txz", "application/x-xz-compressed-tar");
        suffixMapContentType.put(".tzo", "application/x-tzo");
        suffixMapContentType.put(".ufraw", "application/x-ufraw");
        suffixMapContentType.put(".ui", "application/x-designer");
        suffixMapContentType.put(".uil", "text/x-uil");
        suffixMapContentType.put(".ult", "audio/x-mod");
        suffixMapContentType.put(".uni", "audio/x-mod");
        suffixMapContentType.put(".uri", "text/x-uri");
        suffixMapContentType.put(".url", "text/x-uri");
        suffixMapContentType.put(".ustar", "application/x-ustar");
        suffixMapContentType.put(".vala", "text/x-vala");
        suffixMapContentType.put(".vapi", "text/x-vala");
        suffixMapContentType.put(".vcf", "text/directory");
        suffixMapContentType.put(".vcs", "text/calendar");
        suffixMapContentType.put(".vct", "text/directory");
        suffixMapContentType.put(".vda", "image/x-tga");
        suffixMapContentType.put(".vhd", "text/x-vhdl");
        suffixMapContentType.put(".vhdl", "text/x-vhdl");
        suffixMapContentType.put(".viv", "video/vivo");
        suffixMapContentType.put(".vivo", "video/vivo");
        suffixMapContentType.put(".vlc", "audio/x-mpegurl");
        suffixMapContentType.put(".vob", "video/mpeg");
        suffixMapContentType.put(".voc", "audio/x-voc");
        suffixMapContentType.put(".vor", "application/vnd.stardivision.writer");
        suffixMapContentType.put(".vst", "image/x-tga");
        suffixMapContentType.put(".wav", "audio/x-wav");
        suffixMapContentType.put(".wax", "audio/x-ms-asx");
        suffixMapContentType.put(".wb1", "application/x-quattropro");
        suffixMapContentType.put(".wb2", "application/x-quattropro");
        suffixMapContentType.put(".wb3", "application/x-quattropro");
        suffixMapContentType.put(".wbmp", "image/vnd.wap.wbmp");
        suffixMapContentType.put(".wcm", "application/vnd.ms-works");
        suffixMapContentType.put(".wdb", "application/vnd.ms-works");
        suffixMapContentType.put(".webm", "video/webm");
        suffixMapContentType.put(".wk1", "application/vnd.lotus-1-2-3");
        suffixMapContentType.put(".wk3", "application/vnd.lotus-1-2-3");
        suffixMapContentType.put(".wk4", "application/vnd.lotus-1-2-3");
        suffixMapContentType.put(".wks", "application/vnd.ms-works");
        suffixMapContentType.put(".wma", "audio/x-ms-wma");
        suffixMapContentType.put(".wmf", "image/x-wmf");
        suffixMapContentType.put(".wml", "text/vnd.wap.wml");
        suffixMapContentType.put(".wmls", "text/vnd.wap.wmlscript");
        suffixMapContentType.put(".wmv", "video/x-ms-wmv");
        suffixMapContentType.put(".wmx", "audio/x-ms-asx");
        suffixMapContentType.put(".wp", "application/vnd.wordperfect");
        suffixMapContentType.put(".wp4", "application/vnd.wordperfect");
        suffixMapContentType.put(".wp5", "application/vnd.wordperfect");
        suffixMapContentType.put(".wp6", "application/vnd.wordperfect");
        suffixMapContentType.put(".wpd", "application/vnd.wordperfect");
        suffixMapContentType.put(".wpg", "application/x-wpg");
        suffixMapContentType.put(".wpl", "application/vnd.ms-wpl");
        suffixMapContentType.put(".wpp", "application/vnd.wordperfect");
        suffixMapContentType.put(".wps", "application/vnd.ms-works");
        suffixMapContentType.put(".wri", "application/x-mswrite");
        suffixMapContentType.put(".wrl", "model/vrml");
        suffixMapContentType.put(".wv", "audio/x-wavpack");
        suffixMapContentType.put(".wvc", "audio/x-wavpack-correction");
        suffixMapContentType.put(".wvp", "audio/x-wavpack");
        suffixMapContentType.put(".wvx", "audio/x-ms-asx");
        suffixMapContentType.put(".x3f", "image/x-sigma-x3f");
        suffixMapContentType.put(".xac", "application/x-gnucash");
        suffixMapContentType.put(".xbel", "application/x-xbel");
        suffixMapContentType.put(".xbl", "application/xml");
        suffixMapContentType.put(".xbm", "image/x-xbitsuffixMapContentType");
        suffixMapContentType.put(".xcf", "image/x-xcf");
        suffixMapContentType.put(".xcf.bz2", "image/x-compressed-xcf");
        suffixMapContentType.put(".xcf.gz", "image/x-compressed-xcf");
        suffixMapContentType.put(".xhtml", "application/xhtml+xml");
        suffixMapContentType.put(".xi", "audio/x-xi");
        suffixMapContentType.put(".xla", "application/vnd.ms-excel");
        suffixMapContentType.put(".xlc", "application/vnd.ms-excel");
        suffixMapContentType.put(".xld", "application/vnd.ms-excel");
        suffixMapContentType.put(".xlf", "application/x-xliff");
        suffixMapContentType.put(".xliff", "application/x-xliff");
        suffixMapContentType.put(".xll", "application/vnd.ms-excel");
        suffixMapContentType.put(".xlm", "application/vnd.ms-excel");
        suffixMapContentType.put(".xls", "application/vnd.ms-excel");
        suffixMapContentType.put(".xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        suffixMapContentType.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        suffixMapContentType.put(".xlt", "application/vnd.ms-excel");
        suffixMapContentType.put(".xlw", "application/vnd.ms-excel");
        suffixMapContentType.put(".xm", "audio/x-xm");
        suffixMapContentType.put(".xmf", "audio/x-xmf");
        suffixMapContentType.put(".xmi", "text/x-xmi");
        suffixMapContentType.put(".xml", "application/xml");
        suffixMapContentType.put(".xpm", "image/x-xpixsuffixMapContentType");
        suffixMapContentType.put(".xps", "application/vnd.ms-xpsdocument");
        suffixMapContentType.put(".xsl", "application/xml");
        suffixMapContentType.put(".xslfo", "text/x-xslfo");
        suffixMapContentType.put(".xslt", "application/xml");
        suffixMapContentType.put(".xspf", "application/xspf+xml");
        suffixMapContentType.put(".xul", "application/vnd.mozilla.xul+xml");
        suffixMapContentType.put(".xwd", "image/x-xwindowdump");
        suffixMapContentType.put(".xyz", "chemical/x-pdb");
        suffixMapContentType.put(".xz", "application/x-xz");
        suffixMapContentType.put(".w2p", "application/w2p");
        suffixMapContentType.put(".z", "application/x-compress");
        suffixMapContentType.put(".zabw", "application/x-abiword");
        suffixMapContentType.put(".zip", "application/zip");
        suffixMapContentType.put(".zoo", "application/x-zoo");
        suffixMapContentType.put(".woff", "application/font-woff");
    }
}
