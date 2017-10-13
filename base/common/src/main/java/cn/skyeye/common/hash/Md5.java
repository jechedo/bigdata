package cn.skyeye.common.hash;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
	
	public static String  Md5_32( String sourceStr ){
		
		StringBuffer buf = new StringBuffer();

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {}

        md.update(sourceStr.getBytes());
        byte b[] = md.digest();
        int i;
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
	  
	}
	public static String  Md5_16( String sourceStr ) throws NoSuchAlgorithmException{
		return Md5_32(sourceStr).substring(8, 24);
	}
}