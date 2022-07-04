package com.sap.rc.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;

public class DataselectionService extends BaseService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String cdsName = "DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW";

	@Query(entity = "UserSystemRole", serviceName = "RCService")
	// http://localhost:8080/v4/odata/RCService/UserSystemRole
	// http://localhost:8080/v4/odata/RCService/UserSystemRole?$orderby=USER_ID desc&$filter=PRIVILEGE eq 'ZRSTEST'
	public QueryResponse query(QueryRequest queryRequest) {

		QueryResponse queryResponse = null;
		try {
			logger.debug("Execute query on UserSystemRole");
			queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest, cdsName)).response();
		} catch (Exception e) {
			queryResponse = QueryResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return queryResponse;
	}

	// http://localhost:8080/v4/odata/RCService/UserSystemRole('100002')
	@Read(entity = "UserSystemRole", serviceName = "RCService")
	public ReadResponse getAnalysis(ReadRequest readRequest) {

		ReadResponse readResponse = null;
		try {
			logger.debug("Execute read on UserSystemRole");
			readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest, cdsName)).response();
		} catch (Exception e) {
			readResponse = ReadResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return readResponse;
	}
}