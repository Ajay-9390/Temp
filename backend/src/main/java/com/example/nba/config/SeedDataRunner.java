package com.example.nba.config;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.semester.entity.Semester;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.accreditation.entity.AccreditationCycle;
import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import com.example.nba.accreditation.repository.AccreditationCycleRepository;
import com.example.nba.department.entity.Department;
import com.example.nba.department.entity.DepartmentStatus;
import com.example.nba.department.repository.DepartmentRepository;
import com.example.nba.institution.entity.Institution;
import com.example.nba.institution.entity.InstitutionStatus;
import com.example.nba.institution.repository.InstitutionRepository;
import com.example.nba.program.entity.Program;
import com.example.nba.program.entity.ProgramStatus;
import com.example.nba.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Loads clearly-fictional development seed data on startup when {@code app.seed.enabled=true}.
 * Idempotent: skips if any institution already exists. Never runs in production by default.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class SeedDataRunner implements ApplicationRunner {

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final AccreditationCycleRepository cycleRepository;
    private final AcademicYearRepository yearRepository;
    private final SemesterRepository semesterRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (institutionRepository.count() > 0) {
            log.info("Seed data already present; skipping.");
            return;
        }
        log.info("Loading development seed data...");

        Institution abc = institution("ABC Institute of Technology", "ABC-INST");

        Department cse = department(abc, "Computer Science & Engineering", "CSE");
        Department ece = department(abc, "Electronics & Communication Engineering", "ECE");
        Department mech = department(abc, "Mechanical Engineering", "MECH");

        Program cseBtech = program(cse, "B.Tech Computer Science and Engineering", "BTECH-CSE",
                "Computer Science and Engineering", ProgramStatus.ACTIVE);
        program(ece, "B.Tech Electronics and Communication Engineering", "BTECH-ECE",
                "Electronics and Communication Engineering", ProgramStatus.ACTIVE);
        program(mech, "B.Tech Mechanical Engineering", "BTECH-MECH",
                "Mechanical Engineering", ProgramStatus.DRAFT);

        // Accreditation history for CSE (EXPIRED → ACCREDITED → in PREPARATION)
        cycle(cseBtech, "NBA Cycle 2016", "TIER_II", 2016, AccreditationCycleStatus.EXPIRED,
                LocalDate.of(2016, 7, 1), LocalDate.of(2019, 6, 30));
        cycle(cseBtech, "NBA Cycle 2021", "TIER_I", 2021, AccreditationCycleStatus.ACCREDITED,
                LocalDate.of(2021, 7, 1), LocalDate.of(2024, 6, 30));
        cycle(cseBtech, "NBA 2026", "TIER_I", 2026, AccreditationCycleStatus.PREPARATION,
                LocalDate.of(2026, 7, 1), LocalDate.of(2029, 6, 30));

        // Academic years for CSE
        academicYear(cseBtech, "2024-25", LocalDate.of(2024, 7, 1), LocalDate.of(2025, 6, 30),
                AcademicLifecycleStatus.COMPLETED);
        academicYear(cseBtech, "2025-26", LocalDate.of(2025, 7, 1), LocalDate.of(2026, 6, 30),
                AcademicLifecycleStatus.COMPLETED);
        AcademicYear y2627 = academicYear(cseBtech, "2026-27",
                LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30), AcademicLifecycleStatus.ACTIVE);

        semester(y2627, 1, "Semester 1", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31),
                AcademicLifecycleStatus.ACTIVE);
        semester(y2627, 2, "Semester 2", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 6, 30),
                AcademicLifecycleStatus.PLANNED);

        log.info("Seed data loaded.");
    }

    private Institution institution(String name, String code) {
        Institution i = new Institution();
        i.setName(name);
        i.setCode(code);
        i.setType("AUTONOMOUS");
        i.setCity("Bengaluru");
        i.setState("Karnataka");
        i.setCountry("India");
        i.setContactEmail("info@abc-inst.example");
        i.setStatus(InstitutionStatus.ACTIVE);
        return institutionRepository.save(i);
    }

    private Department department(Institution inst, String name, String code) {
        Department d = new Department();
        d.setInstitutionId(inst.getId());
        d.setName(name);
        d.setCode(code);
        d.setStatus(DepartmentStatus.ACTIVE);
        return departmentRepository.save(d);
    }

    private Program program(Department dept, String name, String code, String branch, ProgramStatus status) {
        Program p = new Program();
        p.setDepartmentId(dept.getId());
        p.setName(name);
        p.setCode(code);
        p.setDegree("B.Tech");
        p.setBranch(branch);
        p.setDurationYears(4);
        p.setTotalSemesters(8);
        p.setIntake(120);
        p.setEstablishedYear(2005);
        p.setStatus(status);
        return programRepository.save(p);
    }

    private void cycle(Program program, String name, String tier, int year,
                       AccreditationCycleStatus status, LocalDate start, LocalDate end) {
        AccreditationCycle c = new AccreditationCycle();
        c.setProgramId(program.getId());
        c.setName(name);
        c.setTier(tier);
        c.setFrameworkVersion("GAPC v4.0");
        c.setApplicationYear(year);
        c.setStartDate(start);
        c.setEndDate(end);
        c.setStatus(status);
        cycleRepository.save(c);
    }

    private AcademicYear academicYear(Program program, String name, LocalDate start, LocalDate end,
                                      AcademicLifecycleStatus status) {
        AcademicYear y = new AcademicYear();
        y.setProgramId(program.getId());
        y.setName(name);
        y.setStartDate(start);
        y.setEndDate(end);
        y.setStatus(status);
        return yearRepository.save(y);
    }

    private void semester(AcademicYear year, int number, String name, LocalDate start, LocalDate end,
                          AcademicLifecycleStatus status) {
        Semester s = new Semester();
        s.setAcademicYearId(year.getId());
        s.setSemesterNumber(number);
        s.setName(name);
        s.setStartDate(start);
        s.setEndDate(end);
        s.setStatus(status);
        semesterRepository.save(s);
    }
}
