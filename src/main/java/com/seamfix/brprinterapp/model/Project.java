package com.seamfix.brprinterapp.model;

import io.gsonfire.annotations.PostDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.ArrayList;

@Entity
@Table(name = "PROJECT")
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity {


    @Column(unique = true)
    private String pId;

    @Column
    private String name;

    @Column
    private String type;

    @Column
    private String orgId;

    @Column
    private byte[] logoBytes;

    @Column
    private byte[] configBytes;

    @Column
    private Timestamp createTimestamp;

    @Column
    private String creatorEmail;

    @Column
    private boolean active;

    @Column
    private boolean useId;

    //configuration version
    @Column
    private Double version;

    //settings version
    @Column
    private Double sVersion;

    @Column
    private String locSetting;

    @Column
    private String host;

    @Column
    private String projectUseCases;

    @Column
    private String qParameters;

    @Column
    private Boolean enablePreviewCapture = true;

    @Column
    private Boolean enablePreviewFullDetails = true;

    @Column
    private Boolean enableLoc;

    @Column
    private Boolean enableBvn;

    @Column
    private String bvnCaptureFields;

    @Column
    private String bvnMode;

    @Column
    private Boolean skipBvnVal = true;

    //Non Columns
    @Transient
    private String logo;

    @Getter
    @Setter
    @Transient
    private Boolean allowPrintIdCard;

    @Transient
    private Long created;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ArrayList<String> useCases;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ArrayList<String> qParams;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ArrayList<String> bvnFields;

    public String getLogo() {
        if (this.logo == null && this.logoBytes != null) {
            String logoBytes = new String(this.logoBytes);
            this.logo = logoBytes;
        }
        return this.logo;
    }

    public Boolean getEnableLoc() {
        return enableLoc == null ? false : enableLoc;
    }

    /**
     * Ensures that the transient entities are updated after deserialization
     */
    @PostDeserialize
    private void postDeserialize() {
        this.logoBytes = logo == null ? null : logo.getBytes();
        this.createTimestamp = created == null ? null : new Timestamp(created);
        this.projectUseCases = useCases == null ? null : StringUtils.join(useCases, ",");
        this.qParameters = qParams == null ? null : StringUtils.join(qParams, ",");
        this.bvnCaptureFields = bvnFields == null ? null : StringUtils.join(bvnFields, ",");
    }

    @Override
    public int hashCode() {
        byte hashCode = 13;
        return 129 * hashCode + (this.pId == null ? 0 : this.pId.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object) || equalPid(object);
    }

    private boolean equalPid(Object object) {
        if (object == null) {
            return false;
        }
        Project other = (Project) object;
        return this.pId != null && other.pId != null && this.pId.equalsIgnoreCase(other.pId);
    }

    @Override
    public String toString() {
        return name;
    }
}
