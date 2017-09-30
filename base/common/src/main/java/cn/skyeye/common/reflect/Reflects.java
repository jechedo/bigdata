/**
 * 
 */
package cn.skyeye.common.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflects {
	
	private static final Logger LOG = LoggerFactory.getLogger(Reflects.class);
	
	private Reflects(){}

	@SuppressWarnings("unchecked")
	public static <T> T createInstanceByClassName(String className,
												  Class<T> classType, Class<?>[] parameterTypes, Object[] initargs){

		T instance = null;
		
		try {
			
			classType = (Class<T>) Class.forName(className);
			if(parameterTypes == null || parameterTypes.length == 0
					    || initargs == null || initargs.length == 0){
				instance  =  classType.newInstance();
				LOG.info("使用默认构造器实例化对象:" + classType.getName());
			}else{
			
				Constructor<T> constructor = classType.getDeclaredConstructor(parameterTypes);
				switch(constructor.getModifiers()){
				
					case 1 : 
						instance = constructor.newInstance(initargs);
						LOG.info("通过给定的参数类型数据获取的构造器类型为：public , 初始化成功。");
						break;
					case 4 : 
						constructor.setAccessible(true);
						instance = constructor.newInstance(initargs);
						LOG.info("通过给定的参数类型数据获取的构造器类型为：protected , 初始化成功。");
						break;
					case 0 :
						constructor.setAccessible(true);
						instance = constructor.newInstance(initargs);
						LOG.info("通过给定的参数类型数据获取的构造器类型为：default , 初始化成功。");
						break;
					case 2 :
						constructor.setAccessible(true);
						instance = constructor.newInstance(initargs);
						LOG.info("通过给定的参数类型数据获取的构造器类型为：private , 初始化成功。");
						break;
					default :
						constructor.setAccessible(true);
						instance = constructor.newInstance(initargs);
						LOG.info("通过给定的参数类型数据获取的构造器类型为： 未知 , 初始化成功。");
						break;
				}
			}
			
			
		} catch (ClassNotFoundException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (SecurityException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LOG.error(null, e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			LOG.error(null, e);
			e.printStackTrace();
		}
		
		return instance;
  }

    public static Object executeMethod(String clazz, String method,
								   Class[] parameterTypes, Object[] parameterValues) throws Exception {

	  return executeMethod(Class.forName(clazz), method, parameterTypes, parameterValues);
    }

    public static Object executeMethod(Class clazz, String method,
								   Class[] parameterTypes, Object[] parameterValues) throws Exception {

	  Method declaredMethod = clazz.getDeclaredMethod(method, parameterTypes);
	  declaredMethod.setAccessible(true);
	  return declaredMethod.invoke(clazz.newInstance(), parameterValues);
    }

}
