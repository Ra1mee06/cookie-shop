package com.cookieshop.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_invites")
public class AdminInvite extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "used_by")
    private User usedBy;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getUsedBy() { return usedBy; }
    public void setUsedBy(User usedBy) { this.usedBy = usedBy; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}


