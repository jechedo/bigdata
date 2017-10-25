package cn.skyeye.elasticsearch.sql5x.plugin;

import cn.skyeye.elasticsearch.sql5x.exception.SqlParseException;
import cn.skyeye.elasticsearch.sql5x.query.multi.MultiQueryRequestBuilder;
import org.elasticsearch.client.Client;


/**
 * Created by Eliran on 21/8/2016.
 */
public class MultiRequestExecutorFactory {
     public static ElasticHitsExecutor createExecutor(Client client,MultiQueryRequestBuilder builder) throws SqlParseException {
         switch (builder.getRelation()){
             case UNION_ALL:
             case UNION:
                 return new UnionExecutor(client,builder);
             case MINUS:
                 return new MinusExecutor(client,builder);
             default:
                 throw new SqlParseException("only supports union, and minus operations");
         }
     }
}
