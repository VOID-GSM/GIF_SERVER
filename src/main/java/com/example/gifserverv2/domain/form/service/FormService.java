package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.dto.request.*;
import com.example.gifserverv2.domain.form.dto.response.*;
import com.example.gifserverv2.domain.form.entity.*;
import com.example.gifserverv2.domain.form.repository.*;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final FormFieldAnswerRepository formFieldAnswerRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public Long createForm(FormCreateRequest request) {
        Form form = Form.builder()
                .title(request.title())
                .deadline(request.deadline())
                .announced(false)
                .build();
        formRepository.save(form);

        saveFields(form, request.fields().stream()
                .map(f -> new FormUpdateRequest.FieldRequest(
                        f.title(), f.description(), f.type(), f.orderIndex()))
                .toList());

        return form.getId();
    }

    @Transactional
    public void updateForm(Long formId, FormUpdateRequest request) {
        Form form = getFormOrThrow(formId);

        if (form.isAnnounced()) {
            throw FormException.alreadyAnnounced();
        }

        form.update(request.title(), request.deadline());

        boolean hasAnswers = formSubmitRepository.existsByFormId(formId);
        if (hasAnswers) {
            throw FormException.hasSubmittedAnswers();
        }

        formFieldRepository.deleteAllByFormId(formId);
        saveFields(form, request.fields());
    }

    @Transactional
    public void announceForm(Long formId) {
        Form form = getFormOrThrow(formId);
        validateFormForAnnounce(form);
        form.announce();
    }

    @Transactional
    public void deleteForm(Long formId) {
        Form form = getFormOrThrow(formId);
        formRepository.delete(form);
    }

    public List<FormListResponse> getAllFormsForAdmin() {
        return formRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FormListResponse::from)
                .toList();
    }

    public List<FormSubmitDetailResponse> getSubmitListByForm(Long formId) {
        getFormOrThrow(formId);
        return formSubmitRepository.findAllByFormId(formId).stream()
                .map(FormSubmitDetailResponse::from)
                .toList();
    }

    public List<FormListResponse> getAnnouncedForms(Long projectId) {
        return formRepository.findAllByAnnouncedTrueOrderByDeadlineAsc().stream()
                .map(form -> {
                    boolean submitted = formSubmitRepository
                            .existsByFormIdAndProjectId(form.getId(), projectId);
                    return FormListResponse.from(form, submitted);
                })
                .toList();
    }

    public FormDetailResponse getForm(Long formId) {
        Form form = getFormOrThrow(formId);
        if (!form.isAnnounced()) throw FormException.notAnnounced();
        return FormDetailResponse.from(form);
    }

    @Transactional
    public Long submitForm(Long userId, FormSubmitRequest request) {
        Form form = getFormOrThrow(request.formId());

        if (!form.isAnnounced()) throw FormException.notAnnounced();

        if (!projectMemberRepository.existsByProjectIdAndUserId(request.projectId(), userId)) {
            throw ProjectException.notMember();
        }

        if (formSubmitRepository.existsByFormIdAndProjectId(form.getId(), request.projectId())) {
            throw FormException.alreadySubmitted();
        }

        FormSubmit submit = FormSubmit.builder()
                .form(form)
                .projectId(request.projectId())
                .submittedByUserId(userId)
                .build();
        formSubmitRepository.save(submit);

        request.answers().forEach(answerReq -> {
            FormField field = formFieldRepository.findById(answerReq.fieldId())
                    .orElseThrow(FormException::fieldNotFound);

            formFieldAnswerRepository.save(FormFieldAnswer.builder()
                    .formSubmit(submit)
                    .formField(field)
                    .textAnswer(answerReq.textAnswer())
                    .dateAnswer(answerReq.dateAnswer())
                    .build());
        });

        return submit.getId();
    }

    public FormSubmitDetailResponse getMySubmit(Long formId, Long projectId) {
        FormSubmit submit = formSubmitRepository.findByFormIdAndProjectId(formId, projectId)
                .orElseThrow(FormException::notSubmitted);
        return FormSubmitDetailResponse.from(submit);
    }

    private Form getFormOrThrow(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(FormException::notFound);
    }

    private void saveFields(Form form, List<FormUpdateRequest.FieldRequest> fields) {
        fields.forEach(f -> formFieldRepository.save(FormField.builder()
                .form(form)
                .title(f.title())
                .description(f.description())
                .type(f.type())
                .orderIndex(f.orderIndex())
                .build()));
    }

    private void validateFormForAnnounce(Form form) {
        if (form.getTitle() == null || form.getTitle().isBlank())
            throw FormException.incompleteForm();
        if (form.getDeadline() == null)
            throw FormException.incompleteForm();
        form.getFields().forEach(field -> {
            if (field.getTitle() == null || field.getTitle().isBlank())
                throw FormException.incompleteForm();
            if (field.getDescription() == null || field.getDescription().isBlank())
                throw FormException.incompleteForm();
        });
    }
}