package cn.skyeye.kafka.ganglia.configs;


import java.util.HashSet;
import java.util.Set;

public class ZkMonitorConf {

    String metricPrefix;
    Server[] servers;
    Command[] commands;
    private int threadTimeout;
    private int numberOfThreads;
    private long pollingInterval;
    private String gmondHost;
    private int gmondPort;

    public Server[] getServers() {
        return servers;
    }

    public void setServers(Server[] servers) {
        this.servers = servers;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public Command[] getCommands() {
        return commands;
    }

    public void setCommands(Command[] commands) {
        this.commands = commands;
    }

    public int getThreadTimeout() {
        return threadTimeout;
    }

    public void setThreadTimeout(int threadTimeout) {
        this.threadTimeout = threadTimeout;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	
    public long getPollingIntervalMillis(){
    	return 1000 * getPollingInterval();
    }

	public String getGmondHost() {
		return gmondHost;
	}

	public void setGmondHost(String gmondHost) {
		this.gmondHost = gmondHost;
	}

	public int getGmondPort() {
		return gmondPort;
	}

	public void setGmondPort(int gmondPort) {
		this.gmondPort = gmondPort;
	}

    public static class Server {

        private String displayName;
        private String server;
        private double serverId;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getServerId() {
            return serverId;
        }

        public void setServerId(double serverId) {
            this.serverId = serverId;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Server{");
            sb.append("displayName='").append(displayName).append('\'');
            sb.append(", server='").append(server).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class Command {

        private String command;
        private String separator;
        Set<String> fields = new HashSet<>();

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public Set<String> getFields() {
            return fields;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Command{");
            sb.append("command='").append(command).append('\'');
            sb.append(", separator='").append(separator).append('\'');
            sb.append(", fields=").append(fields);
            sb.append('}');
            return sb.toString();
        }
    }
}



