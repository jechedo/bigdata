package cn.skyeye.aptrules.assets;

import cn.skyeye.aptrules.Record;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/26 16:17
 */
public class Asset extends Record {

    private String id;

    public Asset(String id, Map<String, Object> data) {
        super(data);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Asset asset = (Asset) o;

        return id.equals(asset.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Asset clone(){
        return new Asset(id, Maps.newHashMap(data));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Asset{");
        sb.append("id='").append(id).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
