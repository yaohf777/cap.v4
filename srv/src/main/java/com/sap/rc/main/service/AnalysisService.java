package com.sap.rc.main.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.Severity;
import com.sap.cloud.sdk.service.prov.api.operations.Create;
import com.sap.cloud.sdk.service.prov.api.operations.Delete;
import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.operations.Update;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.DeleteRequest;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
import com.sap.cloud.sdk.service.prov.api.response.DeleteResponse;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;
import com.sap.cloud.sdk.service.prov.api.response.UpdateResponse;

public class AnalysisService extends BaseService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String cdsName = "V_ANALYSIS";

	// http://localhost:8080/v4/odata/RCService/ANALYSIS
	@Query(entity = "ANALYSIS", serviceName = "RCService")
	public QueryResponse query(QueryRequest queryRequest) {

		QueryResponse queryResponse = null;
		try {
			logger.debug("Execute query on ANALYSIS");
			queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest, cdsName)).response();
		} catch (Exception e) {
			queryResponse = QueryResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return queryResponse;
	}

	// Custom Operation — no Before<Operation> or After<Operation> hooks
	// https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/94c7b69cc4584a1a9dfd9cb2da295d5e.html
	@Create(entity = "ANALYSIS", serviceName = "RCService")
	public CreateResponse create(CreateRequest request) {

		EntityData entityData = request.getData();
		if (entityData.getElementValue("NAME") == null || entityData.getElementValue("TARGET_STACK") == null
				|| entityData.getElementValue("CUSTOMER_NO") == null) {

			request.getMessageContainer().addErrorMessage("INTEGRITY_CONSTRAINT_ERROR", "CDS");
			ErrorResponse errorResponse = ErrorResponse.getBuilder().setStatusCode(500)
					.addContainerMessages(Severity.ERROR).response();
			return CreateResponse.setError(errorResponse);
		}

		return createAnalysis(request);
	}

	// http://localhost:8080/odata/RCService/ANALYSIS('100002')
	@Read(entity = "ANALYSIS", serviceName = "RCService")
	public ReadResponse getAnalysis(ReadRequest readRequest) {

		ReadResponse readResponse = null;
		try {
			logger.debug("Execute read on ANALYSIS");
			readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest, cdsName)).response();
		} catch (Exception e) {
			readResponse = ReadResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return readResponse;
	}

	@Update(entity = "ANALYSIS", serviceName = "RCService")
	public UpdateResponse updateAnalysis(UpdateRequest updateRequest) {
		UpdateResponse updateResponse = null;
		try {
			updateEntity(updateRequest);
			updateResponse = UpdateResponse.setSuccess().response();
		} catch (Exception e) {
			updateResponse = UpdateResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return updateResponse;
	}

	@Delete(entity = "ANALYSIS", serviceName = "RCService")
	public DeleteResponse delete(DeleteRequest deleteRequest) {
		DeleteResponse deleteResponse = null;
		try {
			deleteEntity(deleteRequest);
			deleteResponse = DeleteResponse.setSuccess().response();
		} catch (Exception e) {
			deleteResponse = DeleteResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setStatusCode(500).response());
		}
		return deleteResponse;
	}

	private CreateResponse createAnalysis(CreateRequest request) {

		EntityData entityData = request.getData();
		// https://gist.github.com/umbertogriffo/956f3dacc7fbeee0b65a264e8b003044
		UUID analysisID = UUID.randomUUID();
		EntityData entityDataNew = EntityData.getBuilder(entityData).addElement("ID", analysisID.toString())
				.addElement("CREATED_AT", now()).buildEntityData("ANALYSIS");
		return createEntity(request, entityDataNew);
	}

}