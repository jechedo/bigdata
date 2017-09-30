/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.vfs;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

/**
  * Description:
  * 
  *  Date    2016年7月20日 上午6:06:42   
  *                  
  * @author  LiXiaoCong
  * @version 1.0
  * @since   JDK 1.7
 */
public class FileListenerImpl implements FileListener {

	
	public void fileCreated(FileChangeEvent event) throws Exception {
		System.out.println(String.format("%s --> %s ", "fileCreated", event));
	}
	
	public void fileChanged(FileChangeEvent event) throws Exception {
		System.out.println(String.format("%s --> %s ", "fileChanged", event));
	}
	
	public void fileDeleted(FileChangeEvent event) throws Exception {
		System.out.println(String.format("%s --> %s ", "fileDeleted", event));
	}

}
