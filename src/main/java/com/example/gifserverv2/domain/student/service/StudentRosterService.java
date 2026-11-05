package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.student.dto.response.RosterUploadResponse;
import com.example.gifserverv2.domain.student.dto.response.RosterUploadResponse.RowError;
import com.example.gifserverv2.domain.student.dto.response.UnregisteredStudentResponse;
import com.example.gifserverv2.domain.student.entity.StudentRoster;
import com.example.gifserverv2.domain.student.exception.StudentException;
import com.example.gifserverv2.domain.student.repository.StudentRosterRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentRosterService {

    /** 학년(1~3) + 반(1~9) + 번호(2자리). 예: 1101 */
    private static final Pattern STUDENT_NUMBER = Pattern.compile("[1-3][1-9]\\d{2}");
    private static final int MAX_NAME_LENGTH = 50;
    /** 엑셀에서 CSV 로 저장하면 CP949 로 저장되는 경우가 많다. */
    private static final Charset EXCEL_KOREAN = Charset.forName("MS949");
    /** UTF-8 with BOM 으로 저장된 파일 맨 앞의 BOM(U+FEFF). */
    private static final char BOM = 0xFEFF;

    private final StudentRosterRepository studentRosterRepository;
    private final UserRepository userRepository;

    /**
     * "학번,이름" 형식의 CSV 를 받아 명단에 추가한다. 이미 있는 학번은 이름을 갱신한다.
     * 첫 줄이 헤더(학번 칸이 숫자가 아님)면 건너뛰고, 잘못된 줄은 건너뛴 뒤 errors 로 알려준다.
     */
    @Transactional
    public RosterUploadResponse upload(AuthenticatedUser caller, MultipartFile file) {
        StudentManagerGuard.requireManager(caller);
        if (file == null || file.isEmpty()) {
            throw new StudentException(HttpStatus.BAD_REQUEST, "학생 명단 파일이 비어 있습니다.");
        }

        List<String> lines = decode(file).lines().toList();
        Map<String, String> rows = new LinkedHashMap<>();
        List<RowError> errors = new ArrayList<>();

        boolean firstRow = true;
        for (int i = 0; i < lines.size(); i++) {
            int lineNo = i + 1;
            String line = lines.get(i).strip();
            if (line.isEmpty()) continue;

            String[] cells = line.split(",", -1);
            String studentNumber = cell(cells, 0);
            String name = cell(cells, 1);

            if (firstRow) {
                firstRow = false;
                if (studentNumber.isEmpty() || !studentNumber.chars().allMatch(Character::isDigit)) continue;
            }

            if (!STUDENT_NUMBER.matcher(studentNumber).matches()) {
                errors.add(new RowError(lineNo, "학번 형식이 올바르지 않습니다: " + studentNumber));
            } else if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
                errors.add(new RowError(lineNo, "이름이 비어 있거나 " + MAX_NAME_LENGTH + "자를 넘습니다."));
            } else if (rows.containsKey(studentNumber)) {
                errors.add(new RowError(lineNo, "파일 안에 중복된 학번입니다: " + studentNumber));
            } else {
                rows.put(studentNumber, name);
            }
        }

        Map<String, StudentRoster> existing = rows.isEmpty() ? Map.of()
                : studentRosterRepository.findAllByStudentNumberIn(rows.keySet()).stream()
                .collect(Collectors.toMap(StudentRoster::getStudentNumber, Function.identity()));

        int created = 0;
        int updated = 0;
        List<StudentRoster> newEntries = new ArrayList<>();
        for (Map.Entry<String, String> row : rows.entrySet()) {
            StudentRoster roster = existing.get(row.getKey());
            if (roster == null) {
                newEntries.add(new StudentRoster(row.getKey(), row.getValue()));
                created++;
            } else if (!roster.getName().equals(row.getValue())) {
                roster.update(row.getValue());
                updated++;
            }
        }
        studentRosterRepository.saveAll(newEntries);

        return new RosterUploadResponse(created, updated, errors);
    }

    /** 명단에는 있지만 아직 DataGSM 로그인으로 가입하지 않은 학생. 학번으로 비교한다. */
    @Transactional(readOnly = true)
    public List<UnregisteredStudentResponse> getUnregistered(AuthenticatedUser caller, Integer grade) {
        StudentManagerGuard.requireManager(caller);

        Set<String> registered = userRepository.findAllStudents().stream()
                .map(UserEntity::getStudentNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return studentRosterRepository.findAllByOrderByStudentNumberAsc().stream()
                .filter(roster -> grade == null || grade.equals(roster.getGrade()))
                .filter(roster -> !registered.contains(roster.getStudentNumber()))
                .map(roster -> new UnregisteredStudentResponse(
                        roster.getStudentNumber(),
                        roster.getName(),
                        roster.getGrade(),
                        roster.getClassNo(),
                        roster.getStudentNo()))
                .toList();
    }

    private static String decode(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new StudentException(HttpStatus.BAD_REQUEST, "학생 명단 파일을 읽을 수 없습니다.");
        }
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            text = new String(bytes, EXCEL_KOREAN);
        }
        return (!text.isEmpty() && text.charAt(0) == BOM) ? text.substring(1) : text;
    }

    private static String cell(String[] cells, int index) {
        if (index >= cells.length) return "";
        String value = cells[index].strip();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).strip();
        }
        return value;
    }
}
