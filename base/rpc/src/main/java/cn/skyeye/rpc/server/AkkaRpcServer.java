package cn.skyeye.rpc.server;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import cn.skyeye.rpc.transformations.TransformationBackend;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

public class AkkaRpcServer {

	private String port;
	private String hostName;
	private String akkaSystemName;
	private List<String> seedNodes;
	private ActorSystem system;

	public AkkaRpcServer() {
		this.hostName = "127.0.0.1";
		this.port = "9911";
		this.akkaSystemName = "AkkaRpcSystem";
		this.seedNodes = Lists.newArrayList("127.0.0.1:9911", "127.0.0.1:8811");
	}

	public void start() {
		final Config config = ConfigFactory
				.parseString("akka.actor.provider=akka.cluster.ClusterActorRefProvider")
				.withFallback(
						ConfigFactory.parseString(String.format("akka.remote.netty.tcp.hostname=%s", hostName)))
				.withFallback(
						ConfigFactory.parseString(String.format("akka.remote.netty.tcp.port=%s", port)))
				.withFallback(
						ConfigFactory.parseString(String.format("akka.cluster.auto-down=%s", "off")))
				.withFallback(
						ConfigFactory.parseString("akka.cluster.roles = [RpcServer]"));

		system = ActorSystem.create(akkaSystemName, config);

		for (int i = 0; i < seedNodes.size(); i++) {
			String hostPort = seedNodes.get(i);
			String[] hosts = hostPort.split(":");
			Address address = new Address("akka.tcp", akkaSystemName, hosts[0], Integer.valueOf(hosts[1]));
			Cluster.get(system).join(address);
		}

		system.actorOf(Props.create(RpcClusterListener.class), "rpcServer");
		system.actorOf(Props.create(TransformationBackend.class), "rpcServer-t");

	}

	public void close() {
		system.shutdown();
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getAkkaSystemName() {
		return akkaSystemName;
	}

	public void setAkkaSystemName(String akkaSystemName) {
		this.akkaSystemName = akkaSystemName;
	}

	public List<String> getSeedNodes() {
		return seedNodes;
	}

	public void setSeedNodes(List<String> seedNodes) {
		this.seedNodes = seedNodes;
	}

}
