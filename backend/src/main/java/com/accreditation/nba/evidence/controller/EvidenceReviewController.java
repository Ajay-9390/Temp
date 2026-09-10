package com.accreditation.nba.evidence.controller;

import com.accreditation.nba.evidence.dto.request.ReviewActionRequest;
import com.accreditation.nba.evidence.dto.response.ReviewResponse;
import com.accreditation.nba.evidence.enums.ReviewDecision;
import com.accreditation.nba.evidence.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evidence Review", description = "Reviewer decisions and review history")
@RestController
@RequestMapping("/api/v1/evidences/{id}")
public class EvidenceReviewController {

    private final ReviewService reviewService;

    public EvidenceReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Approve evidence (UNDER_REVIEW -> APPROVED)")
    @PostMapping("/approve")
    public ReviewResponse approve(@PathVariable UUID id,
                                  @Valid @RequestBody(required = false) ReviewActionRequest request) {
        return reviewService.review(id, ReviewDecision.APPROVE, reviewer(request), comments(request));
    }

    @Operation(summary = "Reject evidence (UNDER_REVIEW -> REJECTED). Comments required.")
    @PostMapping("/reject")
    public ReviewResponse reject(@PathVariable UUID id,
                                 @Valid @RequestBody(required = false) ReviewActionRequest request) {
        return reviewService.review(id, ReviewDecision.REJECT, reviewer(request), comments(request));
    }

    @Operation(summary = "Request changes (UNDER_REVIEW -> CHANGES_REQUIRED). Comments required.")
    @PostMapping("/request-changes")
    public ReviewResponse requestChanges(@PathVariable UUID id,
                                         @Valid @RequestBody(required = false) ReviewActionRequest request) {
        return reviewService.review(id, ReviewDecision.REQUEST_CHANGES, reviewer(request), comments(request));
    }

    @Operation(summary = "Review history (newest first)")
    @GetMapping("/reviews")
    public List<ReviewResponse> reviews(@PathVariable UUID id) {
        return reviewService.getReviews(id);
    }

    private String reviewer(ReviewActionRequest request) {
        return request == null ? null : request.reviewer();
    }

    private String comments(ReviewActionRequest request) {
        return request == null ? null : request.comments();
    }
}
