package com.edussafy.backend.help.service;

import com.edussafy.backend.board.dto.BoardPostDetailResponse.BoardPostDetail;
import com.edussafy.backend.board.dto.BoardPostListItem;
import com.edussafy.backend.board.dto.CategoryItem;
import com.edussafy.backend.board.error.BoardNotFoundException;
import com.edussafy.backend.board.error.BoardPostNotFoundException;
import com.edussafy.backend.board.repository.BoardRepository;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardSort;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleCategoryItem;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleDetailResponse;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRuleItem;
import com.edussafy.backend.help.dto.AcademicRuleDtos.AcademicRulesResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AcademicRuleService {

    private static final String BOARD_CODE = "academic_rules";
    private static final int MAX_RULES = 100;

    private final BoardRepository boardRepository;

    public AcademicRuleService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public AcademicRulesResponse rules(Long categoryId, String keyword) {
        long boardId = requireBoardId();
        List<CategoryItem> categories = boardRepository.findCategories(boardId);
        BoardQuery query = new BoardQuery(categoryId, normalizeKeyword(keyword), 1, MAX_RULES, "id,asc");
        List<BoardPostListItem> posts = boardRepository.findPosts(boardId, query, BoardSort.parse(query.sort()));

        Map<Long, List<AcademicRuleItem>> rulesByCategory = new LinkedHashMap<>();
        for (BoardPostListItem post : posts) {
            BoardPostDetail detail = boardRepository.findPostDetail(boardId, post.id())
                    .orElseThrow(() -> new BoardPostNotFoundException(post.id()));
            AcademicRuleItem item = toRule(detail);
            rulesByCategory.computeIfAbsent(item.categoryId(), ignored -> new ArrayList<>()).add(item);
        }

        List<AcademicRuleCategoryItem> grouped = categories.stream()
                .filter(category -> categoryId == null || category.id() == categoryId)
                .map(category -> {
                    List<AcademicRuleItem> rules = rulesByCategory.getOrDefault(category.id(), List.of());
                    return new AcademicRuleCategoryItem(
                            category.id(),
                            category.name(),
                            category.sortOrder(),
                            rules.size(),
                            rules
                    );
                })
                .filter(category -> !StringUtils.hasText(keyword) || !category.rules().isEmpty())
                .toList();

        return new AcademicRulesResponse(grouped, normalizeKeyword(keyword));
    }

    public AcademicRuleDetailResponse rule(long ruleId) {
        long boardId = requireBoardId();
        BoardPostDetail detail = boardRepository.findPostDetail(boardId, ruleId)
                .orElseThrow(() -> new BoardPostNotFoundException(ruleId));
        return new AcademicRuleDetailResponse(toRule(detail));
    }

    private long requireBoardId() {
        return boardRepository.findBoardId(BOARD_CODE)
                .orElseThrow(() -> new BoardNotFoundException(BOARD_CODE));
    }

    private AcademicRuleItem toRule(BoardPostDetail detail) {
        if (detail.category() == null) {
            throw new BoardPostNotFoundException(detail.id());
        }
        return new AcademicRuleItem(
                detail.id(),
                detail.category().id(),
                detail.category().name(),
                detail.title(),
                detail.content(),
                detail.updatedAt() == null ? detail.createdAt() : detail.updatedAt()
        );
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
