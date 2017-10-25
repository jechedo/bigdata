package cn.skyeye.elasticsearch.sql5x.plugin;

import cn.skyeye.elasticsearch.sql5x.exception.SqlParseException;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;

/**
 * Created by Eliran on 21/8/2016.
 */
public interface ElasticHitsExecutor {
    public void run() throws IOException, SqlParseException;
    public SearchHits getHits();
}
