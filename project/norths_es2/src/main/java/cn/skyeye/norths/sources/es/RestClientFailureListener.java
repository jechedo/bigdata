package cn.skyeye.norths.sources.es;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 14:42
 */

public class RestClientFailureListener /* extends RestClient.FailureListener*/ {

    /*-
    private final Logger logger = Logger.getLogger(RestClientFailureListener.class);

    private RestClient.FailureListener subFailureListener;

    public void setSubFailureListener(RestClient.FailureListener subFailureListener){
        this.subFailureListener = subFailureListener;
    }

    @Override
    public void onFailure(HttpHost host) {
       logger.error(String.format("es rest client: %s:%s 连接失败。", host.getHostName(), host.getPort()));
       if(subFailureListener != null){
           subFailureListener.onFailure(host);
       }
    }*/
}
