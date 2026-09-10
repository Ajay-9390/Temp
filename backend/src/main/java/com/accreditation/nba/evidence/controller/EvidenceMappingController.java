package com.accreditation.nba.evidence.controller;

import com.accreditation.nba.evidence.dto.request.CreateMappingRequest;
import com.accreditation.nba.evidence.dto.response.MappingResponse;
import com.accreditation.nba.evidence.service.MappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evidence Mappings", description = "Map one evidence item to many NBA criteria/requirements")
@RestController
@RequestMapping("/api/v1/evidences/{id}/mappings")
public class EvidenceMappingController {

    private final MappingService mappingService;

    public EvidenceMappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Operation(summary = "List mappings for an evidence item")
    @GetMapping
    public List<MappingResponse> list(@PathVariable UUID id) {
        return mappingService.listMappings(id);
    }

    @Operation(summary = "Add a mapping (duplicate criterion/requirement rejected)")
    @PostMapping
    public ResponseEntity<MappingResponse> add(@PathVariable UUID id,
                                               @Valid @RequestBody CreateMappingRequest request) {
        MappingResponse response = mappingService.addMapping(id, request);
        return ResponseEntity
                .created(URI.create("/api/v1/evidences/" + id + "/mappings/" + response.id()))
                .body(response);
    }

    @Operation(summary = "Remove a mapping")
    @DeleteMapping("/{mappingId}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @PathVariable UUID mappingId) {
        mappingService.deleteMapping(id, mappingId);
        return ResponseEntity.noContent().build();
    }
}
