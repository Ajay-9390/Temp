package com.example.nba;

import com.example.nba.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end scenario (§36): Institution → Department → Program → NBA 2026 Tier I cycle →
 * Academic Year 2026-27 → Semester 1 → Semester 2 → verify the complete program overview.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProgramManagementE2EIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    static String institutionId;
    static String departmentId;
    static String programId;
    static String cycleId;
    static String academicYearId;

    private JsonNode dataOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    @Test
    @Order(1)
    void createInstitution() throws Exception {
        var body = """
                {"name":"ABC Institute of Technology","code":"ABC-INST","type":"AUTONOMOUS"}""";
        MvcResult r = mockMvc.perform(post("/api/v1/institutions")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        institutionId = dataOf(r).get("id").asText();
    }

    @Test
    @Order(2)
    void createDepartment() throws Exception {
        var body = """
                {"institutionId":"%s","name":"Computer Science & Engineering","code":"CSE"}"""
                .formatted(institutionId);
        MvcResult r = mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        departmentId = dataOf(r).get("id").asText();
    }

    @Test
    @Order(3)
    void createProgram() throws Exception {
        var body = """
                {"departmentId":"%s","name":"B.Tech CSE","code":"BTECH-CSE","degree":"B.Tech",
                 "branch":"Computer Science and Engineering","durationYears":4,
                 "totalSemesters":8,"intake":120,"establishedYear":2005}"""
                .formatted(departmentId);
        MvcResult r = mockMvc.perform(post("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        programId = dataOf(r).get("id").asText();
    }

    @Test
    @Order(4)
    void createTierIAccreditationCycle() throws Exception {
        var body = """
                {"name":"NBA 2026","tier":"TIER_I","frameworkVersion":"GAPC v4.0",
                 "applicationYear":2026,"startDate":"2026-07-01","endDate":"2029-06-30"}""";
        MvcResult r = mockMvc.perform(post("/api/v1/programs/{id}/accreditation-cycles", programId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tier").value("TIER_I"))
                .andExpect(jsonPath("$.data.frameworkVersion").value("GAPC v4.0"))
                .andReturn();
        cycleId = dataOf(r).get("id").asText();
    }

    @Test
    @Order(5)
    void moveCycleToPreparation() throws Exception {
        mockMvc.perform(patch("/api/v1/accreditation-cycles/{id}/status", cycleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PREPARATION"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARATION"));
    }

    @Test
    @Order(6)
    void createAcademicYearAndActivate() throws Exception {
        var body = """
                {"name":"2026-27","startDate":"2026-07-01","endDate":"2027-06-30"}""";
        MvcResult r = mockMvc.perform(post("/api/v1/programs/{id}/academic-years", programId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        academicYearId = dataOf(r).get("id").asText();

        mockMvc.perform(patch("/api/v1/academic-years/{id}/status", academicYearId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(7)
    void createSemesters() throws Exception {
        mockMvc.perform(post("/api/v1/academic-years/{id}/semesters", academicYearId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"semesterNumber":1,"name":"Semester 1",
                                 "startDate":"2026-07-01","endDate":"2026-12-31"}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/academic-years/{id}/semesters", academicYearId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"semesterNumber":2,"name":"Semester 2",
                                 "startDate":"2027-01-01","endDate":"2027-06-30"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(8)
    void duplicateSemesterNumberRejected() throws Exception {
        mockMvc.perform(post("/api/v1/academic-years/{id}/semesters", academicYearId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"semesterNumber":1,"name":"Duplicate",
                                 "startDate":"2026-07-01","endDate":"2026-12-31"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SEMESTER_NUMBER_ALREADY_EXISTS"));
    }

    @Test
    @Order(9)
    void verifyProgramOverview() throws Exception {
        MvcResult r = mockMvc.perform(get("/api/v1/programs/{id}/overview", programId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.programName").value("B.Tech CSE"))
                .andExpect(jsonPath("$.data.departmentName").value("Computer Science & Engineering"))
                .andExpect(jsonPath("$.data.institutionName").value("ABC Institute of Technology"))
                .andExpect(jsonPath("$.data.tier").value("TIER_I"))
                .andExpect(jsonPath("$.data.currentAccreditation.status").value("PREPARATION"))
                .andExpect(jsonPath("$.data.currentAcademicYear.name").value("2026-27"))
                .andExpect(jsonPath("$.data.integrationPoints.sarCompletion").exists())
                .andReturn();

        JsonNode data = dataOf(r);
        assertThat(data.get("totalSemesters").asInt()).isEqualTo(8);
        assertThat(data.get("academicYearCount").asInt()).isEqualTo(1);
        assertThat(data.get("accreditationCycleCount").asInt()).isEqualTo(1);
    }

    @Test
    @Order(10)
    void invalidTierRejected() throws Exception {
        var body = """
                {"name":"Bad","tier":"TIER_X","frameworkVersion":"GAPC v4.0","applicationYear":2027}""";
        mockMvc.perform(post("/api/v1/programs/{id}/accreditation-cycles", programId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }
}
