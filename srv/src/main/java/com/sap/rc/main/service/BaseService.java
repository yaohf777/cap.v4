package com.sap.rc.main.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.hana.connectivity.cds.CDSException;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSQuery;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder.OrderByBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder.SelectColumnBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder.WhereBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryResult;
import com.sap.cloud.sdk.hana.connectivity.cds.ConditionBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.ConditionBuilder.Condition;
import com.sap.cloud.sdk.hana.connectivity.handler.CDSDataSourceHandler;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.service.prov.api.DataSourceHandler;
import com.sap.cloud.sdk.service.prov.api.DatasourceExceptionType;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.EntityMetadata;
import com.sap.cloud.sdk.service.prov.api.Severity;
import com.sap.cloud.sdk.service.prov.api.internal.HasMetadata;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.DeleteRequest;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.request.Request;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.rt.cds.CDSHandler;
import com.sap.cloud.sdk.service.prov.v4.request.impl.QueryRequestV4Impl;
import com.sap.rc.main.multitenancy.TenantContext;

//@formatter:off
/**
* To provide oData V4 service through  com.sap.cloud.sdk.service.prov.v4:
* 1. Specify oData metadata in resources/edmx/RCService.xml which is parsed in ODataApplicationInitializer.java
*    through InitializerHelper.java
*    The oData metadata Namespace has to be identical as the context in the data_model.hdbcds since
*    the Namespace is used as DB artifacts schema. 
*    Use CustomCDSDataSourceHandler.java (sub-class of CDSHandler.java) to override default behavior
* 2. WebAppContextConfig.java defines the ODataServlet to listen to oData request for mapped URL. 
*    Then the OData service can be consumed through URL /odata/RCService.
*    The oData service is initialized in CloudSDKODataServiceFactory
* 3. Package containing the OData service implementation code is defined in application.properties
*    and add to servlet context in MainApplication.java. 
*    The package name is read in ODataApplicationInitializer.java and passed to ClassHelper.java
*    The information is used in com.sap.cloud.sdk.service.prov.v4.custom.dataprovider.CustomDataProvider.java
* 4. Only use names in UPPER CASE in RCService.xml to prevent issue in oData service CDSQuery which is case sensitive.
* 5. Disable exec-maven-plugin of org.codehaus.mojo in pom.xml to prevent build failure which comes from
*    com.sap.cloud.sdk.service.prov.v4.validator.MavenAgent.doValidation(MavenAgent.java:50)
*	 This can simplify the setup of oData service, otherwise we have to specify parameter ${packageName} in pom.xml 
*    and then provide it again as context-param in web.xml. 
* 
* com.sap.cloud.sdk.service.prov.v4.rt.cdx.CDSDataProvider
* com.sap.cloud.sdk.service.prov.v4.rt.cdx.CDXRuntimeDelegate
* com.sap.cloud.sdk.service.prov.v4.rt.cdx.CDXEdmProvider
* 
* http://localhost:8080/v4/odata/RCService/$metadata
* http://localhost:8080/v4/odata/RCService/UserSystemRole
* 
* com.sap.cloud.sdk.service.prov.v4.util.ProcessorHelper.getAnnotatedOperation(ProcessorHelper.java:246)
* com.sap.cloud.sdk.service.prov.v4.util.ProcessorHelper.getClassMethodtoCall(ProcessorHelper.java:187)
* com.sap.cloud.sdk.service.prov.v4.util.ProcessorHelper.invokeOperation(ProcessorHelper.java:82)
* com.sap.cloud.sdk.service.prov.v4.custom.dataprovider.CustomDataProvider.getEntityCollectionForQuery(CustomDataProvider.java:375)
* com.sap.cloud.sdk.service.prov.v4.custom.dataprovider.CustomDataProvider.readEntityCollection(CustomDataProvider.java:328)
* com.sap.cloud.sdk.service.prov.v4.rt.core.GenericODataProcessor.readEntityCollection(GenericODataProcessor.java:362)
* 
* Additional reference can be found in 
* https://wiki.wdf.sap.corp/wiki/display/webapptoolkit/Add+Custom+Handlers+for+Service+Entities+from+local+DB
*/
//@formatter:on

