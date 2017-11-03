package cn.skyeye.kafka.ganglia;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/3 11:24
 */
public class Utils {
    private Utils(){}

    public static List<String> executeCommand(String host, int port, String command, int timeoutms) throws IOException{

        List<String> res = Lists.newLinkedList();
        Socket socket = null;
        OutputStream outputStream = null;
        PrintWriter out = null;
        BufferedReader in = null;
        InputStream inputStream = null;
        try{
            socket = newSocket(host, port, timeoutms);
            outputStream = socket.getOutputStream();
            out = new PrintWriter(outputStream, true);

            inputStream = socket.getInputStream();
            in =  new BufferedReader(new InputStreamReader(inputStream));

            out.println(command);
            String line = in.readLine();
            while(line != null){
                res.add(line);
                line = in.readLine();
            }
        } finally{
            Closeables.close(out, true);
            Closeables.close(in, true);
            Closeables.close(inputStream, true);
            Closeables.close(outputStream, true);
            Closeables.close(socket, true);
        }
        return res;
    }

    public static Socket newSocket(String host, int port, int timeoutms) throws IOException{
        Socket socket = new Socket(host, port);
        socket.setSoTimeout(timeoutms);  //timeout on the socket
        return socket;
    }
}
