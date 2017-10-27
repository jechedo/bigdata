package cn.skyeye.rpc;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.skyeye.rpc.api.RpcEvent;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

public class RpcBeanProxy implements InvocationHandler {

	private ActorRef rpcClientServer;

	private Class<?> clz;

	public <T> T proxy(ActorRef rpcClientServer, Class<T> clz) {
		this.rpcClientServer = rpcClientServer;
		this.clz = clz;
		Class<?>[] clzz = new Class<?>[] { clz };
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), clzz, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result;
		RpcEvent.CallMethod callMethod = new RpcEvent.CallMethod(
				method.getName(), args, clz.getName());
		Future<Object> future = Patterns.ask(rpcClientServer, callMethod,
				new Timeout(Duration.create(5, TimeUnit.SECONDS)));
		Object o = Await.result(future, Duration.create(5, TimeUnit.SECONDS));
		result = o;
		return result;
	}

}
