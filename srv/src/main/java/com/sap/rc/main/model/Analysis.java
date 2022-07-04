package com.sap.rc.main.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;
import org.hibernate.validator.constraints.NotBlank;

import com.sap.rc.main.multitenancy.TenantContext;

@Entity
@Table(name = "ANALYSIS") // @Table(name = "analysis_header")
@Multitenant
@TenantDiscriminatorColumn(name = "TENANT", contextProperty = TenantContext.TENANT_ID, length = 36)
public class Analysis {

	// "id": "888880000004681",
	// "name": "Test 01",
	// "customerNo": "33018",
	// "customerName": "SAP SE",
	// "createdBy": "S*",
	// "countryName": "Germany",
	// "createdAt": "1525936513000",
	// "createdatDate": "1525910400000",
	// "analysisType": "S4_HANA",
	// "source": "Manual",
	// "sourceDesc": "Manual Upload",
	// "status": "R",
	// "statusDesc": "Ready",
	// "targetPrdVersion": "73555000100900001152",
	// "targetPrdVersionText": "SAP S/4HANA 1709",
	// "targetStack": "73554900103300003158",
	// "targetStackText": "01 (01/2018) FP"

	@Id
	@GeneratedValue(generator = "ANALYSIS_ID")
	@TableGenerator(name = "ANALYSIS_ID", table = "SEQUENCE_NUM", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "ANALYSIS", allocationSize = 10)
	private Long id;

	@NotBlank
	@Column(name = "name", nullable = false, length = 60)
	private String name;

	@NotBlank
	@Column(name = "customer_no", nullable = false, length = 10)
	private String customerNo;

	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;

	@NotBlank
	@Column(name = "created_by", nullable = false, length = 12)
	private String createdBy;

	// @Transient
	// @JsonInclude()
	// private String createdAtTime;

	@Column(name = "changed_at")
	private Timestamp changedAt;

	@Column(name = "changed_by")
	private Timestamp changedBy;

	@NotBlank
	@Column(name = "target_stack", length = 20)
	private String targetStack;

	@Column(name = "server", length = 30)
	private String server;

	public String getTargetStack() {
		return targetStack;
	}

	public void setTargetStack(String targetStack) {
		this.targetStack = targetStack;
	}

	@Column(name = "status", nullable = false, length = 1)
	private String status;

	@Version
	private int version;

	public Analysis() {
	}

	public Analysis(Long id, int version) {
		this.id = id;
		this.version = version;
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

	public String getCustomerName() {
		// TODO
		String customerName = "SAP SE";
		return customerName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCountryName() {
		// TODO
		String countryName = "Germany";
		return countryName;
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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