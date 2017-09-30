package cn.skyeye.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;


/**
 * @Description:配置文件存取
 * @author:
 * @date:
 * @version:v1.0
 */
public class Xmls {

	public static void main(String[] args) throws MalformedURLException,
			DocumentException {

		final Document doc = read("D:/demo/GAB_ZIP_INDEX.xml");
		String xpath = "/MESSAGE/DATASET/DATA/DATASET/DATA";
		final List<Element> elements = doc.selectNodes(xpath);
		List<Element> nodes;
		for(Element element : elements){

			//获取文件编码
			nodes = element.selectNodes("ITEM");
			Map<String, String> typeMap = Maps.newHashMap();
			for(Element node : nodes){
				Attribute keyAtttr = node.attribute("key");
				Attribute valAtttr = node.attribute("val");

				typeMap.put(keyAtttr.getValue(), valAtttr.getValue());
			}
			List<String> columns = null;
			nodes = element.selectNodes("DATASET");
			for(Element node : nodes){

				final List<Element> list = node.selectNodes("DATA/ITEM");
				final String rmk = node.attribute("rmk").getValue();
				if(rmk.endsWith("信息")) {
					for (Element e : list) {
						Attribute keyAtttr = e.attribute("key");
						Attribute valAtttr = e.attribute("val");
						typeMap.put(keyAtttr.getValue(), valAtttr.getValue());
					}
				}else if(rmk.endsWith("结构")){
					columns = Lists.newArrayListWithExpectedSize(list.size());
					for (Element e : list) {
						Attribute clAtttr = e.attribute("eng");
						columns.add(clAtttr.getValue());
					}
				}
			}


			System.err.println(typeMap);
			System.err.println(columns);

		}
	}

	/**
	 * @Description:读取xml，返回Document
	 */
	public static Document read(String xmlPath) throws DocumentException {
		return  read(new File(xmlPath));
	}

	/**
	 * @Description:读取xml，返回Document
	 */
	public static Document read(File xmlFile) throws DocumentException {
		return  new SAXReader().read(xmlFile);
	}

	/**
	 * @Description:读取xml，返回Document
	 */
	public static List<Element> read(String xmlPath,
									 String xpathExpression) throws DocumentException {

		return read(new File(xmlPath), xpathExpression);
	}

	/**
	 * @Description:读取xml，返回Document
	 */
	public static List<Element> read(File xmlFile,
									 String xpathExpression) throws DocumentException {

		return read(xmlFile).selectNodes(xpathExpression);
	}

	/**
	 * @Description:得到配置
	 */
	public static String getValue(String fileName, String ckey) throws DocumentException {
		Document document = read(fileName);
		Element root = document.getRootElement();
		String config = "";
		Element element = root.element(ckey);
		if(element!=null)
		  config = element.getText();
		return config;
	}
	
	public static void setValue(String filename,String vkey,
								String vvalue) throws DocumentException, IOException {
		Document document = read(filename);
		Element root = document.getRootElement();
		Element element = root.element(vkey);
		if (null == element) {
			addNode(filename, vkey, vvalue);
			
			return ;
		}
		element.setText(vvalue);
		XMLWriter writer = new XMLWriter(new FileOutputStream(new File(filename)));
		writer.write(document);
		writer.close();
	}
	
	public synchronized static void addNode(String filename,
											String tagname,String value) throws DocumentException, IOException {
        Document doc =read(filename);  
        Element root = doc.getRootElement();  
        root.addElement(tagname).addText(value);

		FileOutputStream fos = new FileOutputStream(filename);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		OutputFormat of = OutputFormat.createPrettyPrint();
		of.setEncoding("UTF-8");

		XMLWriter writer = new XMLWriter(osw, of);
		writer.write(doc);
		writer.flush();
		writer.close();
	}
}
