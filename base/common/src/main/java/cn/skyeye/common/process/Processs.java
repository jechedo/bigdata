package cn.skyeye.common.process;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Description:
 *
 * 进程相关
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/13 10:10
 */
public class Processs {

    private static final Logger LOG = Logger.getLogger(Processs.class);

    private Processs(){}

    public static void printOutMsg(Process process){
        extractOutMsg(process,
                new PrintLineExtracter(PrintLineExtracter.PrintLevel.SYS_OUT));
    }

    public static void extractOutMsg(Process process, LineExtracter extracter){

        Preconditions.checkNotNull(process, "process 不能为 null !");
        if(extracter == null){
            extracter = new PrintLineExtracter(PrintLineExtracter.PrintLevel.SYS_OUT);
        }

        InputStreamExtractTask iwt = new InputStreamExtractTask(process.getInputStream(), false, extracter);
        iwt.start();
        InputStreamExtractTask eiwt = new InputStreamExtractTask(process.getErrorStream(), true, extracter);
        eiwt.start();
        try{
            process.waitFor();
        }catch(InterruptedException e){
            LOG.error(null ,e);
        }finally {
            iwt.setOver(true);
            eiwt.setOver(true);
            LOG.info("进程结束，输出任务完毕。");
        }
    }


    public static void extractOutMsg(Session session, long timeOut, LineExtracter extracter){

        Preconditions.checkNotNull(session, "Session 不能为 null !");
        if(extracter == null){
            extracter = new PrintLineExtracter(PrintLineExtracter.PrintLevel.SYS_OUT);
        }

        InputStreamExtractTask iwt = new InputStreamExtractTask(new StreamGobbler(session.getStdout()), false, extracter);
        iwt.start();
        InputStreamExtractTask eiwt = new InputStreamExtractTask(new StreamGobbler(session.getStderr()), true, extracter);
        eiwt.start();
        try{
            session.waitForCondition(ChannelCondition.EXIT_STATUS, timeOut);
        }catch(IOException e){
            LOG.error(null ,e);
        }finally {
            iwt.setOver(true);
            eiwt.setOver(true);
            LOG.info("进程结束，输出任务完毕。");
        }
    }


    static class InputStreamExtractTask extends Thread{

        private InputStream is;
        private LineExtracter extracter;
        private boolean isError;
        private boolean over = false;

        public InputStreamExtractTask(InputStream is, boolean isError, LineExtracter extracter){
            this.is = is;
            this.isError = isError;
            this.extracter = extracter;
            over = false;
        }
        public void run(){
            String temp;
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(in);
            try{
                while(true){
                    if(is == null || over)break;
                    while((temp = br.readLine()) != null){
                       extracter.extract(isError, temp);
                    }
                }
            }catch(Exception e){
                LOG.error(null,e);
            }finally {
              Closeables.closeQuietly(br);
              Closeables.closeQuietly(in);
              Closeables.closeQuietly(is);
            }
        }
        public void setOver(boolean over){this.over = over;}
    }
}
