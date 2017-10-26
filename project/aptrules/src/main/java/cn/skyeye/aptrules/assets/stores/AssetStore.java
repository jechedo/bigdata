package cn.skyeye.aptrules.assets.stores;

import cn.skyeye.aptrules.assets.Asset;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/26 14:54
 */
public abstract class AssetStore {
    protected final Logger logger = Logger.getLogger(AssetStore.class);

    public abstract List<Asset> queryAsset(String conditions, int maxSize);

    public abstract Asset getByAssetId(String assetId);

    public abstract void updateAssets(List<Asset> assets);
}
