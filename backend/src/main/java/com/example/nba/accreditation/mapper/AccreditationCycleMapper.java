package com.example.nba.accreditation.mapper;

import com.example.nba.accreditation.dto.AccreditationCycleRequest;
import com.example.nba.accreditation.dto.AccreditationCycleResponse;
import com.example.nba.accreditation.entity.AccreditationCycle;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccreditationCycleMapper {

    AccreditationCycle toEntity(AccreditationCycleRequest request);

    AccreditationCycleResponse toResponse(AccreditationCycle entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AccreditationCycleRequest request, @MappingTarget AccreditationCycle entity);
}