public class BaseService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	//http://localhost:8080/v4/odata/RCService/UserSystemRole?$orderby=USER_ID%20desc&$top=10&$skip=1
	protected List<EntityData> getEntitySet(QueryRequest queryRequest, String cdsName) throws Exception {

		// String fullName = getNamespace(request) + "." + cdsName;
		try {
			// CDSQuery cdsQuery = new CDSSelectQueryBuilder(cdsName).build();

			// https://github.wdf.sap.corp/CEC-MKT-EXT/marketing-insight/blob/master/service/application/src/main/java/com/sap/cec/mkt/insight/util/QueryUtil.java
			// https://github.wdf.sap.corp/I035787/ErrorMessage/blob/master/srv/src/main/java/util/DataHandler.java
			// https://github.wdf.sap.corp/iot-mac/offroad.documentation/wiki/Custom-Java-implementations-for-OData-with-CDS
			
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(cdsName)
					.top(queryRequest.getTopOptionValue() > 0 ? queryRequest.getTopOptionValue() : 100)
					.skip(queryRequest.getSkipOptionValue() > 0 ? queryRequest.getSkipOptionValue() : 0).build();

//					.selectColumns(queryRequest.getSelectProperties().toArray(new String[0])).build();
//			CDSQuery cdsQuery = new CDSSelectQueryBuilder("MySalesOrderService.SalesOrderHeader")
//                    .top(12)
//                    .skip(5)
//                    .selectColumns("SalesOrderId", "NetAmount", "TaxAmount", "CurrencyCode")
//                    .where(new ConditionBuilder().columnName("CurrencyCode").IN("INR", "USD"))
//                    .orderBy("NetAmount", true)
//                    .build();
			CDSSelectQueryResult cdsSelectQueryResult = getCDSHandler(queryRequest).executeQuery(cdsQuery);
			return cdsSelectQueryResult.getResult();
		} catch (CDSException e) {
			logger.error("==> Exception while fetching query data from CDS: " + e.getMessage());
			throw e;
		}
	}

	protected EntityData readEntity(ReadRequest request) throws Exception {

		return readEntity(request, getCdsName(request));
	}

	protected EntityData readEntity(ReadRequest request, String cdsName) throws Exception {

		try {
			return getCDSHandler(request).executeRead(cdsName, request.getKeys(),
					request.getEntityMetadata().getElementNames());
		} catch (CDSException e) {
			logger.error("Exception while reading an entity in CDS: " + e.getMessage());
			throw e;
		}
	}

	protected void updateEntity(UpdateRequest request) throws Exception {

		try {
			getCDSHandler(request).executeUpdate(request.getData(), request.getKeys(), false);
		} catch (CDSException e) {
			logger.error("Exception while updatiing an entity in CDS: " + e.getMessage());
			throw e;
		}
	}

	protected void deleteEntity(DeleteRequest request) throws Exception {

		try {
			getCDSHandler(request).executeDelete(request.getEntityMetadata().getName(), request.getKeys());
		} catch (CDSException e) {
			logger.error("Exception while deleting an entity in CDS: " + e.getMessage());
			throw e;
		}
	}

	protected CreateResponse createEntity(CreateRequest request, EntityData entityData) {

		try {

			entityData = getCDSHandler(request).executeInsert(entityData, true);
		} catch (CDSException e) {
			logger.error("Exception while creating an entity in CDS: " + e.getMessage());
			ErrorResponse errorResponse = null;
			if (e.getType().equals(DatasourceExceptionType.INTEGRITY_CONSTRAINT_VIOLATION)) {
				request.getMessageContainer().addErrorMessage("INTEGRITY_CONSTRAINT_ERROR", "CDS");
				errorResponse = ErrorResponse.getBuilder().setStatusCode(500).addContainerMessages(Severity.ERROR)
						.response();
			} else if (e.getType().equals(DatasourceExceptionType.DATABASE_CONNECTION_ERROR)) {
				request.getMessageContainer().addErrorMessage("DATABASE_CONNECTION_ERROR", "CDS");
				errorResponse = ErrorResponse.getBuilder().setStatusCode(500).addContainerMessages(Severity.ERROR)
						.response();
			} else {
				errorResponse = ErrorResponse.getBuilder().setStatusCode(500)
						.setMessage("Exception during CDS create operation: " + e.getMessage()).response();
			}
			return CreateResponse.setError(errorResponse);
		} catch (Exception e) {
			logger.error("==> Exception while creating an entity in CDS: " + e.getMessage());
			return CreateResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response());
		}

		return CreateResponse.setSuccess().setData(entityData).response();
	}

	protected CDSDataSourceHandler getCDSHandler(Request request) throws SQLException {

		return new CustomCDSDataSourceHandler(TenantContext.getDatasource().getConnection(), getNamespace(request));

//		return DataSourceHandlerFactory.getInstance().getCDSHandler(TenantContext.getDatasource().getConnection(),
//				getNamespace(request));
	}

	protected CDSDataSourceHandler getCDSHandler(EntityData entityData) throws SQLException {

		EntityMetadata entityMeta = ((HasMetadata) entityData).getEntityMetadata();
		return new CustomCDSDataSourceHandler(TenantContext.getDatasource().getConnection(), entityMeta.getNamespace());

//		EntityMetadata entityMeta = ((HasMetadata)entityData).getEntityMetadata();
//		return DataSourceHandlerFactory.getInstance().getCDSHandler(TenantContext.getDatasource().getConnection(),
//				entityMeta.getNamespace());
	}

	protected String getCdsName(Request request) {

		return request.getEntityMetadata().getName();
	}

	protected String getNamespace(Request request) {

		return request.getEntityMetadata().getNamespace();
	}

	protected Timestamp now() {
		return new Timestamp(new Date().getTime());
	}

	
	// https://github.wdf.sap.corp/I035787/ErrorMessage/blob/master/srv/src/main/java/util/DataHandler.java
	public List<EntityData> fetchResult(String serviceName, String entityName, Map<String, Object> selectColumns,
			Map<String, Object> whereClauseColumns, DataSourceHandler handler, String columnName, boolean isDescending, boolean isAnd) {
		
		CDSQuery query = null;
		String tableName = entityName;
		CDSSelectQueryBuilder querybuilder = new CDSSelectQueryBuilder(tableName);
		SelectColumnBuilder selectQuery = getSelectQuery(querybuilder, selectColumns);
		WhereBuilder whereQuery = getWhereQuery(querybuilder, selectQuery, whereClauseColumns, isAnd);
		OrderByBuilder orderByQuery = getOrderBy(querybuilder, whereQuery, columnName, isDescending);
		
		// Constructing the query on the table
		if (orderByQuery != null){
			query = orderByQuery.build();
		}
		else if (whereQuery != null) {
			query = whereQuery.build();
		} else if (selectQuery != null) {
			query = selectQuery.build();
		} else {
			query = querybuilder.build();
		}
		List<EntityData> resultData = null;
		// executing the query on the table.
		try {
			resultData = ((CDSHandler) handler).executeQuery(query).getResult();
		} catch (CDSException e) {
			logger.error("Reading entity records failed with query " + query.toString(), e);
		} catch (ClassCastException e) {
			logger.error("Reading entity records failed with query " + query.toString(), e);
		}
		return resultData;
	}

	/**
	 * Private function to get selectClause
	 * 
	 * @param CDSSelectQueryBuilder querybuilder
	 * @param                       Map<String, Object> selectColumns
	 * @return SelectColumnBuilder selectQuery
	 */
	private SelectColumnBuilder getSelectQuery(CDSSelectQueryBuilder querybuilder, Map<String, Object> selectColumns) {
		SelectColumnBuilder selectQuery = null;
		if (selectColumns != null && !selectColumns.isEmpty()) {
			List<String> columns = new ArrayList<>(selectColumns.keySet());
			// Constructing the select query on the table
			selectQuery = querybuilder.selectColumns(columns.toArray(new String[0]));
		}
		return selectQuery;
	}

	/**
	 * Private function to get whereClause
	 * 
	 * @param CDSSelectQueryBuilder querybuilder
	 * @param SelectColumnBuilder   selectQuery
	 * @param                       Map<String, Object> whereClauseColumns
	 * @return WhereBuilder whereQuery
	 */
	private WhereBuilder getWhereQuery(CDSSelectQueryBuilder querybuilder, SelectColumnBuilder selectQuery,
			Map<String, Object> whereClauseColumns, boolean isAnd) {
		
		WhereBuilder whereQuery = null;
		if (whereClauseColumns != null && !whereClauseColumns.isEmpty()) {
			List<Condition> conditions = new ArrayList<>();

			// Building the condtitons here like "where APP_NAME = value"
			for (Map.Entry<String, Object> entry : whereClauseColumns.entrySet()) {
				conditions.add(new ConditionBuilder().columnName(entry.getKey()).EQ(entry.getValue()));
			}
			Condition whereClause = conditions.get(0);
			if (conditions.size() > 1) {
				for (int i = 1; i < conditions.size(); i++) {
					if(isAnd){
						whereClause = whereClause.AND(conditions.get(i));
					}
					else{
						whereClause = whereClause.OR(conditions.get(i));
					}
				}
			}
			// Constructing the where query on the table
			whereQuery = selectQuery != null ? selectQuery.where(whereClause) : querybuilder.where(whereClause);
		}
		return whereQuery;
	}

	private OrderByBuilder getOrderBy(CDSSelectQueryBuilder querybuilder, WhereBuilder whereQuery, String columnName, boolean isDescending){
        OrderByBuilder obquery = null;
        obquery = whereQuery != null ? whereQuery.orderBy(columnName, isDescending) : querybuilder.orderBy(columnName, isDescending);
		return obquery;
	}
	
	/** Build OData query **/
	// https://github.wdf.sap.corp/CEC-MKT-EXT/marketing-insight/blob/master/service/application/src/main/java/com/sap/cec/mkt/insight/util/QueryUtil.java
	public static ODataQueryBuilder createQueryBuilder(QueryRequest queryRequest, String servicePath, String entity,
			String addlFilterQuery, String sortQuery, boolean addPagination) {

		ODataQueryBuilder queryBuilder = ODataQueryBuilder.withEntity(servicePath, entity);

		String filterString = buildFilterString(queryRequest, addlFilterQuery);
		if (StringUtils.isNotBlank(filterString)) {
			queryBuilder.param("$filter", filterString);
		}

		if (StringUtils.isNotBlank(sortQuery)) {
			OrderByOption orderByOption = ((QueryRequestV4Impl) queryRequest).getUriInfo().getOrderByOption();
			if (orderByOption != null) {
				queryBuilder.param("$orderby", orderByOption.getText());
			}
		}
		if (!queryRequest.getSelectProperties().isEmpty()) {
			queryBuilder.select(queryRequest.getSelectProperties().toArray(new String[0]));
		}
		if (addPagination) {
			queryBuilder.skip(queryRequest.getSkipOptionValue() > 0 ? queryRequest.getSkipOptionValue() : 0)
					.top(queryRequest.getTopOptionValue() > 0 ? queryRequest.getTopOptionValue() : 100);
		}

		return queryBuilder;
	}

	private static String buildFilterString(QueryRequest queryRequest, String addlFilterQuery) {

		String filterString = null;

		FilterOption filterOption = ((QueryRequestV4Impl) queryRequest).getUriInfo().getFilterOption();
		if (filterOption != null) {
			String filterQuery = ((QueryRequestV4Impl) queryRequest).getUriInfo().getFilterOption().getText();
			if (filterQuery != null) {
				if (filterQuery.contains("contains(")) {
					filterQuery = getV2SearchFilterQuery(filterQuery);
				}
				filterString = filterQuery.replaceAll("\\{", "(").replaceAll("\\}", ")").replace(" EQ ", " eq ")
						.replace(" AND ", " and ").replace(" OR ", " or ");
			}
		}

//		if (StringUtils.isNotBlank(addlFilterQuery)) {
//			if (StringUtils.isNotBlank(filterString)) {
//				filterString = filterString.concat(" and ").concat(addlFilterQuery);
//			} else {
//				filterString = addlFilterQuery;
//			}
//		}
		return filterString;
	}

	private static String getV2SearchFilterQuery(String filterQuery) {

		String searchfilterQuery = filterQuery;
		String v4Query = filterQuery.substring(filterQuery.indexOf("contains("), filterQuery.indexOf(")") + 1);
		String qString = v4Query.substring(v4Query.indexOf("contains(") + 9, v4Query.indexOf(")"));
		String[] parts = qString.split(",");
		String field = parts[0];
		String value = parts[1];
		String v2Query = "substringof(" + value + "," + field + ") eq true";
		searchfilterQuery = filterQuery.replace(v4Query, v2Query);

		return searchfilterQuery;
	}
}