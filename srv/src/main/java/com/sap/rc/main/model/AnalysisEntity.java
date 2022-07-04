package com.sap.rc.main.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.sap.cloud.sdk.result.ElementName;


public class AnalysisEntity {

	@ElementName("ID")
	private Long id;

	@ElementName("Name")
	private String name;

	@ElementName("Customer_No")
	private String customerNo;

	@ElementName("Created_At")
	private Timestamp createdAt;

	@ElementName("Created_by")
	private String createdBy;

	// @Transient
	// @JsonInclude()
	// private String createdAtTime;

	@ElementName("Changed_At")
	private Timestamp changedAt;

	@ElementName("Changed_by")
	private Timestamp changedBy;

	@ElementName("Target_Stack")
	private String targetStack;

	@ElementName("Server")
	private String server;

	@ElementName("Status")
	private String status;

	public String getTargetStack() {
		return targetStack;
	}

	public void setTargetStack(String targetStack) {
		this.targetStack = targetStack;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public String getCreatedAtTime() {
		if (createdAt != null) {
			return createdAt.toLocalDateTime().toString();
		}
		return null;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDesc() {
		// TODO
		String statusDesc = "Ready";
		return statusDesc;
	}

	public void setChangedAt(Timestamp changedAt) {
		this.changedAt = changedAt;
	}

	public Timestamp getChangedAt() {
		if (changedAt != null) {
			return new Timestamp(changedAt.getTime());
		}
		return null;
	}


	public Timestamp getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(Timestamp changedBy) {
		this.changedBy = changedBy;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@PrePersist // called during INSERT
	protected void onPersist() {
		this.createdAt = now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.changedAt = now();
	}

	protected static Timestamp now() {
		return new Timestamp(new Date().getTime());
	}

	@Override
	public String toString() {
		return "Analysis [id=" + id + ", name=" + name + "]";
	}

}