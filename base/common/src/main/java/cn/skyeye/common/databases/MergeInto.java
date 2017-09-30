package cn.skyeye.common.databases;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public class MergeInto{

	private String table;
	private List<Map<String, Object>> fieldValueMaps;
	private Set<String> columns;
	private Set<String> primaryKeys;

	private Set<String> autoUpdateDateFields = Sets.newHashSet();
	private Set<String> autoInsertDateFields = Sets.newHashSet();

	public MergeInto(String table) {
		this(table, null, null);
	}

	public MergeInto(String table, List<Map<String, Object>> fieldValueMaps, Set<String> columns) {
		this(table, fieldValueMaps, columns, null);
	}

	public MergeInto(String table, List<Map<String, Object>> fieldValueMaps, Set<String> columns, Set<String> primaryKeys) {
		this.table = table;
		this.fieldValueMaps = (fieldValueMaps != null) ? fieldValueMaps : new ArrayList<Map<String, Object>>();
		this.columns = (columns != null) ? columns : new HashSet<String>();
		this.primaryKeys = (primaryKeys != null) ? primaryKeys : new HashSet<String>();
	}

	public String getMergeIntoSql(){

		return createMergeIntoSql(table,
				fieldValueMaps,
				columns,
				primaryKeys,
				autoUpdateDateFields,
				autoInsertDateFields);
	}

	public static String createMergeIntoSql(String table,
											Map<String, Object> fieldValueMap,
											Set<String> primaryKeys,
											Set<String> autoUpdateDateFields,
											Set<String> autoInsertDateFields){

		int columnSize = fieldValueMap.size();
		Preconditions.checkArgument(columnSize > 0, String.format("表%s的字段个数不能为0", table));
		List<String> columns = Lists.newArrayList(fieldValueMap.keySet());

		if(primaryKeys == null)primaryKeys = Sets.newHashSet();
		if(autoUpdateDateFields == null)autoUpdateDateFields = Sets.newHashSet();
		if(autoInsertDateFields == null)autoInsertDateFields = Sets.newHashSet();

		String column;
		Object value;
		StringBuilder dual = new StringBuilder("select ");

		StringBuilder update = new StringBuilder("update set ");
		for(String field : autoUpdateDateFields){
			update.append("tn.").append(field).append(" = sysdate, ");
		}

		StringBuilder insertParam = new StringBuilder();
		StringBuilder insertValue = new StringBuilder();
		for(String field : autoInsertDateFields){
			insertParam.append("tn.").append(field).append(", ");
			insertValue.append("sysdate, ");
		}

		StringBuilder conditions = new StringBuilder();

		for(int i = 0; i < columnSize; i++) {
			column = columns.get(i);
			value = fieldValueMap.get(column);
			if (value == null) continue;

			if (value instanceof String) {
				dual.append("'").append(value).append("'")
						.append(" as ")
						.append(column);
			} else {
				dual.append(value)
						.append(" as ")
						.append(column);
			}

			if (!primaryKeys.contains(column)) {
				update.append("tn.").append(column).append(" = tmp.").append(column).append(", ");
			} else {
				conditions.append("tn.").append(column).append(" = tmp.").append(column).append(" and ");
			}

			insertParam.append("tn.").append(column);
			insertValue.append("tmp.").append(column);

			if (i + 1 < columnSize) {
				dual.append(", ");
				insertParam.append(", ");
				insertValue.append(", ");
			}
		}

		dual.append(" from dual");
		StringBuilder insert = new StringBuilder("insert (");
		insert.append(insertParam).append(") values (").append(insertValue).append(")");

		StringBuilder sql = new StringBuilder("merge into ");
		sql.append(table.toLowerCase())
				.append(" tn using (")
				.append(dual)
				.append(") tmp on (")
				.append(conditions.substring(0, conditions.lastIndexOf("and")))
				.append( ") when matched then ")
				.append(update.substring(0, update.lastIndexOf(",")))
				.append(" when not matched then ")
				.append(insert);

		return sql.toString();

	}

	public static String createMergeIntoSql(String table,
											List<Map<String, Object>> fieldValueMaps,
											Set<String> columns,
											Set<String> primaryKeys,
											Set<String> autoUpdateDateFields,
											Set<String> autoInsertDateFields){

		int recordSize = fieldValueMaps.size();
		Preconditions.checkArgument(recordSize > 0, String.format("表%s插入的记录数据不能为0", table));
		int columnSize = columns.size();
		Preconditions.checkArgument(columnSize > 0, String.format("表%s插入的字段数不能为0", table));

		if(primaryKeys == null)primaryKeys = Sets.newHashSet();
		if(autoUpdateDateFields == null)autoUpdateDateFields = Sets.newHashSet();
		if(autoInsertDateFields == null)autoInsertDateFields = Sets.newHashSet();

		StringBuilder update = new StringBuilder("update set ");
		for(String field : autoUpdateDateFields){
			update.append("tn.").append(field).append(" = sysdate, ");
		}

		StringBuilder insertParam = new StringBuilder();
		StringBuilder insertValue = new StringBuilder();
		for(String field : autoInsertDateFields){
			insertParam.append("tn.").append(field).append(", ");
			insertValue.append("sysdate, ");
		}

		int n = 0;
		for (String column : columns){

			if (!primaryKeys.contains(column)) {
				update.append("tn.").append(column).append(" = tmp.").append(column).append(", ");
			}

			insertParam.append("tn.").append(column);
			insertValue.append("tmp.").append(column);
			if (++n < columnSize) {
				insertParam.append(", ");
				insertValue.append(", ");
			}
		}

		StringBuilder conditions = new StringBuilder();
		n = 0;
		for (String primaryKey : primaryKeys){
			conditions.append("tn.").append(primaryKey).append(" = tmp.").append(primaryKey);
			if(++n < primaryKeys.size()){
				conditions.append(" and ");
			}
		}

		StringBuilder dualAll = new StringBuilder("(");
		StringBuilder dual;
		Object value;
		int m = 0;
		for(Map<String, Object> fieldValueMap : fieldValueMaps){

			dual = new StringBuilder("select ");
			n = 0;
			for(String column : columns){
				value = fieldValueMap.get(column);
				if (value instanceof String) {
					dual.append("'").append(value).append("'").append(" as ").append(column);
				} else {
					dual.append(value).append(" as ").append(column);
				}
				if (++n < columnSize) dual.append(", ");
			}
			dual.append(" from dual");

			dualAll.append(dual);
			if(++m < recordSize){
				dualAll.append(" union all ");
			}else {
				dualAll.append(")");
			}
		}

		StringBuilder insert = new StringBuilder("insert (");
		insert.append(insertParam).append(") values (").append(insertValue).append(")");

		StringBuilder sql = new StringBuilder("merge into ");
		sql.append(table.toLowerCase())
				.append(" tn using ")
				.append(dualAll)
				.append(" tmp on (")
				.append(conditions)
				.append( ") when matched then ")
				.append(update.substring(0, update.lastIndexOf(",")))
				.append(" when not matched then ")
				.append(insert);

		return sql.toString();
	}

	public void addFieldValueMap(Map<String, Object> fieldValueMap){
		if(fieldValueMap != null)
			this.fieldValueMaps.add(fieldValueMap);
	}

	/**
	 * 自动更新的日期字段  不应该包含在 fieldValueMap 中  和  columns 中
	 * @param autoUpdateDateFields
	 */
	public void setAutoUpdateDateFields(Set<String> autoUpdateDateFields) {
		this.autoUpdateDateFields = autoUpdateDateFields;
	}

	/**
	 * 自动生成的日期字段  不应该包含在 fieldValueMap 中  和  columns 中
	 * @param autoInsertDateFields
	 */
	public void setAutoInsertDateFields(Set<String> autoInsertDateFields) {
		this.autoInsertDateFields = autoInsertDateFields;
	}

	/**
	 * merge into  记录相等的判读字段
	 * @param primaryKeys
	 */
	public void setPrimaryKeys(Set<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public void setColumns(Set<String> columns) {
		this.columns = columns;
	}

	public String getTable() {
		return table;
	}

	public Set<String> getPrimaryKeys() {
		return primaryKeys;
	}

	public Set<String> getColumns() {
		return columns;
	}

	public Set<String> getAutoUpdateDateFields() {
		return autoUpdateDateFields;
	}

	public Set<String> getAutoInsertDateFields() {
		return autoInsertDateFields;
	}

	public static void main(String[] args) {

		MergeInto demo = new MergeInto("DEMO");
		Map<String,Object> record = Maps.newHashMap();
		record.put("mac", "000");
		record.put("name", "jechedo");
		record.put("phone", "15623456991");
		record.put("address", "hbwh");
		record.put("age", 26);
		record.put("id", "258145");
		record.put("studid", "365412");

		demo.setColumns(record.keySet());
		demo.setPrimaryKeys(Sets.newHashSet("id", "studid", "name"));

		demo.setAutoInsertDateFields(Sets.newHashSet("rksj", "update"));
		demo.setAutoUpdateDateFields(Sets.newHashSet("update"));

		demo.addFieldValueMap(record);
		demo.addFieldValueMap(record);
		demo.addFieldValueMap(record);
		demo.addFieldValueMap(record);

		System.out.println(demo.getMergeIntoSql());
	}

}