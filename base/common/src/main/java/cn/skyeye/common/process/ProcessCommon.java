package cn.skyeye.common.process;

import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
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
public class ProcessCommon {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessCommon.class);

    private  ProcessCommon(){}

    public static void printProcessMSG(Process process){
        printProcessMSG(process,
                new PrintProcessRowExtracter(PrintProcessRowExtracter.PrintLevel.SYS_OUT));
    }

    public static void printProcessMSG(Process process, ProcessRowExtracter extracter){

        Preconditions.checkNotNull(process, "process 不能为 null !");
        if(extracter == null){
            extracter = new PrintProcessRowExtracter(PrintProcessRowExtracter.PrintLevel.SYS_OUT);
        }

        InputStreamWathThread iwt = new InputStreamWathThread(process.getInputStream(), extracter);
        iwt.start();
        InputStreamWathThread eiwt = new InputStreamWathThread(process.getErrorStream(), extracter);
        eiwt.start();
        try{
            process.waitFor();
            iwt.setOver(true);
            eiwt.setOver(true);
            LOG.info("进程结束，输出任务完毕。");
        }catch(InterruptedException e){
            LOG.error(null ,e);
        }
    }

    static class InputStreamWathThread extends Thread{

        private InputStream is;
        private ProcessRowExtracter extracter;
        private boolean over = false;

        public InputStreamWathThread(InputStream is, ProcessRowExtracter extracter){
            this.is = is;
            this.extracter = extracter;
            over = false;
        }
        public void run(){
            try{
                String temp;
                InputStreamReader in = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(in);
                while(true){
                    if(is == null || over)break;
                    while((temp = br.readLine()) != null){
                       extracter.extractRowData(temp);
                    }
                }
                in.close();
            }catch(Exception e){
                LOG.error(null,e);
            }finally {
               if(is != null) IOUtils.closeQuietly(is);
            }
        }
        public void setOver(boolean over){this.over = over;}
    }
}
