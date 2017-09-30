package cn.skyeye.common.vfs;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class TestWatcherService {  
      
    private WatchService watcher;  
      
    public TestWatcherService(Path path)throws IOException{  
        watcher = FileSystems.getDefault().newWatchService();  
        path.register(watcher, ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY);  
    }  
      
    public void handleEvents() throws InterruptedException{  
    	
        while(true){  
            WatchKey key = watcher.take();  
            for(WatchEvent<?> event : key.pollEvents()){ 
            	
                WatchEvent.Kind kind = event.kind();  
                if(kind == OVERFLOW){ //事件可能lost or discarded  
                    continue;  
                }  
                  
                WatchEvent<Path> e = (WatchEvent<Path>)event;  
                Path fileName = e.context();  
                  
                System.out.printf("Event %s has happened,which fileName is %s%n"  
                        ,kind.name(),fileName);  
            }  
            if(!key.reset()){  
                break;  
            }  
        }  
    }  
      
    public static void main(String args[]) throws IOException, InterruptedException{  
    
       // new TestWatcherService(Paths.get("D:/demo/stationcenter2")).handleEvents();  
    	
    	
    	     
		 FileMonitorCenter center = new FileMonitorCenter(new FileListenerImpl(), "D:/demo/stationcenter2", "D:/demo/docs");
		 new Thread(center).start();
		 System.out.println("************************");
		 try {
			Thread.sleep(10000);
			center.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}  
    
}  