package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.request.SubmitFormRequest;
import com.example.gifserverv2.domain.form.dto.response.DetailFormResponse;
import com.example.gifserverv2.domain.form.dto.response.ListFormResponse;
import com.example.gifserverv2.domain.form.dto.response.SubmitDetailFormResponse;
import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormFieldAnswerRepository;
import com.example.gifserverv2.domain.form.repository.FormFieldRepository;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientFormService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final FormFieldAnswerRepository formFieldAnswerRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryFormService queryFormService;

    public List<ListFormResponse> getAnnouncedForms(Long projectId) {
        return formRepository.findAllByAnnouncedTrueOrderByDeadlineAsc().stream()
                .map(form -> {
                    boolean submitted = formSubmitRepository
                            .existsByFormIdAndProjectId(form.getId(), projectId);
                    return ListFormResponse.from(form, submitted);
                })
                .toList();
    }

    public DetailFormResponse getForm(Long formId) {
        Form form = queryFormService.getFormOrThrow(formId);
        if (!form.isAnnounced()) throw FormException.notAnnounced();
        return DetailFormResponse.from(form);
    }

    @Transactional
    public Long submitForm(Long userId, SubmitFormRequest request) {
        Form form = queryFormService.getFormOrThrow(request.formId());

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

    public SubmitDetailFormResponse getMySubmit(Long formId, Long projectId) {
        FormSubmit submit = formSubmitRepository
                .findByFormIdAndProjectId(formId, projectId)
                .orElseThrow(FormException::notSubmitted);
        return SubmitDetailFormResponse.from(submit);
    }
}