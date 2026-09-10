package com.nba.attainment.provider.mock;

import java.util.UUID;

/**
 * Fixed UUIDs and constants shared across all mock providers.
 *
 * <p>Using deterministic UUIDs lets tests reference predictable IDs
 * without needing to look them up. Replace the entire mock layer by
 * swapping the Spring beans — nothing else changes.
 */
public final class MockDataConstants {

    private MockDataConstants() {}

    // ── Program ──────────────────────────────────────────────────────────────
    public static final UUID PROGRAM_ID =
            UUID.fromString("11111111-0000-0000-0000-000000000001");
    public static final String PROGRAM_NAME = "B.Tech Computer Science & Engineering";
    public static final String ACADEMIC_YEAR = "2023-24";

    // ── Courses ───────────────────────────────────────────────────────────────
    public static final UUID COURSE_DS_ID =
            UUID.fromString("22222222-0000-0000-0000-000000000001");
    public static final String COURSE_DS_NAME = "Data Structures";

    public static final UUID COURSE_OOP_ID =
            UUID.fromString("22222222-0000-0000-0000-000000000002");
    public static final String COURSE_OOP_NAME = "Object-Oriented Programming";

    public static final UUID COURSE_ALGO_ID =
            UUID.fromString("22222222-0000-0000-0000-000000000003");
    public static final String COURSE_ALGO_NAME = "Design & Analysis of Algorithms";

    // ── COs — Data Structures ─────────────────────────────────────────────────
    public static final UUID DS_CO1_ID =
            UUID.fromString("33333333-0001-0000-0000-000000000001");
    public static final UUID DS_CO2_ID =
            UUID.fromString("33333333-0001-0000-0000-000000000002");
    public static final UUID DS_CO3_ID =
            UUID.fromString("33333333-0001-0000-0000-000000000003");

    // ── COs — OOP ────────────────────────────────────────────────────────────
    public static final UUID OOP_CO1_ID =
            UUID.fromString("33333333-0002-0000-0000-000000000001");
    public static final UUID OOP_CO2_ID =
            UUID.fromString("33333333-0002-0000-0000-000000000002");
    public static final UUID OOP_CO3_ID =
            UUID.fromString("33333333-0002-0000-0000-000000000003");

    // ── COs — Algorithms ─────────────────────────────────────────────────────
    public static final UUID ALGO_CO1_ID =
            UUID.fromString("33333333-0003-0000-0000-000000000001");
    public static final UUID ALGO_CO2_ID =
            UUID.fromString("33333333-0003-0000-0000-000000000002");
    public static final UUID ALGO_CO3_ID =
            UUID.fromString("33333333-0003-0000-0000-000000000003");

    // ── POs (NBA standard 12 POs) ─────────────────────────────────────────────
    public static final UUID PO1_ID  = UUID.fromString("44444444-0000-0000-0000-000000000001");
    public static final UUID PO2_ID  = UUID.fromString("44444444-0000-0000-0000-000000000002");
    public static final UUID PO3_ID  = UUID.fromString("44444444-0000-0000-0000-000000000003");
    public static final UUID PO4_ID  = UUID.fromString("44444444-0000-0000-0000-000000000004");
    public static final UUID PO5_ID  = UUID.fromString("44444444-0000-0000-0000-000000000005");
    public static final UUID PO6_ID  = UUID.fromString("44444444-0000-0000-0000-000000000006");
    public static final UUID PO7_ID  = UUID.fromString("44444444-0000-0000-0000-000000000007");
    public static final UUID PO8_ID  = UUID.fromString("44444444-0000-0000-0000-000000000008");
    public static final UUID PO9_ID  = UUID.fromString("44444444-0000-0000-0000-000000000009");
    public static final UUID PO10_ID = UUID.fromString("44444444-0000-0000-0000-000000000010");
    public static final UUID PO11_ID = UUID.fromString("44444444-0000-0000-0000-000000000011");
    public static final UUID PO12_ID = UUID.fromString("44444444-0000-0000-0000-000000000012");

    // ── PSOs ──────────────────────────────────────────────────────────────────
    public static final UUID PSO1_ID = UUID.fromString("55555555-0000-0000-0000-000000000001");
    public static final UUID PSO2_ID = UUID.fromString("55555555-0000-0000-0000-000000000002");
    public static final UUID PSO3_ID = UUID.fromString("55555555-0000-0000-0000-000000000003");
}
