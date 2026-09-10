package com.example.nba.program.mapper;

import com.example.nba.program.dto.ProgramRequest;
import com.example.nba.program.dto.ProgramResponse;
import com.example.nba.program.entity.Program;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProgramMapper {

    Program toEntity(ProgramRequest request);

    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "currentAccreditation", ignore = true)
    @Mapping(target = "currentAcademicYear", ignore = true)
    ProgramResponse toResponse(Program entity);

    /** departmentId is immutable; ignore it on update along with nulls. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "departmentId", ignore = true)
    void updateEntity(ProgramRequest request, @MappingTarget Program entity);
}
