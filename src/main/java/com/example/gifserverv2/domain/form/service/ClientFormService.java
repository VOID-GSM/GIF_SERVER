package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.request.SubmitFormRequest;
import com.example.gifserverv2.domain.form.dto.request.UpdateSubmitAnswerRequest;
import com.example.gifserverv2.domain.form.dto.request.UpdateSubmitRequest;
import com.example.gifserverv2.domain.form.dto.response.DetailFormResponse;
import com.example.gifserverv2.domain.form.dto.response.ListFormResponse;
import com.example.gifserverv2.domain.form.dto.response.SubmitDetailFormResponse;
import com.example.gifserverv2.domain.form.entity.CalendarEvent;
import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormFieldAnswerRepository;
import com.example.gifserverv2.domain.form.repository.FormFieldRepository;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final ProjectRepository projectRepository;

    public List<ListFormResponse> getAnnouncedForms(Long projectId) {
        List<Form> forms = formRepository.findAllByAnnouncedTrueOrderByDeadlineAsc();

        if (projectId == null) {
            return forms.stream()
                    .map(form -> ListFormResponse.from(form, false, null))
                    .toList();
        }

        Map<Long, FormSubmit> submitMap = new HashMap<>();
        for (FormSubmit submit : formSubmitRepository.findAllByProjectId(projectId)) {
            submitMap.put(submit.getForm().getId(), submit);
        }

        return forms.stream()
                .map(form -> {
                    FormSubmit submit = submitMap.get(form.getId());
                    boolean submitted = submit != null;
                    Boolean deadlineComplied = null;
                    if (submit != null) {
                        deadlineComplied = !submit.getSubmittedAt().isAfter(form.getDeadline());
                    }
                    return ListFormResponse.from(form, submitted, deadlineComplied);
                })
                .toList();
    }

    public DetailFormResponse getForm(Long formId, Long projectId) {
        Form form = queryFormService.getFormOrThrow(formId);
        if (!form.isAnnounced()) throw FormException.notAnnounced();

        Boolean deadlineComplied = null;
        if (projectId != null) {
            deadlineComplied = formSubmitRepository
                    .findByFormIdAndProjectId(formId, projectId)
                    .map(submit -> !submit.getSubmittedAt().isAfter(form.getDeadline()))
                    .orElse(null);
        }

        return DetailFormResponse.from(form, deadlineComplied);
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

            FormFieldAnswer answer = FormFieldAnswer.builder()
                    .formSubmit(submit)
                    .formField(field)
                    .textAnswer(answerReq.textAnswer())
                    .filePath(answerReq.filePath())
                    .fileSize(answerReq.fileSize())
                    .build();
            formFieldAnswerRepository.save(answer);

            if (field.getType() == FormField.FieldType.CALENDAR && answerReq.dateAnswer() != null) {
                answerReq.dateAnswer().forEach(eventReq -> {
                    CalendarEvent event = CalendarEvent.builder()
                            .formFieldAnswer(answer)
                            .eventName(eventReq.eventName())
                            .startDate(java.time.LocalDate.parse(eventReq.startDate()))
                            .endDate(java.time.LocalDate.parse(eventReq.endDate()))
                            .color(eventReq.color())
                            .build();
                    answer.getCalendarEvents().add(event);
                });
            }
            formFieldAnswerRepository.save(answer);
        });

        return submit.getId();
    }

    public SubmitDetailFormResponse getMySubmit(Long formId, Long projectId) {
        FormSubmit submit = formSubmitRepository
                .findByFormIdAndProjectId(formId, projectId)
                .orElseThrow(FormException::notSubmitted);

        String teamName = projectRepository.findById(projectId)
                .map(Project::getTeamName)
                .orElse(null);

        return SubmitDetailFormResponse.from(submit, teamName);
    }

    @Transactional
    public void updateSubmit(Long userId, UpdateSubmitRequest request) {
        FormSubmit submit = formSubmitRepository.findById(request.submitId())
                .orElseThrow(FormException::notSubmitted);

        if (!submit.getSubmittedByUserId().equals(userId)) {
            throw new FormException(HttpStatus.FORBIDDEN, "본인이 제출한 양식만 수정할 수 있습니다.");
        }

        if (submit.getForm().isDeadlinePassed()) {
            throw FormException.deadlinePassed();
        }

        List<FormFieldAnswer> existing = formFieldAnswerRepository.findAllByFormSubmitId(request.submitId());
        formFieldAnswerRepository.deleteAll(existing);

        Map<Long, UpdateSubmitAnswerRequest> answerMap = new HashMap<>();
        for (UpdateSubmitAnswerRequest answer : request.answers()) {
            answerMap.put(answer.fieldId(), answer);
        }

        List<FormField> fields = formFieldRepository.findAllById(answerMap.keySet());
        if (fields.size() != answerMap.size()) {
            throw FormException.fieldNotFound();
        }

        List<FormFieldAnswer> newAnswers = new ArrayList<>();
        for (FormField field : fields) {
            if (!field.getForm().getId().equals(submit.getForm().getId())) {
                throw new FormException(HttpStatus.BAD_REQUEST, "해당 양식에 존재하지 않는 항목입니다.");
            }

            UpdateSubmitAnswerRequest answerReq = answerMap.get(field.getId());

            FormFieldAnswer answer = FormFieldAnswer.builder()
                    .formSubmit(submit)
                    .formField(field)
                    .textAnswer(answerReq.textAnswer())
                    .filePath(answerReq.filePath())
                    .fileSize(answerReq.fileSize())
                    .originalFileName(answerReq.originalFileName())
                    .build();

            if (field.getType() == FormField.FieldType.CALENDAR && answerReq.dateAnswer() != null) {
                answerReq.dateAnswer().forEach(eventReq -> {
                    CalendarEvent event = CalendarEvent.builder()
                            .formFieldAnswer(answer)
                            .eventName(eventReq.eventName())
                            .startDate(eventReq.startDate())
                            .endDate(eventReq.endDate())
                            .color(eventReq.color())
                            .build();
                    answer.getCalendarEvents().add(event);
                });
            }
            newAnswers.add(answer);
        }
        formFieldAnswerRepository.saveAll(newAnswers);
    }
}