package cn.skyeye.common;

import java.io.File;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/9/30 16:28
 */
public class SysEnvs {
    private SysEnvs(){}

    public static String getJarFilePathByClass(String clazz) throws ClassNotFoundException {
        return getJarFilePathByClass(Class.forName(clazz));
    }

    public static String getJarFileDirByClass(String clazz) throws ClassNotFoundException {
        return getJarFileDirByClass(Class.forName(clazz));
    }

    public static String getJarFilePathByClass(Class<?> clazz){
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getFile()).getAbsolutePath();
    }

    public static String getJarFileDirByClass(Class<?> clazz){
        return new File(getJarFilePathByClass(clazz)).getParent();
    }

    public static String getEnv(String key){
        return System.getenv(key);
    }

    public static String getSystemArch(){
        return  System.getProperty("os.arch");
    }
    public static String getSystemOSName(){
        return  System.getProperty("os.name");
    }
    public static String getSystemPath(){
        return  System.getProperty("java.library.path");
    }

    public static String getSystemJAVAHome(){
        return  System.getProperty("sun.boot.library.path");
    }
}
