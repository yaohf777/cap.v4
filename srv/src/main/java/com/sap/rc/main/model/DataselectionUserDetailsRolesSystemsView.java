package com.sap.rc.main.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@SapSemantics(semantics = "aggregate")
@Entity
@Table(name="DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW")
public class DataselectionUserDetailsRolesSystemsView {

	@Id
	@Column(name="ROW_ID")
	private String rowId;
	
	@Column(name="USER_ID", insertable=false, updatable=false)
	//@SapAggregationRole(aggregationRole = "dimension")
	private String userId;
	
	@Column(name="CONNECTOR")
	//@SapAggregationRole(aggregationRole = "dimension")
	private String connector;

	@Column(name="PRIVILEGE")
	//@SapAggregationRole(aggregationRole = "dimension")
	private String role;

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}