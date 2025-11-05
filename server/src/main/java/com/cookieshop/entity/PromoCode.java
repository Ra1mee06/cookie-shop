package com.cookieshop.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PromoType type;

    @Column(name = "value")
    private BigDecimal value; // percent (0-100) or amount depending on type

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product; // for PRODUCT_PERCENT or buy2get1 specific product

    @Column(name = "max_uses")
    private Integer maxUses; // null means unlimited

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // optional extra data (e.g., gift certificate target)

    public enum PromoType {
        ORDER_PERCENT,
        PRODUCT_PERCENT,
        BUY2GET1,
        GIFT_CERTIFICATE
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public PromoType getType() { return type; }
    public void setType(PromoType type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}


