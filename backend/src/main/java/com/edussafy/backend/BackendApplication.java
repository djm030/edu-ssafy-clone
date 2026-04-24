package com.edussafy.backend;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RabbitAutoConfiguration.class
})
public class BackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

  static Map<String, Object> obj(Object... pairs) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      map.put(String.valueOf(pairs[i]), pairs[i + 1]);
    }
    return map;
  }

  static List<Map<String, Object>> items(Map<String, Object>... values) {
    return new ArrayList<>(List.of(values));
  }

  @RestController
  @RequestMapping("/api")
  static class ApiController {
    private final AtomicLong idSequence = new AtomicLong(9000);
    private Map<String, Object> profile = obj(
        "id", 1L,
        "name", "김싸피",
        "email", "learner@ssafy.com",
        "role", "LEARNER",
        "campusName", "서울",
        "cohortName", "12기",
        "trackName", "Java 전공",
        "learnerNo", "1200001",
        "className", "서울 1반",
        "zipCode", "06234",
        "addressLine1", "서울특별시 강남구 테헤란로",
        "addressLine2", "멀티캠퍼스",
        "mobilePhone", "010-1234-5678",
        "emergencyPhone", "010-9999-0000",
        "marketingOptIn", true
    );
    private final List<Map<String, Object>> posts = items(
        post(101L, "notice", 1L, "운영", "4월 월말평가 안내", "월말평가 일정과 준비물을 확인하세요.", true),
        post(102L, "faq", 2L, "학사", "출결 정정은 어디서 하나요?", "마이캠퍼스 출석현황에서 이의신청을 제출할 수 있습니다.", false),
        post(201L, "free", 3L, "스터디", "알고리즘 스터디 모집", "평일 저녁 문제 풀이 스터디원을 모집합니다.", false),
        post(301L, "qna", 4L, "학습", "프로젝트 서버 배포 문의", "배포 권한과 절차를 문의합니다.", false)
    );

    @GetMapping("/health")
    Map<String, Object> health() {
      return obj("status", "UP", "service", "edussafy-backend");
    }

    @PostMapping("/auth/login")
    Map<String, Object> login(@RequestBody Map<String, Object> request) {
      String email = String.valueOf(request.getOrDefault("email", profile.get("email")));
      Map<String, Object> user = new LinkedHashMap<>(profile);
      user.put("email", email);
      return obj("user", user);
    }

    @GetMapping("/me")
    Map<String, Object> me() {
      return obj("user", profile);
    }

    @PostMapping("/profile/password-check")
    Map<String, Object> passwordCheck(@RequestBody Map<String, Object> request) {
      String password = String.valueOf(request.getOrDefault("password", ""));
      return obj("verified", !password.isBlank());
    }

    @GetMapping("/profile")
    Map<String, Object> getProfile() {
      return obj("profile", profile);
    }

    @PutMapping("/profile")
    Map<String, Object> updateProfile(@RequestBody Map<String, Object> request) {
      Map<String, Object> updated = new LinkedHashMap<>(profile);
      request.forEach((key, value) -> {
        if (value != null && !"id".equals(key) && !"email".equals(key) && !"role".equals(key)) {
          updated.put(key, value);
        }
      });
      profile = updated;
      return obj("profile", profile);
    }

    @GetMapping("/dashboard/summary")
    Map<String, Object> dashboard() {
      return obj(
          "user", obj("name", profile.get("name"), "campusName", profile.get("campusName"), "cohortName", profile.get("cohortName"), "trackName", profile.get("trackName")),
          "level", obj("level", 5, "exp", 4230, "nextLevelExp", 5000, "scholarshipPoints", 87, "rank", 12),
          "attendance", obj("present", 18, "late", 1, "absent", 0, "appealAvailable", true),
          "notifications", obj("unreadCount", 2, "latest", List.of(notification(1L, "월말평가 안내", "평가 일정이 등록되었습니다.", "notice", false), notification(2L, "Quest 마감", "Quest 제출 마감이 임박했습니다.", "quest", false))),
          "today", obj("curriculumTitle", "Spring REST API", "questTitle", "게시판 API 구현", "surveyTitle", "만족도 설문")
      );
    }

    @GetMapping("/attendance/records")
    Map<String, Object> attendanceRecords() {
      return obj("items", List.of(
          obj("id", 1L, "date", LocalDate.now().minusDays(2).toString(), "status", "present", "checkInAt", "09:00", "checkOutAt", "18:00"),
          obj("id", 2L, "date", LocalDate.now().minusDays(1).toString(), "status", "late", "checkInAt", "09:21", "checkOutAt", "18:05", "approvalType", "지각"),
          obj("id", 3L, "date", LocalDate.now().toString(), "status", "present", "checkInAt", "08:55", "appealAvailable", true)
      ));
    }

    @PostMapping("/attendance/appeals")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> createAppeal(@RequestBody Map<String, Object> request) {
      return obj("item", obj("id", idSequence.incrementAndGet(), "status", "requested", "type", request.get("type"), "reason", request.get("reason")));
    }

    @GetMapping("/notifications")
    Map<String, Object> notifications() {
      return obj("items", List.of(
          notification(1L, "공지 등록", "새 공지사항이 등록되었습니다.", "notice", false),
          notification(2L, "설문 요청", "설문 응답을 완료해 주세요.", "survey", false),
          notification(3L, "학습자료", "새 학습자료가 업로드되었습니다.", "learning", true)
      ));
    }

    @GetMapping("/learning/curriculum")
    Map<String, Object> curriculum() {
      return obj("items", List.of(
          obj("id", 1L, "weekNo", 1, "title", "Spring Boot 기초", "classDate", LocalDate.now().minusDays(7).toString(), "startTime", "09:00", "endTime", "18:00", "instructorName", "최강사", "classroom", "A101"),
          obj("id", 2L, "weekNo", 2, "title", "REST API 설계", "classDate", LocalDate.now().toString(), "startTime", "09:00", "endTime", "18:00", "instructorName", "최강사", "classroom", "A101"),
          obj("id", 3L, "weekNo", 3, "title", "프로젝트 실습", "classDate", LocalDate.now().plusDays(7).toString(), "startTime", "09:00", "endTime", "18:00", "instructorName", "최강사", "classroom", "A101")
      ));
    }

    @GetMapping("/learning/replays")
    Map<String, Object> replays(@RequestParam(required = false) String keyword) {
      List<Map<String, Object>> all = List.of(
          obj("id", 1L, "title", "Spring Boot 다시보기", "instructor", "최강사", "publishedAt", LocalDateTime.now().minusDays(1).toString(), "duration", "01:20:00", "versionNo", 1, "watched", true),
          obj("id", 2L, "title", "JPA 기초 특강", "instructor", "김강사", "publishedAt", LocalDateTime.now().minusDays(3).toString(), "duration", "00:55:00", "versionNo", 2, "watched", false)
      );
      return obj("items", filterByTitle(all, keyword));
    }

    @GetMapping("/learning/materials")
    Map<String, Object> materials(@RequestParam(required = false) String keyword, @RequestParam(required = false) String type) {
      List<Map<String, Object>> all = materialSeed();
      List<Map<String, Object>> filtered = all.stream()
          .filter(item -> containsTitle(item, keyword))
          .filter(item -> type == null || type.isBlank() || Objects.equals(type, item.get("materialTypeCode")))
          .toList();
      return obj("items", filtered, "page", page(1, filtered.size(), filtered.size()));
    }

    @GetMapping("/learning/materials/{id}")
    Map<String, Object> material(@PathVariable long id) {
      return obj("item", findById(materialSeed(), id));
    }

    @GetMapping("/learning/materials/{id}/resources")
    Map<String, Object> materialResources(@PathVariable long id) {
      return obj("items", List.of(obj("id", id * 10, "title", "실습 PDF", "type", "file", "targetUrl", "/files/material-" + id + ".pdf")));
    }

    @GetMapping("/quests")
    Map<String, Object> quests() {
      List<Map<String, Object>> items = questSeed();
      return obj("items", items, "page", page(1, items.size(), items.size()));
    }

    @GetMapping("/quests/{id}")
    Map<String, Object> quest(@PathVariable long id) {
      return obj("item", findById(questSeed(), id));
    }

    @PostMapping("/quests/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> submitQuest(@PathVariable long id, @RequestBody Map<String, Object> request) {
      return obj("item", obj("id", idSequence.incrementAndGet(), "questId", id, "status", "submitted", "content", request.get("content")));
    }

    @GetMapping("/surveys")
    Map<String, Object> surveys() {
      List<Map<String, Object>> items = surveySeed();
      return obj("items", items, "page", page(1, items.size(), items.size()));
    }

    @GetMapping("/surveys/{id}")
    Map<String, Object> survey(@PathVariable long id) {
      return obj("item", findById(surveySeed(), id));
    }

    @PostMapping("/surveys/{id}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> respondSurvey(@PathVariable long id, @RequestBody Map<String, Object> request) {
      int answerCount = request.get("answers") instanceof List<?> answers ? answers.size() : 0;
      return obj("item", obj("id", idSequence.incrementAndGet(), "surveyId", id, "completed", true, "answerCount", answerCount));
    }

    @GetMapping("/boards/{boardCode}/categories")
    Map<String, Object> categories(@PathVariable String boardCode) {
      return obj("items", List.of(
          obj("id", 1L, "name", "운영", "sortOrder", 1, "postCount", countPosts(boardCode, 1L)),
          obj("id", 2L, "name", "학사", "sortOrder", 2, "postCount", countPosts(boardCode, 2L)),
          obj("id", 3L, "name", "스터디", "sortOrder", 3, "postCount", countPosts(boardCode, 3L)),
          obj("id", 4L, "name", "학습", "sortOrder", 4, "postCount", countPosts(boardCode, 4L))
      ));
    }

    @GetMapping("/boards/{boardCode}/posts")
    Map<String, Object> boardPosts(@PathVariable String boardCode, @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size) {
      List<Map<String, Object>> filtered = posts.stream()
          .filter(post -> Objects.equals(boardCode, post.get("boardCode")))
          .filter(post -> categoryId == null || Objects.equals(categoryId, ((Map<?, ?>) post.get("category")).get("id")))
          .filter(post -> containsTitle(post, keyword) || keyword == null || keyword.isBlank() || String.valueOf(post.get("content")).toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)))
          .sorted(Comparator.comparing(post -> String.valueOf(post.get("createdAt")), Comparator.reverseOrder()))
          .toList();
      int from = Math.min(Math.max(page - 1, 0) * size, filtered.size());
      int to = Math.min(from + size, filtered.size());
      return obj("items", filtered.subList(from, to), "page", page(page, size, filtered.size()));
    }

    @PostMapping("/boards/{boardCode}/posts")
    ResponseEntity<Map<String, Object>> createPost(@PathVariable String boardCode, @RequestBody Map<String, Object> request) {
      long id = idSequence.incrementAndGet();
      Map<String, Object> created = post(id, boardCode, asLong(request.get("categoryId"), 3L), "일반", String.valueOf(request.getOrDefault("title", "제목 없음")), String.valueOf(request.getOrDefault("content", "")), false);
      posts.add(created);
      return ResponseEntity.created(URI.create("/api/boards/" + boardCode + "/posts/" + id)).body(obj("item", created));
    }

    @GetMapping("/boards/{boardCode}/posts/{postId}")
    Map<String, Object> boardPost(@PathVariable String boardCode, @PathVariable long postId) {
      Map<String, Object> post = posts.stream()
          .filter(item -> Objects.equals(boardCode, item.get("boardCode")) && Objects.equals(postId, item.get("id")))
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
      return obj("post", post);
    }

    @PostMapping("/boards/{boardCode}/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> createComment(@PathVariable String boardCode, @PathVariable long postId, @RequestBody Map<String, Object> request) {
      return obj("item", obj("id", idSequence.incrementAndGet(), "postId", postId, "content", request.get("content"), "authorName", profile.get("name"), "createdAt", LocalDateTime.now().toString()));
    }

    @PostMapping("/boards/{boardCode}/posts/{postId}/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> react(@PathVariable String boardCode, @PathVariable long postId) {
      return obj("item", obj("postId", postId, "reactionCount", 1, "reacted", true));
    }

    @GetMapping("/support/tickets")
    Map<String, Object> tickets() {
      List<Map<String, Object>> items = List.of(obj("id", 1L, "title", "출결 문의", "content", "출결 정정 문의입니다.", "status", "open", "createdAt", LocalDateTime.now().minusDays(2).toString()));
      return obj("items", items, "page", page(1, items.size(), items.size()));
    }

    @PostMapping("/support/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> createTicket(@RequestBody Map<String, Object> request) {
      return obj("item", obj("id", idSequence.incrementAndGet(), "title", request.get("title"), "content", request.get("content"), "status", "open", "createdAt", LocalDateTime.now().toString()));
    }

    @GetMapping("/community/classmates")
    Map<String, Object> classmates() {
      return obj("items", List.of(
          obj("id", 2L, "name", "이싸피", "campusName", "서울", "trackName", "Java 전공", "teamName", "A팀", "statusMessage", "오늘도 화이팅"),
          obj("id", 3L, "name", "박싸피", "campusName", "서울", "trackName", "임베디드", "teamName", "B팀", "statusMessage", "프로젝트 진행 중")
      ));
    }

    @PostMapping("/community/classmates/{userId}/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> notifyClassmate(@PathVariable long userId) {
      return obj("item", obj("userId", userId, "status", "sent"));
    }

    private static Map<String, Object> post(long id, String boardCode, long categoryId, String categoryName, String title, String content, boolean pinned) {
      return obj(
          "id", id,
          "boardCode", boardCode,
          "category", obj("id", categoryId, "name", categoryName),
          "title", title,
          "content", content,
          "authorName", "SSAFY 운영자",
          "createdAt", LocalDateTime.now().minusDays(id % 7).toString(),
          "viewCount", 120 + id,
          "commentCount", 2,
          "reactionCount", 5,
          "bookmarkCount", 1,
          "hasAttachment", false,
          "isPinned", pinned,
          "isNew", id % 2 == 1,
          "engagement", obj("commentCount", 2, "reactionCount", 5)
      );
    }

    private static Map<String, Object> notification(long id, String title, String body, String type, boolean read) {
      return obj("id", id, "title", title, "body", body, "type", type, "createdAt", LocalDateTime.now().minusHours(id).toString(), "read", read);
    }

    private static Map<String, Object> page(int page, int size, int totalItems) {
      int safeSize = Math.max(size, 1);
      return obj("page", page, "size", size, "totalItems", totalItems, "totalPages", (int) Math.ceil((double) totalItems / safeSize));
    }

    private static List<Map<String, Object>> materialSeed() {
      return List.of(
          obj("id", 1L, "title", "Spring 교재", "materialTypeCode", "ebook", "authorName", "SSAFY", "createdAt", LocalDate.now().minusDays(5).toString(), "viewCount", 45, "summary", "Spring 핵심 교재", "resources", List.of(obj("title", "spring.pdf", "type", "file", "targetUrl", "/files/spring.pdf"))),
          obj("id", 2L, "title", "REST API 영상", "materialTypeCode", "video", "authorName", "SSAFY", "createdAt", LocalDate.now().minusDays(3).toString(), "viewCount", 72, "summary", "REST API 설계 영상", "detailUrl", "https://edu.ssafy.com/replay/2")
      );
    }

    private static List<Map<String, Object>> questSeed() {
      return List.of(
          obj("id", 1L, "title", "게시판 API Quest", "startAt", LocalDate.now().minusDays(1).toString(), "endAt", LocalDate.now().plusDays(3).toString(), "submitStatus", "progress", "classification", "BACKEND", "maxExp", 100, "tasks", List.of("목록 API", "상세 API", "등록 API")),
          obj("id", 2L, "title", "React 화면 Quest", "startAt", LocalDate.now().minusDays(5).toString(), "endAt", LocalDate.now().minusDays(1).toString(), "submitStatus", "submitted", "resultStatus", "graded", "classification", "FRONTEND", "maxExp", 80)
      );
    }

    private static List<Map<String, Object>> surveySeed() {
      return List.of(
          obj("id", 1L, "title", "수업 만족도 설문", "required", true, "startAt", LocalDate.now().minusDays(1).toString(), "endAt", LocalDate.now().plusDays(5).toString(), "completed", false, "category", "learning", "questionCount", 2, "questions", List.of(obj("id", 1L, "text", "수업 난이도는 적절했나요?"), obj("id", 2L, "text", "추가 의견을 입력해 주세요.")))
      );
    }

    private static List<Map<String, Object>> filterByTitle(List<Map<String, Object>> items, String keyword) {
      return items.stream().filter(item -> containsTitle(item, keyword)).toList();
    }

    private static boolean containsTitle(Map<String, Object> item, String keyword) {
      return keyword == null || keyword.isBlank() || String.valueOf(item.get("title")).toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static Map<String, Object> findById(List<Map<String, Object>> items, long id) {
      return items.stream()
          .filter(item -> Objects.equals(item.get("id"), id))
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다."));
    }

    private long countPosts(String boardCode, long categoryId) {
      return posts.stream()
          .filter(post -> Objects.equals(boardCode, post.get("boardCode")))
          .filter(post -> Objects.equals(categoryId, ((Map<?, ?>) post.get("category")).get("id")))
          .count();
    }

    private static long asLong(Object value, long fallback) {
      if (value instanceof Number number) return number.longValue();
      try {
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
      } catch (NumberFormatException ex) {
        return fallback;
      }
    }
  }
}
