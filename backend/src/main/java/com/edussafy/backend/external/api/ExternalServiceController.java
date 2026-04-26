package com.edussafy.backend.external.api;

import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServiceAccessResponse;
import com.edussafy.backend.external.dto.ExternalServiceDtos.ExternalServicesResponse;
import com.edussafy.backend.external.service.ExternalServiceService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external-services")
public class ExternalServiceController {

    private final ExternalServiceService service;

    public ExternalServiceController(ExternalServiceService service) {
        this.service = service;
    }

    @GetMapping
    public ExternalServicesResponse services() {
        return service.services();
    }

    @PostMapping("/{code}/access-log")
    public ExternalServiceAccessResponse logAccess(@PathVariable @NotBlank String code) {
        return service.logAccess(code);
    }
}
