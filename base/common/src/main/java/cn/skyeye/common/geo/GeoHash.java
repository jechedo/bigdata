package cn.skyeye.common.geo;

import java.util.Arrays;
import java.util.HashMap;

public class GeoHash {
    private LocationBean location;
    /**
     * 1 2500km;2 630km;3 78km;4 30km
     * 5 2.4km; 6 610m; 7 76m; 8 19m
     */
    private int hashLength; //经纬度转化为geohash长度
    private int latLength = 15; //纬度转化为二进制长度
    private int lngLength = 15; //经度转化为二进制长度

    private double minLat;//每格纬度的单位大小
    private double minLng;//每格经度的单位大小
    private static final char[] CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static HashMap<Character, Integer> CHARSMAP;

    static {
        CHARSMAP = new HashMap<Character, Integer>();
        for (int i = 0; i < CHARS.length; i++) {
            CHARSMAP.put(CHARS[i], i);
        }
    }

    public GeoHash(double lat, double lng, int hashLength) {
        location = new LocationBean(lat, lng);
        this.hashLength = hashLength;
        setMinLatLng();
    }

    public int getHashLength() {
        return hashLength;
    }

    /**
     * @Author:lulei
     * @Description: 设置经纬度的最小单位
     */
    private void setMinLatLng() {
        minLat = LocationBean.MAXLAT - LocationBean.MINLAT;
        for (int i = 0; i < latLength; i++) {
            minLat /= 2.0;
        }
        minLng = LocationBean.MAXLNG - LocationBean.MINLNG;
        for (int i = 0; i < lngLength; i++) {
            minLng /= 2.0;
        }
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLng() {
        return minLng;
    }

    /**
     * @param length
     * @Description: 设置经纬度转化为geohash长度
     */
    public boolean setHashLength(int length) {
        if (length < 1) {
            return false;
        }
        hashLength = length;
        latLength = (length * 5) / 2;
        if (length % 2 == 0) {
            lngLength = latLength;
        } else {
            lngLength = latLength + 1;
        }
        setMinLatLng();
        return true;
    }

    /**
     * @Description: 获取经纬度的base32字符串
     */
    public String getGeoHashBase32() {
        return getGeoHashBase32(location.getLat(), location.getLng());
    }

    /**
     * @param lat
     * @param lng
     * @Description: 获取经纬度的base32字符串
     */
    private String getGeoHashBase32(double lat, double lng) {
        boolean[] bools = getGeoBinary(lat, lng);
        if (bools == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bools.length; i = i + 5) {
            boolean[] base32 = new boolean[5];
            for (int j = 0; j < 5; j++) {
                base32[j] = bools[i + j];
            }
            char cha = getBase32Char(base32);
            if (' ' == cha) {
                return null;
            }
            sb.append(cha);
        }
        return sb.toString();
    }

    /**
     * @param base32
     * @Description: 将五位二进制转化为base32
     */
    private char getBase32Char(boolean[] base32) {
        if (base32 == null || base32.length != 5) {
            return ' ';
        }
        int num = 0;
        for (boolean bool : base32) {
            num <<= 1;
            if (bool) {
                num += 1;
            }
        }
        return CHARS[num % CHARS.length];
    }

    /**
     * @param i
     * @Description: 将数字转化为二进制字符串
     */
    private String getBase32BinaryString(int i) {
        if (i < 0 || i > 31) {
            return null;
        }
        String str = Integer.toBinaryString(i + 32);
        return str.substring(1);
    }

    /**
     * @param geoHash
     * @Description: 将geoHash转化为二进制字符串
     */
    private String getGeoHashBinaryString(String geoHash) {
        if (geoHash == null || "".equals(geoHash)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < geoHash.length(); i++) {
            char c = geoHash.charAt(i);
            if (CHARSMAP.containsKey(c)) {
                String cStr = getBase32BinaryString(CHARSMAP.get(c));
                if (cStr != null) {
                    sb.append(cStr);
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param geoHash
     * @Description: 返回geoHash 对应的坐标
     */
    public LocationBean getLocation(String geoHash) {
        String geoHashBinaryStr = getGeoHashBinaryString(geoHash);
        if (geoHashBinaryStr == null) {
            return null;
        }
        StringBuffer lat = new StringBuffer();
        StringBuffer lng = new StringBuffer();
        for (int i = 0; i < geoHashBinaryStr.length(); i++) {
            if (i % 2 != 0) {
                lat.append(geoHashBinaryStr.charAt(i));
            } else {
                lng.append(geoHashBinaryStr.charAt(i));
            }
        }
        double latValue = getGeoHashMid(lat.toString(), LocationBean.MINLAT, LocationBean.MAXLAT);
        double lngValue = getGeoHashMid(lng.toString(), LocationBean.MINLNG, LocationBean.MAXLNG);
        LocationBean location = new LocationBean(latValue, lngValue);
        //location.setGeoHash(geoHash);
        return location;
    }

    /**
     * @param binaryStr
     * @param min
     * @param max
     * @Description: 返回二进制对应的中间值
     */
    private double getGeoHashMid(String binaryStr, double min, double max) {
        if (binaryStr == null || binaryStr.length() < 1) {
            return (min + max) / 2.0;
        }
        if (binaryStr.charAt(0) == '1') {
            return getGeoHashMid(binaryStr.substring(1), (min + max) / 2.0, max);
        } else {
            return getGeoHashMid(binaryStr.substring(1), min, (min + max) / 2.0);
        }
    }

    /**
     * @param lat
     * @param lng
     * @return
     * @Author:lulei
     * @Description: 获取坐标的geo二进制字符串
     */
    private boolean[] getGeoBinary(double lat, double lng) {
        boolean[] latArray = getHashArray(lat, LocationBean.MINLAT, LocationBean.MAXLAT, latLength);
        boolean[] lngArray = getHashArray(lng, LocationBean.MINLNG, LocationBean.MAXLNG, lngLength);
        return merge(latArray, lngArray);
    }

    /**
     * @param latArray
     * @param lngArray
     * @Description: 合并经纬度二进制
     */
    private boolean[] merge(boolean[] latArray, boolean[] lngArray) {
        if (latArray == null || lngArray == null) {
            return null;
        }
        boolean[] result = new boolean[lngArray.length + latArray.length];
        Arrays.fill(result, false);
        for (int i = 0; i < lngArray.length; i++) {
            result[2 * i] = lngArray[i];
        }
        for (int i = 0; i < latArray.length; i++) {
            result[2 * i + 1] = latArray[i];
        }
        return result;
    }

    /**
     * @param value
     * @param min
     * @param max
     * @Description: 将数字转化为geohash二进制字符串
     */
    private boolean[] getHashArray(double value, double min, double max, int length) {
        if (value < min || value > max) {
            return null;
        }
        if (length < 1) {
            return null;
        }
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            double mid = (min + max) / 2.0;
            if (value > mid) {
                result[i] = true;
                min = mid;
            } else {
                result[i] = false;
                max = mid;
            }
        }
        return result;
    }

}

