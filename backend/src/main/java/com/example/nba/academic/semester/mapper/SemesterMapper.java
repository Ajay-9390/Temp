package com.example.nba.academic.semester.mapper;

import com.example.nba.academic.semester.dto.SemesterRequest;
import com.example.nba.academic.semester.dto.SemesterResponse;
import com.example.nba.academic.semester.entity.Semester;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SemesterMapper {

    Semester toEntity(SemesterRequest request);

    SemesterResponse toResponse(Semester entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SemesterRequest request, @MappingTarget Semester entity);
}
