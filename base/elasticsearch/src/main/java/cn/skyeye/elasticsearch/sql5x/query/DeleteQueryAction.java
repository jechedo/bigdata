package cn.skyeye.elasticsearch.sql5x.query;


import cn.skyeye.elasticsearch.sql5x.domain.Delete;
import cn.skyeye.elasticsearch.sql5x.domain.Where;
import cn.skyeye.elasticsearch.sql5x.exception.SqlParseException;
import cn.skyeye.elasticsearch.sql5x.query.maker.QueryMaker;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;


public class DeleteQueryAction extends QueryAction {

	private final Delete delete;
	private DeleteByQueryRequestBuilder request;

	public DeleteQueryAction(Client client, Delete delete) {
		super(client, delete);
		this.delete = delete;
	}

	@Override
	public SqlElasticDeleteByQueryRequestBuilder explain() throws SqlParseException {
		this.request = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);

		setIndicesAndTypes();
		setWhere(delete.getWhere());
        SqlElasticDeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new SqlElasticDeleteByQueryRequestBuilder(request);
		return deleteByQueryRequestBuilder;
	}


	/**
	 * Set indices and types to the delete by query request.
	 */
	private void setIndicesAndTypes() {

        DeleteByQueryRequest innerRequest = request.request();
        innerRequest.indices(query.getIndexArr());
        String[] typeArr = query.getTypeArr();
        if (typeArr!=null){
            innerRequest.getSearchRequest().types(typeArr);
        }
//		String[] typeArr = query.getTypeArr();
//		if (typeArr != null) {
//            request.set(typeArr);
//		}
	}


	/**
	 * Create filters based on
	 * the Where clause.
	 *
	 * @param where the 'WHERE' part of the SQL query.
	 * @throws SqlParseException
	 */
	private void setWhere(Where where) throws SqlParseException {
		if (where != null) {
			QueryBuilder whereQuery = QueryMaker.explan(where);
			request.filter(whereQuery);
		} else {
			request.filter(QueryBuilders.matchAllQuery());
		}
	}

}
