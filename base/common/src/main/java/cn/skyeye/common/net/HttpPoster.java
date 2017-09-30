package cn.skyeye.common.net;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *   http post 请求。
 * @author LiXiaoCong
 * @version 2017/6/3 11:14
 */
public class HttpPoster {

    private String content_type_text_json = "text/json";
    private Charset charset = Consts.UTF_8;

    private HttpPost post;
    private Map params;

    private boolean useJson;

    public HttpPoster(String url){
       this(url, false);
    }

    public HttpPoster(String url, boolean useJson){

        Preconditions.checkNotNull(url, "url 不能为空。");

        this.post = new HttpPost(url);
        this.useJson = useJson;
        this.params = new HashMap();
    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    public void setParams(Map params){
        if(params != null)this.params.putAll(params);
    }

    public void addHandler(String name, String value){
        this.post.addHeader(name, value);
    }

    public void setHandler(String name, String value){
        this.post.setHeader(name, value);
    }

    public void addParam(Object key, Object value){
      if(key != null && value != null){
          this.params.put(key, value);
      }
    }

    public String execute() throws Exception {

        this.post.setEntity(createHttpEntity());
        return Https.execute(this.post);
    }

    private HttpEntity createHttpEntity(){
        String str;
        if(useJson){
            str = JSON.toJSONString(params);
        }else {
            str = Https.format(params, charset);
        }

        StringEntity entity = new StringEntity(str, ContentType.create(Https.CONTENT_TYPE, charset));
        if(useJson)entity.setContentType(content_type_text_json);

        return entity;
    }

    public static void main(String[] args) throws Exception {

        String url = "http://hadoop-1:8082/consumers/restdemo";
        HttpPoster poster = new HttpPoster(url, true);

        poster.addParam("name", "rest01");
        //params.put("format", "json");
        poster.addParam("auto.offset.reset", "smallest");
        poster.addParam("auto.commit.enable", "true");

        poster.addHandler("Content-Type", "application/vnd.kafka.json.v1+json");
        poster.addHandler("Accept", "application/vnd.kafka.v1+json, application/vnd.kafka+json, application/json");

        String execute = poster.execute();
        System.out.println(execute);

    }

}
