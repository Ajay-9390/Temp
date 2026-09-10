package com.nba.attainment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for persisted PSO attainment results.
 *
 * <p>Mirrors {@link POAttainment} in structure. Database ownership:
 * this module owns {@code pso_attainment}.
 */
@Entity
@Table(
    name = "pso_attainment",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pso_attainment_current",
        columnNames = {"program_id", "pso_id", "academic_year", "calculation_version"}
    )
)
public class PSOAttainment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "program_id", nullable = false, updatable = false)
    private UUID programId;

    /** PSO identifier — owned by the PO/PSO Management module. Stored as reference only. */
    @Column(name = "pso_id", nullable = false, updatable = false)
    private UUID psoId;

    @Column(name = "pso_code", nullable = false, length = 20)
    private String psoCode;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "direct_attainment")
    private Double directAttainment;

    @Column(name = "indirect_attainment")
    private Double indirectAttainment;

    @Column(name = "final_attainment")
    private Double finalAttainment;

    @Column(name = "calculation_method", nullable = false, length = 50)
    private String calculationMethod;

    @Column(name = "calculation_version", nullable = false, length = 20)
    private String calculationVersion;

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

    protected PSOAttainment() {}

    private PSOAttainment(Builder builder) {
        this.programId          = builder.programId;
        this.psoId              = builder.psoId;
        this.psoCode            = builder.psoCode;
        this.academicYear       = builder.academicYear;
        this.directAttainment   = builder.directAttainment;
        this.indirectAttainment = builder.indirectAttainment;
        this.finalAttainment    = builder.finalAttainment;
        this.calculationMethod  = builder.calculationMethod;
        this.calculationVersion = builder.calculationVersion;
        this.calculationTrace   = builder.calculationTrace;
    }

    public static Builder builder() { return new Builder(); }

    public UUID    getId()                 { return id; }
    public UUID    getProgramId()          { return programId; }
    public UUID    getPsoId()              { return psoId; }
    public String  getPsoCode()            { return psoCode; }
    public String  getAcademicYear()       { return academicYear; }
    public Double  getDirectAttainment()   { return directAttainment; }
    public Double  getIndirectAttainment() { return indirectAttainment; }
    public Double  getFinalAttainment()    { return finalAttainment; }
    public String  getCalculationMethod()  { return calculationMethod; }
    public String  getCalculationVersion() { return calculationVersion; }
    public String  getCalculationTrace()   { return calculationTrace; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }

    public void setDirectAttainment(Double v)   { this.directAttainment   = v; }
    public void setIndirectAttainment(Double v) { this.indirectAttainment = v; }
    public void setFinalAttainment(Double v)    { this.finalAttainment    = v; }
    public void setCalculationTrace(String v)   { this.calculationTrace   = v; }
    public void setPsoCode(String v)            { this.psoCode            = v; }

    public static final class Builder {
        private UUID    programId;
        private UUID    psoId;
        private String  psoCode;
        private String  academicYear;
        private Double  directAttainment;
        private Double  indirectAttainment;
        private Double  finalAttainment;
        private String  calculationMethod;
        private String  calculationVersion;
        private String  calculationTrace;

        public Builder programId(UUID v)           { this.programId          = v; return this; }
        public Builder psoId(UUID v)               { this.psoId              = v; return this; }
        public Builder psoCode(String v)           { this.psoCode            = v; return this; }
        public Builder academicYear(String v)      { this.academicYear       = v; return this; }
        public Builder directAttainment(Double v)  { this.directAttainment   = v; return this; }
        public Builder indirectAttainment(Double v){ this.indirectAttainment = v; return this; }
        public Builder finalAttainment(Double v)   { this.finalAttainment    = v; return this; }
        public Builder calculationMethod(String v)  { this.calculationMethod  = v; return this; }
        public Builder calculationVersion(String v) { this.calculationVersion = v; return this; }
        public Builder calculationTrace(String v)  { this.calculationTrace   = v; return this; }

        public PSOAttainment build() { return new PSOAttainment(this); }
    }
}
