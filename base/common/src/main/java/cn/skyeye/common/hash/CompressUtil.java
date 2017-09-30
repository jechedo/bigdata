package cn.skyeye.common.hash;

import java.io.Serializable;

public class CompressUtil implements Serializable{
	private static final long serialVersionUID = 4659044569307389596L;

	public static long mulHash2Version(String str) {
		int hash, i;
		for (hash = str.length(), i = 0; i < str.length(); i++) {
			hash = 33 * hash + str.charAt(i);
		}
		
		String result = String.valueOf(hash).replaceAll("-", "1");
		
		return Long.parseLong(result);
	}
}
