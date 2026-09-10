package com.example.nba.department.mapper;

import com.example.nba.department.dto.DepartmentRequest;
import com.example.nba.department.dto.DepartmentResponse;
import com.example.nba.department.entity.Department;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    Department toEntity(DepartmentRequest request);

    @Mapping(target = "programCount", source = "programCount")
    DepartmentResponse toResponse(Department entity, long programCount);

    /** institutionId is immutable; ignore it on update along with nulls. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "institutionId", ignore = true)
    void updateEntity(DepartmentRequest request, @MappingTarget Department entity);
}
