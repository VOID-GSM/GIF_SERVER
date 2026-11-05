package com.example.gifserverv2.domain.user.service;

import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.user.dto.request.UpdateMypageRequest;
import com.example.gifserverv2.domain.user.dto.request.UserLinkRequest;
import com.example.gifserverv2.domain.user.dto.response.MypageResponse;
import com.example.gifserverv2.domain.user.dto.response.UserLinkResponse;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.entity.UserLink;
import com.example.gifserverv2.domain.user.repository.UserLinkRepository;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageService {

    private static final String PROFILE_IMAGE_DIR = "users/profile";
    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024 * 1024;
    // svg 는 같은 도메인에서 서빙될 때 스크립트가 실행될 수 있어 제외한다.
    private static final Set<String> PROFILE_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final UserRepository userRepository;
    private final UserLinkRepository userLinkRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FileStorageService fileStorageService;
    private final TransactionTemplate transactionTemplate;

    // ===== 기본 정보 =====

    @Transactional(readOnly = true)
    public MypageResponse getMypage(Long userId) {
        return buildMypage(requireUser(userId));
    }

    @Transactional
    public MypageResponse updateMypage(Long userId, UpdateMypageRequest request) {
        UserEntity user = requireUser(userId);

        if (request.major() != null) {
            String major = request.major().trim();
            user.updateMajor(major.isEmpty() ? null : major);
        }
        if (request.techStacks() != null) {
            user.replaceTechStacks(normalizeTechStacks(request.techStacks()));
        }
        return buildMypage(user);
    }

    // ===== 프로필 이미지 =====

    public MypageResponse updateProfileImage(Long userId, MultipartFile file) {
        validateProfileImage(file);

        // 파일을 먼저 저장하고, DB 반영에 실패하면 새 파일을 지운다. 기존 파일은 DB 반영 후에 지운다.
        String newUrl = fileStorageService.save(file, PROFILE_IMAGE_DIR);
        String oldUrl;
        try {
            oldUrl = transactionTemplate.execute(status -> {
                UserEntity user = requireUser(userId);
                String previous = user.getProfileImageUrl();
                user.changeProfileImageUrl(newUrl);
                return previous;
            });
        } catch (RuntimeException e) {
            deleteFileQuietly(newUrl);
            throw e;
        }
        deleteFileQuietly(oldUrl);
        return readMypage(userId);
    }

    public MypageResponse deleteProfileImage(Long userId) {
        String oldUrl = transactionTemplate.execute(status -> {
            UserEntity user = requireUser(userId);
            String previous = user.getProfileImageUrl();
            user.changeProfileImageUrl(null);
            return previous;
        });
        deleteFileQuietly(oldUrl);
        return readMypage(userId);
    }

    // ===== 개인 사이트 링크 =====

    @Transactional(readOnly = true)
    public List<UserLinkResponse> getLinks(Long userId) {
        return userLinkRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(UserLinkResponse::from)
                .toList();
    }

    @Transactional
    public UserLinkResponse addLink(Long userId, UserLinkRequest request) {
        String url = normalizeUrl(request.url());
        String title = normalizeTitle(request.title());

        // 동시에 추가 요청이 와도 최대 개수를 넘지 않도록 사용자 행을 잠그고 센다.
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        if (userLinkRepository.countByUserId(userId) >= UserLink.MAX_LINKS_PER_USER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "개인 사이트 링크는 최대 " + UserLink.MAX_LINKS_PER_USER + "개까지 등록할 수 있습니다.");
        }
        return UserLinkResponse.from(userLinkRepository.save(new UserLink(userId, title, url)));
    }

    @Transactional
    public UserLinkResponse updateLink(Long userId, Long linkId, UserLinkRequest request) {
        UserLink link = requireLink(userId, linkId);
        link.update(normalizeTitle(request.title()), normalizeUrl(request.url()));
        return UserLinkResponse.from(link);
    }

    @Transactional
    public void deleteLink(Long userId, Long linkId) {
        userLinkRepository.delete(requireLink(userId, linkId));
    }

    // ===== 내부 =====

    private MypageResponse readMypage(Long userId) {
        TransactionTemplate readOnly = new TransactionTemplate(transactionTemplate.getTransactionManager());
        readOnly.setReadOnly(true);
        return readOnly.execute(status -> buildMypage(requireUser(userId)));
    }

    private MypageResponse buildMypage(UserEntity user) {
        String studentNumber = user.getStudentNumber();
        boolean validStudentNumber = studentNumber != null && studentNumber.matches("\\d{4}");
        Integer grade = validStudentNumber ? Character.getNumericValue(studentNumber.charAt(0)) : parseIntOrNull(user.getGrade());
        Integer classNo = validStudentNumber ? Character.getNumericValue(studentNumber.charAt(1)) : null;

        List<MypageResponse.TeamInfo> teams = projectMemberRepository.findAllByUserId(user.getId()).stream()
                .sorted(Comparator.comparing((ProjectMember m) -> m.getRole() != ClientRole.LEADER)
                        .thenComparing(m -> m.getProject().getId()))
                .map(m -> new MypageResponse.TeamInfo(
                        m.getProject().getId(),
                        m.getProject().getName(),
                        m.getProject().getTeamName(),
                        m.getRole().name(),
                        m.getPart()))
                .toList();

        return new MypageResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDisplayProfileImageUrl(),
                user.getProfileImageUrl() != null,
                studentNumber,
                grade,
                classNo,
                user.getMajor(),
                List.copyOf(user.getTechStacks()),
                teams,
                getLinks(user.getId())
        );
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }

    private UserLink requireLink(Long userId, Long linkId) {
        return userLinkRepository.findByIdAndUserId(linkId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "링크를 찾을 수 없습니다."));
    }

    /** 앞뒤 공백 제거, 대소문자만 다른 중복은 처음 것만 남긴다. */
    static List<String> normalizeTechStacks(List<String> techStacks) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String techStack : techStacks) {
            if (techStack == null || techStack.isBlank()) {
                continue;
            }
            String trimmed = techStack.trim();
            unique.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
        }
        return List.copyOf(unique.values());
    }

    /** http/https 주소만 허용한다. (javascript: 등 차단) */
    static String normalizeUrl(String url) {
        String trimmed = url == null ? "" : url.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "http 또는 https 주소만 등록할 수 있습니다.");
            }
            return trimmed;
        } catch (java.net.URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바른 링크 주소가 아닙니다.");
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }

    private static void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일이 비어 있습니다.");
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 이미지는 5MB 이하여야 합니다.");
        }
        String filename = file.getOriginalFilename();
        String extension = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!PROFILE_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 이미지는 png, jpg, jpeg, webp 파일만 업로드할 수 있습니다.");
        }
    }

    private void deleteFileQuietly(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            fileStorageService.delete(fileUrl);
        } catch (RuntimeException e) {
            log.warn("프로필 이미지 파일 삭제 실패: {}", fileUrl, e);
        }
    }

    private static Integer parseIntOrNull(String value) {
        if (value == null || !value.matches("\\d+")) {
            return null;
        }
        return Integer.valueOf(value);
    }
}
