package cn.skyeye.rpc.transformations;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

public class TransformationBackendMain {

  public static void main(String[] args) {

    // Override the configuration of the port when specified as program argument

    String akkaSystemName = "AkkaRpcSystem";
    List<String> seedNodes = Lists.newArrayList("127.0.0.1:9911", "127.0.0.1:8811");

    final Config config = ConfigFactory
            .parseString("akka.actor.provider=akka.cluster.ClusterActorRefProvider")
            .withFallback(
                    ConfigFactory.parseString(String.format("akka.remote.netty.tcp.hostname=%s", "127.0.0.1")))
            .withFallback(
                    ConfigFactory.parseString(String.format("akka.remote.netty.tcp.port=%s", 8812)))
            .withFallback(
                    ConfigFactory.parseString(String.format("akka.cluster.auto-down=%s", "off")))
            .withFallback(
                    ConfigFactory.parseString("akka.cluster.roles = [RpcServer]"));

      ActorSystem system = ActorSystem.create("AkkaRpcSystem", config);

      for (int i = 0; i < seedNodes.size(); i++) {
        String hostPort = seedNodes.get(i);
        String[] hosts = hostPort.split(":");
        Address address = new Address("akka.tcp", akkaSystemName, hosts[0], Integer.valueOf(hosts[1]));
        Cluster.get(system).join(address);
    }

    ActorRef backend = system.actorOf(Props.create(TransformationBackend.class), "backend");
    ActorRef actorRef = system.actorFor(toAkkaUrl("127.0.0.1", 9911, "rpcServer-t"));

    actorRef.tell(new TransformationMessages.TransformationJob("hello world"), backend);

  }


  public static String toAkkaUrl(String host, int port, String actorName) {
    return "akka.tcp://AkkaRpcSystem@" + host + ":" + port + "/user/"
            + actorName;
  }

}
