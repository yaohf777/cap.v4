package com.sap.rc.sample;

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
@Table(name = "sample_advmnt")
@Multitenant
@TenantDiscriminatorColumn(name = "TENANT", contextProperty = TenantContext.TENANT_ID, length = 36)
public class Advertisement {
    
	@Id
	@GeneratedValue(generator = "ADVMNT_ID")
	@TableGenerator(name = "ADVMNT_ID", table = "SEQUENCE_NUM", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "ADVERTISEMENT", allocationSize = 10)
	private Long id;

    @NotBlank
    @Column(name = "title")
    private String title;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "changed_at",insertable = false)
    private Timestamp modifiedAt;

    @Version
    private int version;

    public Advertisement() {
    }

    public Advertisement(String title) {
        this.title = title;
    }

    public Advertisement(Long id, int version) {
        this.id = id;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public Timestamp getCreatedAt() {
        if (createdAt != null) {
            return new Timestamp(createdAt.getTime());
        }
        return null;
    }

    protected void setCreatedAt(Timestamp timestamp) {
        this.createdAt = timestamp;
    }

    public Timestamp getModifiedAt() {
        if (modifiedAt != null) {
            return new Timestamp(modifiedAt.getTime());
        }
        return null;
    }

    public int getVersion() {
        return version;
    }

    @PrePersist // called during INSERT
    protected void onPersist() {
        setCreatedAt(now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = now();
    }

    protected static Timestamp now() {
        return new Timestamp(new Date().getTime());
    }

    protected void setUpdatedAt(Timestamp timestamp) {
        modifiedAt = timestamp;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "Advertisement [id=" + id + ", title=" + title + "]";
    }

}
