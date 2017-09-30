package cn.skyeye.common.logging;

import cn.skyeye.common.SysEnvs;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.*;

import java.io.File;

/**
 *  获取动态的Logger的静态工厂类
 *  若没有指定 logFilePath：
 *     在tomcat下 日志路径为 {tomcat_home}/logs/D406
 *     不再tomcat下 日志路径为当前包同目录下
 *  @author lixiaocong
 *
 */
public class DynamicLogFactory {

	public static final String SIMPLE_FORMAT = "%m%n";
	public static final String NORMAL_FORMAT = "%-d{yyyy-MM-dd HH:mm:ss} %p [%C{1}:%L] %m%n";

	public static final String PATH ;
	static {
		String parent = System.getProperty("catalina.home");
		if(StringUtils.isBlank(parent)){
			parent= SysEnvs.getJarFileDirByClass(DynamicLogFactory.class);
		}
		PATH = new File(parent, "logs/D406").getAbsolutePath();
	}

	private DynamicLogFactory(){}

	public static Logger getSimpleLogger(Class<?> clazz){
		return getSimpleLogger(clazz.getName(), PATH);
	}

	public static Logger getSimpleLogger(Class<?> clazz, boolean stdout){
		return getSimpleLogger(clazz.getName(), PATH, stdout);
	}

	public static Logger getSimpleLogger(String name){
		return getSimpleLogger(name,  PATH);
	}

	public static Logger getSimpleLogger(String name, boolean stdout){
		return getSimpleLogger(name,  PATH, stdout);
	}

	public static Logger getSimpleLogger(Class<?> clazz, String logFilePath){
		return getSimpleLogger(clazz.getName(), Level.INFO, logFilePath);
	}

	public static Logger getSimpleLogger(Class<?> clazz, String logFilePath, boolean stdout){
		return getSimpleLogger(clazz.getName(), Level.INFO, logFilePath, stdout);
	}

	public static Logger getSimpleLogger(String name, String logFilePath){
		return getSimpleLogger(name, Level.INFO, logFilePath);
	}

	public static Logger getSimpleLogger(String name, String logFilePath, boolean stdout){
		return getSimpleLogger(name, Level.INFO, logFilePath, stdout);
	}

	public static Logger getSimpleLogger(Class<?> clazz, Level level, String logFilePath){
		return getLogger(clazz.getName(), level, logFilePath, SIMPLE_FORMAT, true);
	}

	public static Logger getSimpleLogger(Class<?> clazz, Level level, String logFilePath, boolean stdout){
		return getLogger(clazz.getName(), level, logFilePath, SIMPLE_FORMAT, stdout);
	}

	public static Logger getSimpleLogger(String name, Level level, String logFilePath){
		return getLogger(name, level, logFilePath, SIMPLE_FORMAT, true);
	}

	public static Logger getSimpleLogger(String name, Level level, String logFilePath, boolean stdout){
		return getLogger(name, level, logFilePath, SIMPLE_FORMAT, stdout);
	}

	public static Logger getLogger(Class<?> clazz){
		return getLogger(clazz, PATH);
	}

	public static Logger getLogger(Class<?> clazz, boolean stdout){
		return getLogger(clazz, PATH, stdout);
	}

	public static Logger getLogger(String name){
		return getLogger(name, PATH);
	}

	public static Logger getLogger(String name, boolean stdout){
		return getLogger(name, PATH, stdout);
	}

	public static Logger getLogger(Class<?> clazz, String logFilePath){
		return getLogger(clazz.getName(), Level.INFO, logFilePath);
	}

	public static Logger getLogger(Class<?> clazz, String logFilePath, boolean stdout){
		return getLogger(clazz.getName(), Level.INFO, logFilePath, stdout);
	}

	public static Logger getLogger(String name, String logFilePath){
		return getLogger(name, Level.INFO, logFilePath);
	}

	public static Logger getLogger(String name, String logFilePath, boolean stdout){
		return getLogger(name, Level.INFO, logFilePath, stdout);
	}

	public static Logger getLogger(Class<?> clazz, Level level, String logFilePath){
		return getLogger(clazz.getName(), level, logFilePath);
	}

