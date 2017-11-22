package cn.skyeye.common.hash;

public class HexUtil {
    /* 
     * Convert byte[] to hex 
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。 
     *  
     * @param src byte[] data 
     *  
     * @return hex string 
     */  
    public static String bytesToHexString(byte[] src) {  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString().toUpperCase();  
    }  
    /** 
     * Convert hex string to byte[] 
     *  
     * @param hexStr
     *            the hex string 
     * @return byte[] 
     */  
    public static byte[] hexStringToBytes(String hexStr) {  
        int index = 0;  
        if (hexStr == null || hexStr.equals("")) {  
            return null;  
        }  
        if (hexStr.startsWith("0x", index) || hexStr.startsWith("0X", index)) {  
            index += 2;  
        }  
        hexStr = hexStr.substring(index).toUpperCase();  
        int length = hexStr.length() / 2;  
        char[] hexChars = hexStr.toCharArray();  
        byte[] d = new byte[length];  
        for (int i = 0; i < length; i++) {  
            int pos = i * 2;  
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
        }  
        return d;  
    }  
    /** 
     * Convert char to byte 
     * @param c 
     * char 
     * @return byte 
     */  
    private static byte charToByte(char c) {  
        byte b = (byte) "0123456789ABCDEF".indexOf(c);  
        return b;  
    }  
  
    // 将指定byte数组以16进制的形式打印到控制台  
    public static void printHexString(byte[] b) {  
        for (int i = 0; i < b.length; i++) {  
            String hex = Integer.toHexString(b[i] & 0xFF);  
            if (hex.length() == 1) {  
                hex = '0' + hex;  
            }  
            System.out.print(hex.toUpperCase());  
        }  
    }  
    public static String bytes2HexString(byte[] b) {  
        String ret = "";  
        for (int i = 0; i < b.length; i++) {  
            String hex = Integer.toHexString(b[i] & 0xFF);  
            if (hex.length() == 1) {  
                hex = '0' + hex;  
            }  
            ret += hex.toUpperCase();  
        }  
        return ret;  
    }  
    public static void main(String[] args) {  
        String hex = "EFEF";  
        byte[] a = hexStringToBytes(hex);  
        String b = bytesToHexString(a);  
        if(b.equals(hex)){  
            System.out.println("ok");  
        }else{  
            System.out.println("no");  
        }  
    }  
}  