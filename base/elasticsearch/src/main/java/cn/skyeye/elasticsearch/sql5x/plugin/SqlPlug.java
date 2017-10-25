package cn.skyeye.elasticsearch.sql5x.plugin;

import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;

import java.util.ArrayList;
import java.util.List;

public class SqlPlug extends Plugin implements ActionPlugin {

	public SqlPlug() {
	}


	public String name() {
		return "sql";
	}

	public String description() {
		return "Use sql to query elasticsearch.";
	}


	@Override
	public List<Class<? extends RestHandler>> getRestHandlers() {
		List<Class<? extends RestHandler>> restHandlers = new ArrayList<>(1);
		restHandlers.add(RestSqlAction.class);

		return restHandlers;
	}
}