	public static Logger getLogger(Class<?> clazz, Level level, String logFilePath, boolean stdout){
		return getLogger(clazz.getName(), level, logFilePath, stdout);
	}

	public static Logger getLogger(String name, Level level, String logFilePath) {

		return getLogger(name, level, logFilePath, NORMAL_FORMAT, true);
	}

	public static Logger getLogger(String name, Level level, String logFilePath, boolean stdout) {

		return getLogger(name, level, logFilePath, NORMAL_FORMAT, stdout);
	}

	public static Logger getLogger(String name, Level level, String logFilePath, String format, boolean stdout) {

		Preconditions.checkArgument(StringUtils.isNotBlank(name), "创建Logger的参数name不能为空。");

		Logger logger = Logger.getLogger(name);
		logger.removeAllAppenders();

		if(level == null)level = Level.INFO;
		logger.setLevel(level);

		// 设定是否继承Logger。
		// 默认为true。继承root输出。
		// 设定为false将不再输出root。
		logger.setAdditivity(false);

		// 将新的Appender加到Logger中
		if(StringUtils.isBlank(logFilePath)) logFilePath = PATH;
		if(!name.endsWith(".log"))name = name + ".log";


		switch (level.toInt()){
			case Priority.ALL_INT :
			case Priority.DEBUG_INT:
				logger.addAppender(newDebugAppender(name, logFilePath, format));
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;
			case Priority.INFO_INT:
				logger.addAppender(newInfoAppender(name, logFilePath, format));
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;
			case Priority.WARN_INT:
				logger.addAppender(newWarnAppender(name, logFilePath, format));
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;

			case Priority.ERROR_INT:
				logger.addAppender(newErrorAppender(name, logFilePath, format));
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;
			case Priority.FATAL_INT:
				logger.addAppender(newFatalAppender(name, logFilePath, format));
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;
			default:
				if(stdout)logger.addAppender(newStdoutAppender(logFilePath));
				break;
		}

		return logger;
	}

	private static ConsoleAppender newStdoutAppender(String logFilePath){

		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("[%p] %d{yyyy-MM-dd HH:mm:ss} method:%l%n%m%n");
		ConsoleAppender appender = new ConsoleAppender(layout);
        appender.setEncoding("UTF-8");
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newDebugAppender(String name, String logFilePath, String format){
		FileAppender appender = newAppenderModel(Level.DEBUG, format);
		appender.setFile(new File(logFilePath, name).getAbsolutePath());
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newInfoAppender(String name, String logFilePath, String format){
		FileAppender appender = newAppenderModel(Level.INFO, format);
		appender.setFile(new File(logFilePath, name).getAbsolutePath());
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newWarnAppender(String name, String logFilePath, String format){
		FileAppender appender = newAppenderModel(Level.WARN, format);
		appender.setFile(new File(logFilePath, name).getAbsolutePath());
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newErrorAppender(String name, String logFilePath, String format){
		FileAppender appender = newAppenderModel(Level.ERROR, format);
		appender.setFile(new File(logFilePath, name).getAbsolutePath());
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newFatalAppender(String name, String logFilePath, String format){
		FileAppender appender = newAppenderModel(Level.FATAL, format);
		appender.setFile(new File(logFilePath, name).getAbsolutePath());
		appender.activateOptions();
		return appender;
	}

	private static FileAppender newAppenderModel(Level level, String format){
		// 生成新的Appender
		RollingFileAppender appender = new RollingFileAppender();

		PatternLayout layout = new PatternLayout();
		// log的输出形式
		if(StringUtils.isNotBlank(format))
			layout.setConversionPattern(format);
		appender.setLayout(layout);

		appender.setThreshold(level);
		appender.setMaxFileSize("200MB");

		// log的文字码
		appender.setEncoding("UTF-8");
		// true:在已存在log文件后面追加 false:新log覆盖以前的log
		appender.setAppend(true);

		return appender;
	}

	public static void main(String[] args) {
		Logger logger = DynamicLogFactory.getLogger("etl", "D:/logs");
		logger.debug("just a test");
		logger.info("just a test");
		logger.warn("just a test");
		logger.error("just a test");
		System.out.println(logger);

	}

}
