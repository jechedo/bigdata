package cn.skyeye.aptrules.ioc2rules.rules.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.IndexKey;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:50
 */
public class RuleMemoryStore extends RuleStore{

    private Map<String, RuleCache> cacheMap;

    public RuleMemoryStore(ARConf arConf) {
        super(arConf);
        this.cacheMap = Maps.newHashMap();
    }

    @Override
    public List<VagueRule> allRules(Ruler.RuleID ruleID) {
        RuleCache ruleCache = cacheMap.get(ruleID.name());
        return ruleCache == null ? Lists.newArrayList() : ruleCache.getRulesCache();
    }

    @Override
    public void overrideRules(Ruler.RuleID ruleID, List<VagueRule> vagueRules) {
        deleteCache(ruleID.name());
        addCache(ruleID.name(), vagueRules);
    }

    public List<VagueRule> findRules(IndexKey indexKey){
        List<VagueRule> rules = Lists.newArrayList();
        Set<Integer> ids = Sets.newHashSet();
        cacheMap.forEach((key, ruleCache) ->{
            Set<Integer> cacheIds = ruleCache.rulesIndexs.get(indexKey.getRuleKey());
            cacheIds.removeAll(ids);
            VagueRule rule;
            for (Integer cacheId : cacheIds) {
                rule = ruleCache.getRulesCache().get(cacheId);
                if (rule.matches(indexKey)) {
                    rules.add(rule);
                }
            }
            ids.addAll(cacheIds);
        });

        return rules;
    }

    private void deleteCache(String id){
        this.cacheMap.remove(id);
    }

    private void addCache(String id, List<VagueRule> vagueRules){
        int size = vagueRules.size();
        HashMultimap<String, Integer> rulesIndexs = HashMultimap.create();
        VagueRule vagueRule;
        Set<String> roleIndexKeys;
        for (int i = 0; i < size; i++) {
            vagueRule = vagueRules.get(i);
            roleIndexKeys = vagueRule.getRoleIndexKeys();
            for(String roleIndexKey : roleIndexKeys){
                rulesIndexs.put(roleIndexKey, i);
            }
        }
        cacheMap.put(id, new RuleCache(vagueRules, rulesIndexs));
        logger.info(String.format("%s的rule写入内存成功, roleCount = %s。", id, size));
    }

    private class RuleCache{
        private List<VagueRule> rulesCache;
        private HashMultimap<String, Integer> rulesIndexs;

        public RuleCache(List<VagueRule> rulesCache,
                         HashMultimap<String, Integer> rulesIndexs) {
            this.rulesCache = rulesCache;
            this.rulesIndexs = rulesIndexs;
        }

        public List<VagueRule> getRulesCache() {
            return rulesCache;
        }

        public HashMultimap<String, Integer> getRulesIndexs() {
            return rulesIndexs;
        }
    }
}
