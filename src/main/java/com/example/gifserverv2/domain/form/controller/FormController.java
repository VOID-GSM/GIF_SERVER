package com.example.gifserverv2.domain.form.controller;

import com.example.gifserverv2.domain.form.dto.request.*;
import com.example.gifserverv2.domain.form.dto.response.*;
import com.example.gifserverv2.domain.form.service.AdminFormService;
import com.example.gifserverv2.domain.form.service.ClientFormService;
import com.example.gifserverv2.domain.form.service.FormFileService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.ai.service.AiSummaryService;
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

    private final AdminFormService adminFormService;
    private final ClientFormService clientFormService;
    private final FormFileService formFileService;
    private final AiSummaryService aiSummaryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CreateFormRequest request
    ) {
        return ResponseEntity.ok(adminFormService.createForm(user.userId(), request));
    }

    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long formId,
            @RequestBody UpdateFormRequest request
    ) {
        adminFormService.updateForm(user.userId(), formId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/announce")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> announceForm(@RequestParam Long formId) {
        adminFormService.announceForm(formId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{formId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable("formId") Long formId
    ) {
        adminFormService.deleteForm(user.userId(), formId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListFormResponse>> getAllFormsForAdmin(
            @RequestParam(required = false) String grade
    ) {
        Integer numericGrade = null;
        if (grade != null && !grade.isBlank() && !grade.equals("null")
                && !grade.equals("undefined")) {
            try {
                numericGrade = Integer.parseInt(grade);
            } catch (NumberFormatException e) {
                numericGrade = null;
            }
        }
        return ResponseEntity.ok(adminFormService.getAllFormsForAdmin(numericGrade));
    }

    @GetMapping("/admin/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubmitDetailFormResponse>> getSubmitList(@RequestParam Long formId) {
        return ResponseEntity.ok(adminFormService.getSubmitListByForm(formId));
    }

    @GetMapping
    public ResponseEntity<List<ListFormResponse>> getAnnouncedForms(
            @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getAnnouncedForms(projectId));
    }

    @GetMapping("/{formId}")
    public ResponseEntity<DetailFormResponse> getForm(
            @PathVariable Long formId,
            @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getForm(formId, projectId));
    }

    @PostMapping("/submit")
    public ResponseEntity<Long> submitForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody SubmitFormRequest request
    ) {
        return ResponseEntity.ok(clientFormService.submitForm(user.userId(), request));
    }

    @GetMapping("/my-submit")
    public ResponseEntity<SubmitDetailFormResponse> getMySubmit(
            @RequestParam Long formId,
            @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getMySubmit(formId, projectId));
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long submitId,
            @RequestParam Long fieldId,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(formFileService.uploadFile(user.userId(), submitId, fieldId, file));
    }

    @DeleteMapping("/upload")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long submitId,
            @RequestParam Long fieldId
    ) {
        formFileService.deleteFile(user.userId(), submitId, fieldId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/submit")
    public ResponseEntity<Void> updateSubmit(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody UpdateSubmitRequest request
    ) {
        clientFormService.updateSubmit(user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/draft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListFormResponse>> getDraftForms() {
        return ResponseEntity.ok(adminFormService.getDraftForms());
    }

    @GetMapping("/admin/draft/{formId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DetailFormResponse> getDraftForm(@PathVariable Long formId) {
        return ResponseEntity.ok(adminFormService.getDraftForm(formId));
    }

    @GetMapping("/submit/{submitId}/summary")
    public ResponseEntity<String> summarizeFormSubmit(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long submitId
    ) {
        return ResponseEntity.ok(aiSummaryService.summarizeFormSubmit(submitId));
    }
}

