package com.github.zhizuqiu.nettyrestful.server.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirAndFile {

    private File or;
    private File[] files;
    private List<String> pathName = new ArrayList<>();

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public List<String> getPathName() {
        return pathName;
    }

    public void setPathName(List<String> pathName) {
        this.pathName = pathName;
    }

    public File getOr() {

        return or;
    }

    public void setOr(File or) {
        this.or = or;
    }

    public void iteratorPath(String dir) {
        try {
            or = new File(dir);
            files = or.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        pathName.add(file.getPath());
                    } else if (file.isDirectory()) {
                        iteratorPath(file.getPath());
                    }
                }
            }
        } catch (Exception e) {
            //
        }
    }
}
