package com.sap.rc.main.service;

import static com.sap.cloud.sdk.service.prov.api.internal.SQLMapping.isPlainSqlMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.hana.connectivity.cds.CDSException;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSQuery;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSQueryWithParameters;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryResult;
import com.sap.cloud.sdk.hana.connectivity.handler.CDSDataSourceHandlerImpl;
import com.sap.cloud.sdk.service.prov.api.DatasourceExceptionType;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.EntityDataBuilder;
import com.sap.cloud.sdk.service.prov.api.internal.DefaultEntityData;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CustomCDSDataSourceHandler extends CDSDataSourceHandlerImpl {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Connection conn;
	private String SEPARATOR  = "\\.";
	
	CustomCDSDataSourceHandler(Connection conn, String ns) {
		
		super(conn, ns);
		this.conn = conn;
	}

	public CDSSelectQueryResult executeQuery(CDSQuery query) throws CDSException {
		
		List<EntityData> entityDataList = new ArrayList<EntityData>();

		// Change of default behavior begin
		//String fqEntityName = query.getEntityName();
		String currentTableName =  '"' + query.getEntityName() + '"';
		//String fqEntityName = getFqName(query.getEntityName());
		//currentTableName = getPersistenceName(fqEntityName, null);
		// Change of default behavior end
		String queryString = query.toString().replaceAll("\\$ENTITY_FQ_NAME\\$", currentTableName);

		try(PreparedStatement statment = this.conn.prepareStatement(queryString);) {

			if (query instanceof CDSQueryWithParameters) {
				List parameterValues = ((CDSQueryWithParameters) query).getParameterValues();
				setDataToPreparedStatement(parameterValues, statment);
			}
			
			try(ResultSet resultSet = statment.executeQuery();){
				while (resultSet.next()) {
					Map<String, Object> resultSetMap = extractResultSetRow(resultSet);
					EntityDataBuilder eBuilder = EntityData.getBuilder();
					for (Entry<String, Object> e : resultSetMap.entrySet()) {
						eBuilder.addElement(e.getKey(), e.getValue());
					}
					entityDataList.add(eBuilder.buildEntityData(query.getEntityName()));
				}
			}

		} catch (SQLException | IOException e) {
			logger.error("Error while executing query operation : " + e.getMessage(), e);
			throw new CDSException(DatasourceExceptionType.OTHER, getExceptionCause(e));
		} 
		
		if (isPlainSqlMapping()) {
			/*
			 * This is done because from db we will get Upper case, and we need to convert
			 * this back to the case which the developer has asked for. A better solution
			 * for this could be get the db to give us with proper case by using aliases.
			 * Should move to that if possible.
			 */
			List<EntityData> caseChangedList = new ArrayList<>();
			String selectedProperties = queryString.split("\\s*(SELECT|FROM)")[1];

			if (!selectedProperties.trim().equals("*")) {
				List<String> selectedList = Arrays.asList(selectedProperties.split(",")).stream().map(s -> s.trim())
						.collect(Collectors.toList());
				for (EntityData ed : entityDataList) {
					caseChangedList.add(getEntityDataAfterCaseChange(ed, selectedList, query.getEntityName()));
				}
				entityDataList = caseChangedList;
			}

		}
		return new CDSSelectQueryResult(entityDataList);
	}
	
	private void setDataToPreparedStatement(List keyValues, PreparedStatement ps) throws SQLException {
		
		for(int i = 1;i<=keyValues.size();i++) {
			Object obj = keyValues.get(i-1);
			if(obj!=null){
				if(obj instanceof Calendar) {
					try{
						ps.setTimestamp(i, new Timestamp(((Calendar)obj).getTimeInMillis()));
					}catch(Exception e) {
						try{
							ps.setTime(i, new Time(((Calendar) obj).getTimeInMillis()));
						}catch(Exception ex){
							ps.setDate(i, new Date(((Calendar)obj).getTimeInMillis()));
						}
					}
				}
				else {
					ps.setObject(i, obj);
				}
			}else{
				ps.setNull(i, Types.VARCHAR);
			}
		}
	}
	
	private Map<String, Object> extractResultSetRow(ResultSet rs) throws SQLException, IOException { //NOSONAR

		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		Map<String, Object> currentMap;
		for (int i = 0; i < count; i++) {
			currentMap = row;
			int columnNumber = i + 1;
			// use column label to get the name as it also handled SQL SELECT
			// aliases
			String columnName = meta.getColumnLabel(columnNumber);
			//String tableName = meta.getTableName(columnNumber);
			String prev = null;
			for (String c : columnName.split(SEPARATOR)) {
				if (prev == null) {
					prev = c;
					continue;
				}
				HashMap<String, Object> forTheNext = null;
				if (currentMap.containsKey(prev))
					forTheNext = (HashMap<String, Object>) currentMap.get(prev);
				else {
					forTheNext = new HashMap<String, Object>();
					currentMap.put(prev, forTheNext);
				}
				currentMap = forTheNext;
				prev = c;
			}
			
			columnName = prev;
			// use index based which should be faster
			int columnType = meta.getColumnType(columnNumber);
			// If column type is blob convert the Binary stream to a byte array
			if (columnType == Types.BLOB)// && eInfo.isMediaURL()) 
			{

				InputStream inputStream = rs.getBinaryStream(columnNumber);
				ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
				int reads = 0;
				if (inputStream != null) {
					try {
						reads = inputStream.read();
					} catch (IOException e) {
						inputStream.close();
						throw e;
					}

					while (reads != -1) {
						byteArray.write(reads);
						try {
							reads = inputStream.read();
						} catch (IOException e) {
							inputStream.close();
							throw e;
						}
					}
					inputStream.close();
				}
				currentMap.put(columnName, byteArray.toByteArray());
			} else if ((columnType == Types.BOOLEAN) || (columnType == Types.BIT) || (columnType == Types.TINYINT)) {
				//FIX for CSN:1880144478. To make parity with auto exposure scenario (refer CDSql.java-->getColumnValue() method)
				currentMap.put(columnName, rs.getBoolean(columnNumber));
				if(rs.wasNull()) {
					row.put(columnName, null);
				}
			} else if (columnType == Types.CLOB || columnType == Types.NCLOB) {
				currentMap.put(columnName, rs.getString(columnNumber));
			} else if (columnType == Types.TIMESTAMP) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				currentMap.put(columnName, rs.getTimestamp(columnNumber, cal));
			} else {
				currentMap.put(columnName, rs.getObject(columnNumber));
			}
		}

		return row;
	}
	
	private EntityData getEntityDataAfterCaseChange(EntityData ed, List<String> selectedList, String entityName) {
		if(ed == null)
			return null;
		Map<String, Object> initial = ed.asMap();
		for(String selectedProp : selectedList) {
			if(initial.containsKey(selectedProp.toUpperCase())) {
				initial.put(selectedProp, initial.remove(selectedProp.toUpperCase()));
			}
		}
		return EntityData.createFromMap(initial, ((DefaultEntityData)ed).getEntityMetadata().getKeyNames(), entityName);
	}
}
