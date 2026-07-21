package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.request.CreateFormRequest;
import com.example.gifserverv2.domain.form.dto.request.UpdateFormRequest;
import com.example.gifserverv2.domain.form.dto.response.DetailFormResponse;
import com.example.gifserverv2.domain.form.dto.response.ListFormResponse;
import com.example.gifserverv2.domain.form.dto.response.SubmitDetailFormResponse;
import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.AllowedFileExtensions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFormService {

    private final FormRepository formRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final QueryFormService queryFormService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PushSenderService pushSenderService;

    @Transactional
    public Long createForm(Long userId, CreateFormRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        AdminRole adminRole = user.getAdminRole();
        if (adminRole != AdminRole.MASTER && adminRole != AdminRole.VOID) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "양식 생성 권한이 없습니다.");
        }

        Form form = Form.builder()
                .title(request.title())
                .deadline(request.deadline())
                .createdByUserId(userId)
                .fields(new ArrayList<>())
                .build();

        if (request.fields() != null) {
            List<FormField> formFields = request.fields().stream()
                    .map(fieldReq -> FormField.builder()
                            .form(form)
                            .title(fieldReq.title())
                            .description(fieldReq.description())
                            .type(fieldReq.type())
                            .orderIndex(fieldReq.orderIndex())
                            .allowedExtensions(normalizeExtensions(fieldReq.type(), fieldReq.allowedExtensions()))
                            .build())
                    .toList();
            form.getFields().addAll(formFields);
        }

        return formRepository.save(form).getId();
    }

    @Transactional
    public void updateForm(Long userId, Long formId, UpdateFormRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        AdminRole adminRole = user.getAdminRole();
        if (adminRole != AdminRole.MASTER && adminRole != AdminRole.VOID) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "양식 수정 권한이 없습니다.");
        }
        Form form = queryFormService.getFormOrThrow(formId);

        List<FormField> newFields = new ArrayList<>();
        if (request.fields() != null) {
            newFields = request.fields().stream()
                    .map(fieldReq -> FormField.builder()
                            .form(form)
                            .title(fieldReq.title())
                            .description(fieldReq.description())
                            .type(fieldReq.type())
                            .orderIndex(fieldReq.orderIndex())
                            .allowedExtensions(normalizeExtensions(fieldReq.type(), fieldReq.allowedExtensions()))
                            .build())
                    .toList();
        }

        form.update(request.title(), request.description(), request.deadline(), request.targetGrade(), newFields);
    }

    private String normalizeExtensions(FormField.FieldType type, List<String> extensions) {
        if (type != FormField.FieldType.FILE || extensions == null || extensions.isEmpty()) {
            return null;
        }

        List<String> normalized = extensions.stream()
                .filter(ext -> ext != null && !ext.isBlank())
                .map(ext -> {
                    String trimmed = ext.trim().toLowerCase();
                    return trimmed.startsWith(".") ? trimmed.substring(1) : trimmed;
                })
                .distinct()
                .toList();

        for (String ext : normalized) {
            if (!AllowedFileExtensions.ALL.contains(ext)) {
                throw FormException.invalidAllowedExtension();
            }
        }

        return String.join(",", normalized);
    }

    @Transactional
    public void announceForm(Long formId) {
        Form form = queryFormService.getFormOrThrow(formId);
        form.announce();

        String title = PushMessageTemplate.FORM_ANNOUNCED.getTitle();
        String body = PushMessageTemplate.FORM_ANNOUNCED.getBody();

        List<Long> targetUserIds = (form.getTargetGrade() == null)
                ? userRepository.findAllStudentIds()
                : userRepository.findStudentIdsByGrade(form.getTargetGrade());

        pushSenderService.sendBulkNotifications(targetUserIds, title, body);
    }

    @Transactional
    public void deleteForm(Long userId, Long formId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        AdminRole adminRole = user.getAdminRole();
        if (adminRole != AdminRole.MASTER && adminRole != AdminRole.VOID) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "양식 삭제 권한이 없습니다.");
        }

        Form form = queryFormService.getFormOrThrow(formId);
        formRepository.delete(form);
    }

    @Transactional(readOnly = true)
    public List<ListFormResponse> getAllFormsForAdmin(Integer grade) {
        List<Form> forms = (grade == null)
                ? formRepository.findAll()
                : formRepository.findAllByTargetGrade(grade);

        return forms.stream()
                .map(ListFormResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmitDetailFormResponse> getSubmitListByForm(Long formId) {
        Form form = queryFormService.getFormOrThrow(formId);
        List<FormSubmit> submits = formSubmitRepository.findAllByFormId(form.getId());

        Set<Long> projectIds = submits.stream()
                .map(FormSubmit::getProjectId)
                .collect(Collectors.toSet());
        Set<Long> userIds = submits.stream()
                .map(FormSubmit::getSubmittedByUserId)
                .collect(Collectors.toSet());

        Map<Long, String> teamNameMap = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getTeamName));

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        return submits.stream()
                .map(submit -> {
                    String teamName = teamNameMap.get(submit.getProjectId());
                    UserEntity user = userMap.get(submit.getSubmittedByUserId());
                    String submittedByName = user != null ? user.getName() : null;
                    String submittedByStudentNumber = user != null ? user.getStudentNumber() : null;
                    return SubmitDetailFormResponse.from(submit, teamName, submittedByName, submittedByStudentNumber);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ListFormResponse> getDraftForms() {
        return formRepository.findAllByAnnouncedFalseOrderByCreatedAtDesc().stream()
                .map(ListFormResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DetailFormResponse getDraftForm(Long formId) {
        Form form = formRepository.findByIdAndAnnouncedFalse(formId)
                .orElseThrow(FormException::notFound);
        return DetailFormResponse.from(form, null);
    }
}