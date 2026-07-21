package com.philomath.controller;

import com.philomath.dto.TenFieldDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sample")
public class SampleController {

    @PostMapping
    public ResponseEntity<TenFieldDTO> createSample(@RequestBody TenFieldDTO dto) {
        // Echo back the received DTO for easy testing
        return ResponseEntity.ok(dto);
    }
}
