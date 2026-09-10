package com.nba.attainment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for persisted PO attainment results.
 *
 * <p>Database ownership: this module owns {@code po_attainment}.
 * It does NOT own {@code program_outcome} or any other module's table.
 * poId is stored as a plain UUID — no FK constraint crossing module
 * ownership boundaries.
 *
 * <p>The {@code calculationTrace} column is JSONB in PostgreSQL,
 * allowing full drill-down without additional joins.
 */
@Entity
@Table(
    name = "po_attainment",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_po_attainment_current",
        columnNames = {"program_id", "po_id", "academic_year", "calculation_version"}
    )
)
public class POAttainment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "program_id", nullable = false, updatable = false)
    private UUID programId;

    /** PO identifier — owned by the PO/PSO Management module. Stored as reference only. */
    @Column(name = "po_id", nullable = false, updatable = false)
    private UUID poId;

    @Column(name = "po_code", nullable = false, length = 20)
    private String poCode;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "direct_attainment")
    private Double directAttainment;

    /**
     * Indirect attainment — null until the indirect calculation feature is wired.
     * Column exists in schema to avoid a future migration.
     */
    @Column(name = "indirect_attainment")
    private Double indirectAttainment;

    @Column(name = "final_attainment")
    private Double finalAttainment;

    @Column(name = "calculation_method", nullable = false, length = 50)
    private String calculationMethod;

    @Column(name = "calculation_version", nullable = false, length = 20)
    private String calculationVersion;

    /**
     * Full JSON trace stored as JSONB.
     * Serialised from {@link com.nba.attainment.dto.calculation.AttainmentCalculationResult.CalculationTrace}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_trace", columnDefinition = "jsonb")
    private String calculationTrace;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected POAttainment() {}

    private POAttainment(Builder builder) {
        this.programId          = builder.programId;
        this.poId               = builder.poId;
        this.poCode             = builder.poCode;
        this.academicYear       = builder.academicYear;
        this.directAttainment   = builder.directAttainment;
        this.indirectAttainment = builder.indirectAttainment;
        this.finalAttainment    = builder.finalAttainment;
        this.calculationMethod  = builder.calculationMethod;
        this.calculationVersion = builder.calculationVersion;
        this.calculationTrace   = builder.calculationTrace;
    }

    public static Builder builder() { return new Builder(); }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID    getId()                 { return id; }
    public UUID    getProgramId()          { return programId; }
    public UUID    getPoId()               { return poId; }
    public String  getPoCode()             { return poCode; }
    public String  getAcademicYear()       { return academicYear; }
    public Double  getDirectAttainment()   { return directAttainment; }
    public Double  getIndirectAttainment() { return indirectAttainment; }
    public Double  getFinalAttainment()    { return finalAttainment; }
    public String  getCalculationMethod()  { return calculationMethod; }
    public String  getCalculationVersion() { return calculationVersion; }
    public String  getCalculationTrace()   { return calculationTrace; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }

    // ── Setters for updates ───────────────────────────────────────────────────

    public void setDirectAttainment(Double directAttainment)     { this.directAttainment   = directAttainment; }
    public void setIndirectAttainment(Double indirectAttainment) { this.indirectAttainment = indirectAttainment; }
    public void setFinalAttainment(Double finalAttainment)       { this.finalAttainment    = finalAttainment; }
    public void setCalculationTrace(String calculationTrace)     { this.calculationTrace   = calculationTrace; }
    public void setPoCode(String poCode)                         { this.poCode             = poCode; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {
        private UUID    programId;
        private UUID    poId;
        private String  poCode;
        private String  academicYear;
        private Double  directAttainment;
        private Double  indirectAttainment;
        private Double  finalAttainment;
        private String  calculationMethod;
        private String  calculationVersion;
        private String  calculationTrace;

        public Builder programId(UUID v)          { this.programId          = v; return this; }
        public Builder poId(UUID v)               { this.poId               = v; return this; }
        public Builder poCode(String v)           { this.poCode             = v; return this; }
        public Builder academicYear(String v)     { this.academicYear       = v; return this; }
        public Builder directAttainment(Double v) { this.directAttainment   = v; return this; }
        public Builder indirectAttainment(Double v){ this.indirectAttainment = v; return this; }
        public Builder finalAttainment(Double v)  { this.finalAttainment    = v; return this; }
        public Builder calculationMethod(String v) { this.calculationMethod  = v; return this; }
        public Builder calculationVersion(String v){ this.calculationVersion = v; return this; }
        public Builder calculationTrace(String v) { this.calculationTrace   = v; return this; }

        public POAttainment build() { return new POAttainment(this); }
    }
}
