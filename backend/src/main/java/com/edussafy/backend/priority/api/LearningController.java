package com.edussafy.backend.priority.api;

import com.edussafy.backend.priority.dto.PriorityDtos.CurriculumResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialDetailResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialResourcesResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialViewResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialsResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.ReplayResponse;
import com.edussafy.backend.priority.service.PriorityApiService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final PriorityApiService priorityApiService;

    public LearningController(PriorityApiService priorityApiService) {
        this.priorityApiService = priorityApiService;
    }

    @GetMapping("/curriculum")
    public CurriculumResponse curriculum() {
        return priorityApiService.curriculum();
    }

    @GetMapping("/replays")
    public ReplayResponse replays() {
        return priorityApiService.replays();
    }

    @GetMapping("/materials")
    public MaterialsResponse materials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return priorityApiService.materials(keyword, type, page, size);
    }

    @GetMapping("/materials/{id}")
    public MaterialDetailResponse material(@PathVariable Long id) {
        return priorityApiService.material(id);
    }

    @PostMapping("/materials/{id}/views")
    public MaterialViewResponse recordMaterialView(@PathVariable Long id) {
        return priorityApiService.recordMaterialView(id);
    }

    @GetMapping("/materials/{id}/resources")
    public MaterialResourcesResponse materialResources(@PathVariable Long id) {
        return priorityApiService.materialResources(id);
    }
}
