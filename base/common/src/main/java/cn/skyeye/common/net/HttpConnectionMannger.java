package cn.skyeye.common.net;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/6/3 11:08
 */
public class HttpConnectionMannger {

    public static HttpConnectionMannger instance = new HttpConnectionMannger();

    private volatile DefaultHttpClient httpclient;

    private HttpConnectionMannger(){}

    /**
     * @param rTimeOut 请求超时 ms
     * @param sTimeOut 等待数据超时 ms
     * @return
     */
    private DefaultHttpClient getOrNewHttpClient(Integer rTimeOut,Integer sTimeOut) {
        if(httpclient == null) {
            synchronized (HttpConnectionMannger.class) {
                if(httpclient == null) {
                    PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
                    connectionManager.setMaxTotal(200);
                    connectionManager.setDefaultMaxPerRoute(20);

                    BasicHttpParams httpParams = new BasicHttpParams();
                    if(rTimeOut != null && rTimeOut > 0)
                        HttpConnectionParams.setConnectionTimeout(httpParams, rTimeOut);
                    if(sTimeOut != null && sTimeOut > 0)
                        HttpConnectionParams.setSoTimeout(httpParams, sTimeOut);

                    httpclient = new DefaultHttpClient(connectionManager, httpParams);
                }
            }
        }
        return httpclient;
    }

    public DefaultHttpClient getHttpclient(){
        return getOrNewHttpClient(null, null);
    }

    public DefaultHttpClient getHttpclient(int rTimeOut,int sTimeOut){
        return getOrNewHttpClient(rTimeOut, sTimeOut);
    }

}
