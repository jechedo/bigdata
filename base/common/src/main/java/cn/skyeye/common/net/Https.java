package cn.skyeye.common.net;

import com.google.common.base.Preconditions;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/6/3 10:27
 */
public class Https {

    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String PARAMETER_SEPARATOR = "&";
    public static final String NAME_VALUE_SEPARATOR = "=";

    private static final BitSet URLENCODER   = new BitSet(256);
    private static final int RADIX = 16;

    private Https(){}

    public static HttpPoster post(String url){
        return new HttpPoster(url);
    }

    public static HttpPoster post(String url, boolean useJson){
        return new HttpPoster(url, useJson);
    }

    public static String get(String url) throws Exception {
        Preconditions.checkNotNull(url, "url 不能为空。");
        return execute(new HttpGet(url));
    }


    static String execute(HttpUriRequest request) throws Exception {

        HttpResponse response = HttpConnectionMannger.instance.getHttpclient().execute(request);
        HttpEntity entity = response.getEntity();

        int statusCode = response.getStatusLine().getStatusCode();
        String s = EntityUtils.toString(entity);

        if (statusCode >= 300) {
            throw new Exception(String.format("执行失败：%s, statusCode = %s, msg = %s", request, statusCode, s));
        }
        return s;
    }

    public static String format (
            final Map parameters,
            final Charset charset) {
        final StringBuilder result = new StringBuilder();

        Set<Map.Entry> entrySet = parameters.entrySet();
        for (Map.Entry entry : entrySet) {

            final String encodedName = encodeFormFields(String.valueOf(entry.getKey()), charset);
            final String encodedValue = encodeFormFields(String.valueOf(entry.getValue()), charset);
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }


    public static String encodeFormFields (final String content, final Charset charset) {
        if (content == null) {
            return null;
        }
        return urlencode(content, charset != null ? charset : Consts.UTF_8, URLENCODER, true);
    }

    private static String urlencode(
            final String content,
            final Charset charset,
            final BitSet safechars,
            final boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }

}
