package com.example.nba.academic.year.mapper;

import com.example.nba.academic.year.dto.AcademicYearRequest;
import com.example.nba.academic.year.dto.AcademicYearResponse;
import com.example.nba.academic.year.entity.AcademicYear;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AcademicYearMapper {

    AcademicYear toEntity(AcademicYearRequest request);

    @Mapping(target = "semesterCount", source = "semesterCount")
    AcademicYearResponse toResponse(AcademicYear entity, long semesterCount);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AcademicYearRequest request, @MappingTarget AcademicYear entity);
}
