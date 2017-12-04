package cn.skyeye.common.reflect;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public final class ExtLibsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ExtLibsLoader.class);

    /** URLClassLoader的addURL方法 */
    private volatile static Method addURL = initAddMethod();

    private static URLClassLoader classloader
            = (URLClassLoader) ClassLoader.getSystemClassLoader();

    /**
     * 初始化addUrl 方法.
     * @return 可访问addUrl方法的Method对象
     */
    private static Method initAddMethod() {
        try {
            Method add = URLClassLoader.class
                    .getDeclaredMethod("addURL", new Class[] { URL.class });
            add.setAccessible(true);
            return add;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param libsPaths
     * @see "loadJars2Classpath"
     */
    @Deprecated
    public static void loadLibs2Classpath(Collection<String> libsPaths) {
        if(libsPaths != null) {
            for (String f : libsPaths) {
                loadLibs2Classpath(f);
            }
        }
    }

    public static void loadJars2Classpath(Collection<String> jarsPaths) throws Exception {
        if(jarsPaths != null) {
            for (String f : jarsPaths) {
                loadJars2Classpath(f);
            }
        }
    }

    /**
     * @param resPaths
     * @see "loadRes2Classpath2"
     */
    @Deprecated
    public static void loadRes2Classpath(Collection<String> resPaths) {

        if(resPaths != null) {
            for (String r : resPaths) {
                loadRes2Classpath(r);
            }
        }
    }

    public static void loadRes2Classpath2(Collection<String> resPaths) throws Exception {

        if(resPaths != null) {
            for (String r : resPaths) {
                loadRes2Classpath2(r);
            }
        }
    }

    /**
     * @param libsPath
     * @see "loadJars2Classpath"
     */
    @Deprecated
    public static void loadLibs2Classpath(String libsPath) {
        File file = new File(libsPath);
        try {
            loopFiles(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将给定 目录下的所有jar文件 或 单个jar 加载到 classpath
     * @param jarsPath
     */
    public static void loadJars2Classpath(String jarsPath) throws Exception {
        File file = new File(jarsPath);
        loopFiles(file);

    }

    /**
     * @param resPath
     * @see "loadRes2Classpath2"
     */
    @Deprecated
    public static void loadRes2Classpath(String resPath) {
        File file = new File(resPath);
        try {
            loopDirs(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRes2Classpath2(String resPath) throws Exception {
        File file = new File(resPath);
        loopDirs(file);

    }

    /**    
     * 循环遍历目录，找出所有的资源路径。
     * @param file 当前遍历文件
     */
    private static void loopDirs(File file) throws Exception {
        // 资源文件只加载路径
        if (file.isDirectory()) {
            loadURL(file);
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopDirs(tmp);
            }
        }
    }

    /**    
     * 循环遍历目录，找出所有的jar包。
     * @param file 当前遍历文件
     */
    private static void loopFiles(File file) throws Exception {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp);
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar")
                    || file.getAbsolutePath().endsWith(".zip")) {
                loadURL(file);
            }
        }
    }

    /**
     * 通过filepath加载文件到classpath。
     * @param file 文件路径
     * @return URL
     * @throws Exception 异常
     */
    private static void addURL(File file) {
        try {
            addURL.invoke(classloader, new Object[] { file.toURI().toURL() });
        } catch (Exception e) {
            LOG.error(null, e);
        }
    }

    private static void loadURL(File file) throws Exception {
        getAddURLMethod();
        addURL.invoke(classloader, new Object[] { file.toURI().toURL() });
    }

    public static void loadURL(URL url) throws Exception {
        getAddURLMethod();
        addURL.invoke(classloader, new Object[] { url});
    }

    private static void getAddURLMethod() {
        if(addURL == null){
            synchronized (ExtLibsLoader.class){
                if(addURL == null){
                    addURL = initAddMethod();
                }
            }
            Preconditions.checkNotNull(addURL, "URLClassLoader的addURL方法 获取为空。");
        }
    }
}