package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.tools.DirAndFile;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * DirAndFile 目录遍历测试。
 *
 * iteratorPath 递归收集目录下所有文件路径到 pathName 列表，跳过子目录本身。
 * 该方法内部 try-catch 吞掉异常，因此对不存在的目录不应抛出。
 */
public class DirAndFileTest {

    private static final String TMP_BASE = "target/dirandfile-test";

    @Test
    public void iteratorPath_collectsAllFilesRecursively() throws Exception {
        Path root = Paths.get(TMP_BASE, "tree");
        Files.createDirectories(root);
        Files.createDirectories(root.resolve("sub"));
        Files.createDirectories(root.resolve("sub/deep"));

        Path f1 = root.resolve("a.txt");
        Path f2 = root.resolve("sub/b.txt");
        Path f3 = root.resolve("sub/deep/c.txt");
        Files.write(f1, "a".getBytes());
        Files.write(f2, "b".getBytes());
        Files.write(f3, "c".getBytes());

        DirAndFile daf = new DirAndFile();
        daf.iteratorPath(root.toString());

        List<String> paths = daf.getPathName();
        assertEquals("应收集 3 个文件", 3, paths.size());
        assertTrue("应包含 a.txt: " + paths, containsSuffix(paths, "a.txt"));
        assertTrue("应包含 b.txt: " + paths, containsSuffix(paths, "b.txt"));
        assertTrue("应包含 c.txt: " + paths, containsSuffix(paths, "c.txt"));
    }

    @Test
    public void iteratorPath_emptyDirectory_returnsEmptyList() throws Exception {
        Path root = Paths.get(TMP_BASE, "empty");
        Files.createDirectories(root);

        DirAndFile daf = new DirAndFile();
        daf.iteratorPath(root.toString());
        assertTrue("空目录应返回空列表", daf.getPathName().isEmpty());
    }

    @Test
    public void iteratorPath_nonExistentDirectory_doesNotThrow() {
        DirAndFile daf = new DirAndFile();
        // 不存在的目录：listFiles 返回 null，方法内部已处理，不应抛出异常
        daf.iteratorPath(TMP_BASE + "/no-such-dir-" + System.nanoTime());
        assertTrue(daf.getPathName().isEmpty());
    }

    @Test
    public void getFiles_returnsListedFiles() throws Exception {
        Path root = Paths.get(TMP_BASE, "files");
        Files.createDirectories(root);
        Files.write(root.resolve("x.txt"), "x".getBytes());

        DirAndFile daf = new DirAndFile();
        daf.iteratorPath(root.toString());
        File[] files = daf.getFiles();
        assertTrue("getFiles 应非 null", files != null);
        assertTrue("应至少包含 1 个条目", files.length >= 1);
    }

    @Test
    public void getOr_returnsRootFile() throws Exception {
        Path root = Paths.get(TMP_BASE, "or");
        Files.createDirectories(root);

        DirAndFile daf = new DirAndFile();
        daf.iteratorPath(root.toString());
        assertEquals(new File(root.toString()), daf.getOr());
    }

    @Test
    public void setters_storeValues() {
        DirAndFile daf = new DirAndFile();
        File f = new File("/tmp");
        daf.setOr(f);
        daf.setFiles(new File[]{f});
        assertEquals(f, daf.getOr());
        assertEquals(1, daf.getFiles().length);
        assertFalse("pathName 初始非 null", daf.getPathName() == null);
    }

    private static boolean containsSuffix(List<String> paths, String suffix) {
        for (String p : paths) {
            if (p.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
