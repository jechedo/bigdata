package cn.skyeye.kafka.ganglia.metrics;


import java.util.HashMap;
import java.util.Map;

/**
 * @author pcaparroy
 *
 */
public class ZookeeperMetrics {
    private String displayName;
    private double serverId;
    private Map<String, Double> metrics;

    public ZookeeperMetrics(String displayName) {
        this.displayName = displayName;
        this.metrics = new HashMap<>();
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public double getMetric(String key, double defaultValue) {
        Double aDouble = this.metrics.get(key);
        if(aDouble == null) aDouble = defaultValue;
        return aDouble;
    }

    public void setMetrics(String metricName, double metricValue){
        this.metrics.put(metricName, metricValue);
    }

    public void addOrSetMetrics(String metricName, double metricValue){
        Double aDouble = this.metrics.get(metricName);
        if(aDouble != null){
            metricValue += aDouble;
        }
        setMetrics(metricName, metricValue);
    }

    public void cutMetrics(String metricName, double metricValue){
        Double aDouble = this.metrics.get(metricName);
        if(aDouble != null){
            aDouble -= metricValue;
            setMetrics(metricName, aDouble);
        }
    }

    public double getServerId() {
        return serverId;
    }

    public void setServerId(double serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZookeeperMetrics{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", metrics=").append(metrics);
        sb.append('}');
        return sb.toString();
    }
}
