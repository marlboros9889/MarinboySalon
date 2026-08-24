package com.marinboy.controller;

import com.marinboy.service.ServiceItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** ADMIN 시술 메뉴와 이미지 multipart API를 메뉴 책임으로 분리합니다. */
@RestController
public class AdminServiceController {
    private final ServiceItemService serviceItemService;

    public AdminServiceController(ServiceItemService serviceItemService) {
        this.serviceItemService = serviceItemService;
    }

    @GetMapping("/api/admin/services")
    Object services() {
        return serviceItemService.getServices();
    }

    @PostMapping(value = "/api/admin/services", consumes = "multipart/form-data")
    ResponseEntity<Void> createService(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam int durationMinutes,
            @RequestParam int price,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile[] galleryImages) {
        serviceItemService.saveService(
                null, name, category, durationMinutes, price, description, image, galleryImages);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/api/admin/services/{id}", consumes = "multipart/form-data")
    ResponseEntity<Void> updateService(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam int durationMinutes,
            @RequestParam int price,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile[] galleryImages) {
        serviceItemService.saveService(
                id, name, category, durationMinutes, price, description, image, galleryImages);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/services/{id}")
    ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceItemService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
