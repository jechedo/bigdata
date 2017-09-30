package cn.skyeye.common;

import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CharacterEncodingDetector {
	
	 private CharacterEncodingDetector(){}

	 public static String detect(byte[] content) {
		 
	        UniversalDetector detector = new UniversalDetector(null);
	            //开始给一部分数据，官方建议是1000个byte左右（当然这1000个byte你得包含中文之类的）
	        detector.handleData(content, 0, content.length);
	            //识别结束必须调用这个方法
	        detector.dataEnd();
	        //神奇的时刻就在这个方法了，返回字符集编码。
	        return detector.getDetectedCharset();
	        
	    }

	    public static void main(String[] args) throws IOException {
	        byte[] bytes = IOUtils.toByteArray(new FileInputStream(new File("D:\\demo\\bh_sgk_capture.txt")));
	        System.out.println(detect(bytes));
	    }
	 
}
