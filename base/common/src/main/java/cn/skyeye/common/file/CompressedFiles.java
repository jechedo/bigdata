package cn.skyeye.common.file;

import cn.skyeye.common.logging.DynamicLogFactory;
import com.github.junrar.extract.ExtractArchive;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;

public class CompressedFiles {

    private static final int BUFFEREDSIZE = 1024;

    private static Logger LOG;
    private static CompressedFiles com;

    private CompressedFiles(Logger log){
       LOG = log;
        if(LOG == null)
            LOG = Logger.getLogger(CompressedFiles.class);
    }

    public static CompressedFiles get(){
       return get(null);
    }

    public static CompressedFiles get(Logger log){
        if(com == null){
            synchronized (CompressedFiles.class){
                if(com == null)com = new CompressedFiles(log);
            }
        }
        return com;
    }

    public boolean unzipFile(String zipFilePath, String descDir){
        return unzipFile(zipFilePath, descDir, false);
    }

    public boolean unzipFile(String zipFilePath, String descDir, boolean overWrite){

        Preconditions.checkArgument(zipFilePath.endsWith(".zip"),
                String.format("要解压的文件不是标准的zip压缩文件"));
        File src = checkSrcFile(zipFilePath);
        File dir = checkDescDir(descDir, src, overWrite);

        try {
            Project proj = new Project();
            Expand expand = new Expand();
            expand.setProject(proj);
            expand.setTaskType("unzip");
            expand.setTaskName("unzip");

            if(System.getProperty ("os.name").startsWith("Windows"))
            expand.setEncoding("GBK");

            expand.setSrc(src);
            expand.setDest(dir);
            expand.execute();

            return true;
        } catch(Exception e) {
           LOG.error(null, e);
        }
        return false;
    }

    private File checkDescDir(String descDir, File src, boolean overWrite){
        File dir;
        if(StringUtils.isBlank(descDir)){
            String name = src.getName();
            dir = new File(src.getParentFile(),
                    name.substring(0, name.lastIndexOf(".")));
        }else {
            dir = new File(descDir);
        }

        boolean exist = Files.exist(dir);

        if(exist && !overWrite) {
            throw  new IllegalArgumentException(
                    String.format("要解压的文件 %s 的目标目录 %s 已存在。",
                            src.getAbsolutePath(), dir.getAbsolutePath()));
        }else {
            if(!exist)dir.mkdirs();
        }
        return dir;
    }

    private File checkSrcFile(String path) {

        Preconditions.checkArgument(StringUtils.isNotBlank(path), "要解压的文件路径不能为空。");

        File f = new File(path);
        Preconditions.checkArgument(Files.exist(f), String.format("要解压的文件 %s 不存在。", path));
        Preconditions.checkArgument(f.isFile(), String.format("要解压的路径 %s 是一个目录。", path));
        if(f.length() == 0){
            LOG.warn(String.format("需要解压的zip文件: %s 的长度为为空。", path));
        }
        return f;
    }

    /** 
     * 压缩zip格式的压缩文件 
     * @param zipFilename 输出文件名称及详细路径
     * @throws IOException 
     */  
    public void zip(File destDir, File zipFilename) throws IOException {

        Project proj = new Project();
        FileSet fileSet = new FileSet();
        fileSet.setProject(proj);

        // 判断是目录还是文件
        if (destDir.isDirectory()) {
            fileSet.setDir(destDir);
            // ant中include/exclude规则在此都可以使用
            // 比如:
            // fileSet.setExcludes("**/*.txt");
            // fileSet.setIncludes("**/*.xls");
        } else {
            fileSet.setFile(destDir);
        }

        Zip zip = new Zip();
        zip.setProject(proj);
        zip.setDestFile(zipFilename);
        zip.addFileset(fileSet);

        if(System.getProperty ("os.name").startsWith("Windows"))
            zip.setEncoding("GBK");

        zip.execute();
    }  


     /** 
     * 解压rar格式的压缩文件到指定目录下 
     * @param rarFilePath 压缩文件
     * @param descDir 解压目录
     * @throws Exception 
     */  
    public boolean unrar(String rarFilePath, String descDir, boolean overWrite){

        Preconditions.checkArgument(rarFilePath.endsWith(".rar"),
                String.format("要解压的文件不是标准的rar压缩文件"));
        File src = checkSrcFile(rarFilePath);
        File dir = checkDescDir(descDir, src, overWrite);

        try {
            ExtractArchive extractArchive = new ExtractArchive();
            extractArchive.extractArchive(src, dir);
            return  true;
        } catch (Exception e) {
            LOG.error(null, e);
        }
        return false;
    }

    public boolean unrar(String rarFilePath, String descDir){
       return unrar(rarFilePath, descDir, false);
    }
      

    public static void main(String[] args) throws Exception {  
        CompressedFiles decompression= CompressedFiles.get(DynamicLogFactory.getLogger("test", "D:/logs"));
        System.out.println(decompression.unzipFile("D:/demo/离职总结-2016.10.27.zip", null));
        System.out.println(decompression.unrar("D:/demo/demo.rar", null));
          
    }
}