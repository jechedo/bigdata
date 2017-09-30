/**
 * 
 */
package cn.skyeye.common.logging;


import org.apache.log4j.Logger;

public abstract class Logging  {
	
	private transient Logger log_;

	public Logging(){
		  this(Logger.getLogger(Logging.class));
	  }

	public Logging(Logger log){
		  if(log == null){
			  log_ = Logger.getLogger(Logging.class);
		  }else{
			  this.log_ = log;
		  }
	  }

	public void logDebug(String msg ) {
		log_.debug(msg);
	}

     public void logInfo(String msg ) {
	    log_.info(msg);
	  }

	public void logWarning(String msg){
		log_.warn(msg);
	  }

	public void logError(String msg) {
			log_.error(msg);
	  }

	public void logTrace(String msg) {
		log_.trace(msg);
	}

	public void logInfo(String msg, Throwable throwable) {
		log_.info(msg, throwable);
	  }

	public void logDebug(String msg, Throwable throwable) {
		log_.debug(msg, throwable);
	  }

	public void logTrace(String msg, Throwable throwable) {
		log_.trace(msg, throwable);
	  }

	public void logWarning(String msg, Throwable throwable) {
		log_.warn(msg, throwable);
	  }

	public void logError(String msg, Throwable throwable) {
		log_.error(msg, throwable);
	  }

	public Boolean isTraceEnabled() {
	    return log_.isTraceEnabled();
	  }

	public void setLogger(Logger log) {
		 this.log_ = log;
	}
}
