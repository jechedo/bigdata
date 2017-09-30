/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.vfs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Description:
 * 
 *  Date    2016-6-28 下午3:55:39   
 *                  
 * @author  LiXiaoCong
 * @version 1.0
 * @since   JDK 1.7
 */
public class FileMonitorCenter implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(FileMonitorCenter.class);
	
	private static final int MAXMONITORNUM = 8;
	
	private boolean recursive;
	private boolean shutdown = false;
	
	private FileSystemManager fsManager;
	private FileListener listener;
	
	private Map<String, FileObject> files;
	private Map<String, DefaultFileMonitor> fileAndMonitor;
	private List<DefaultFileMonitor> monitors;
	
	public FileMonitorCenter(FileListener listener, String ... filePaths){
		this(true, listener, filePaths);
	}
	
	public FileMonitorCenter(boolean recursive, FileListener listener, String ... filePaths){
		 this(recursive, Sets.newHashSet(filePaths), listener);
	}

	
	public FileMonitorCenter(Collection<String>  filePaths, FileListener listener){
		this(true, filePaths, listener);
	}
	
	public FileMonitorCenter(boolean recursive, Collection<String>  filePaths, FileListener listener){
		
		this.recursive = recursive;
		this.listener = listener;
		
		files = Maps.newHashMap();
		fileAndMonitor = Maps.newHashMapWithExpectedSize(MAXMONITORNUM);
		monitors = Lists.newArrayListWithExpectedSize(MAXMONITORNUM);
		
		try {  
			FileObject listendir = null;  
			fsManager = VFS.getManager(); 
			for(String filePath : filePaths){
				listendir = fsManager.resolveFile(filePath);  
				files.put(filePath, listendir);
			}
        } catch (FileSystemException e) {  
        	LOG.error("监视文件夹出错了", e);  
        }  

		checkArgument(fsManager != null, "创建虚拟文件系统失败.");
		checkArgument(!files.isEmpty(),  "需要监听文件目录为空.");
	}
	
	public void run() {
		
		int n = 0;
		DefaultFileMonitor monitor = null;
		for(Entry<String, FileObject> entry : files.entrySet()){
			
			if(n < MAXMONITORNUM){
				monitor =  new DefaultFileMonitor(listener);
				monitor.setRecursive(recursive);
				monitor.addFile(entry.getValue());
				
				monitors.add(monitor);
				fileAndMonitor.put(entry.getKey(), monitor);
			}else{
				monitors.get((n % MAXMONITORNUM)).addFile(entry.getValue());
			}
			n++;
		}
		
		int size = monitors.size();
		for(int i = 0; i < size; i++){
			monitor = monitors.get(i);
			monitor.start();
		}
		System.out.println(String.format("创建的 monitor的个数为： %s ", size));
		LOG.info(String.format("创建的 monitor的个数为： %s ", size));
		
		try {
			while(!shutdown){
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			LOG.error(null, e);
		}
		
		 close();
		
		System.out.println("监控关闭.");
		LOG.info("监控关闭.");
	}
	
	private void close(){
		
		for(int i = 0; i < monitors.size(); i++) monitors.get(i).stop();
		monitors.clear();
		fileAndMonitor.clear();
	}
	
	public void shutdown(){
		this.shutdown = true;
	}

}
