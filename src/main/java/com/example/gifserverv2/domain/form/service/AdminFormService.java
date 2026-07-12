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
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFormService {

    private final FormRepository formRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final QueryFormService queryFormService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createForm(CreateFormRequest request) {
        Form form = Form.builder()
                .title(request.title())
                .deadline(request.deadline())
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
                            .build())
                    .toList();
            form.getFields().addAll(formFields);
        }

        return formRepository.save(form).getId();
    }

    @Transactional
    public void updateForm(Long formId, UpdateFormRequest request) {
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
                            .build())
                    .toList();
        }

        form.update(request.title(), request.description(), request.deadline(), request.targetGrade(), newFields);
    }

    public List<ListFormResponse> getAllFormsForAdmin(Integer grade) {
        List<Form> forms = (grade == null)
                ? formRepository.findAll()
                : formRepository.findAllByTargetGrade(grade);

        return forms.stream()
                .map(ListFormResponse::from)
                .toList();
    }

    @Transactional
    public void announceForm(Long formId) {
        Form form = queryFormService.getFormOrThrow(formId);
        form.announce();
    }

    @Transactional
    public void deleteForm(Long formId) {
        Form form = queryFormService.getFormOrThrow(formId);
        formRepository.delete(form);
    }

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

    public List<ListFormResponse> getDraftForms() {
        return formRepository.findAllByAnnouncedFalseOrderByCreatedAtDesc().stream()
                .map(ListFormResponse::from)
                .toList();
    }

    public DetailFormResponse getDraftForm(Long formId) {
        Form form = formRepository.findByIdAndAnnouncedFalse(formId)
                .orElseThrow(FormException::notFound);
        return DetailFormResponse.from(form, null);
    }
}