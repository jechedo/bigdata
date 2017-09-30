package cn.skyeye.common.net;

import com.google.common.base.Preconditions;
import org.apache.http.Consts;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/6/3 13:40
 */
public class HttpGetURL {

    private StringBuffer url;
    private Charset charset = Consts.UTF_8;
    private boolean hasParam = false;

    public HttpGetURL(String url){
        this(url, null);
    }

    public HttpGetURL(String url, Charset charset){

        Preconditions.checkNotNull(url, "url 不能为空。");

        if(url.endsWith(Https.PARAMETER_SEPARATOR)){
            url = url.substring(0, url.length() - 1);
        }

        this.url = new StringBuffer(url);
        if(!url.contains("?")){
            this.url.append("?");
        }

        if(charset != null)this.charset = charset;
    }


    public void addParam(String name, String value){
        if(name != null && value != null) {

            String encodedName = Https.encodeFormFields(name, this.charset);
            String encodedValue = Https.encodeFormFields(value, this.charset);

            if(this.hasParam){
                this.url.append(Https.PARAMETER_SEPARATOR);
            }else {
                this.hasParam = true;
            }

            this.url.append(encodedName).append(Https.NAME_VALUE_SEPARATOR).append(encodedValue);

        }
    }

    public void addParams(Map params){
        if(params != null){
            Set<Map.Entry> entrySet = params.entrySet();
            for (Map.Entry entry : entrySet) {
                addParam(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }

    public String getURL(){
        return url.toString();
    }
}
