package com.example.gifserverv2.domain.form.controller;

import com.example.gifserverv2.domain.form.dto.request.FormCreateRequest;
import com.example.gifserverv2.domain.form.dto.request.FormSubmitRequest;
import com.example.gifserverv2.domain.form.dto.request.FormUpdateRequest;
import com.example.gifserverv2.domain.form.dto.response.FormDetailResponse;
import com.example.gifserverv2.domain.form.dto.response.FormListResponse;
import com.example.gifserverv2.domain.form.dto.response.FormSubmitDetailResponse;
import com.example.gifserverv2.domain.form.service.FormService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/form")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createForm(
            @RequestBody FormCreateRequest request
    ) {
        return ResponseEntity.ok(formService.createForm(request));
    }

    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateForm(
            @RequestParam Long formId,
            @RequestBody FormUpdateRequest request
    ) {
        formService.updateForm(formId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/announce")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> announceForm(@RequestParam Long formId) {
        formService.announceForm(formId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForm(@RequestParam Long formId) {
        formService.deleteForm(formId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FormListResponse>> getAllFormsForAdmin() {
        return ResponseEntity.ok(formService.getAllFormsForAdmin());
    }

    @GetMapping("/admin/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FormSubmitDetailResponse>> getSubmitList(@RequestParam Long formId) {
        return ResponseEntity.ok(formService.getSubmitListByForm(formId));
    }

    @GetMapping
    public ResponseEntity<List<FormListResponse>> getAnnouncedForms(
            @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(formService.getAnnouncedForms(projectId));
    }

    @GetMapping("/{formId}")
    public ResponseEntity<FormDetailResponse> getForm(@PathVariable Long formId) {
        return ResponseEntity.ok(formService.getForm(formId));
    }

    @PostMapping("/submit")
    public ResponseEntity<Long> submitForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody FormSubmitRequest request
    ) {
        return ResponseEntity.ok(formService.submitForm(user.userId(), request));
    }

    @GetMapping("/my-submit")
    public ResponseEntity<FormSubmitDetailResponse> getMySubmit(
            @RequestParam Long formId,
            @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(formService.getMySubmit(formId, projectId));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam Long submitId,
            @RequestParam Long fieldId,
            @RequestParam MultipartFile file
    ) {

        return ResponseEntity.ok("파일 업로드 추후 구현");
    }
}
