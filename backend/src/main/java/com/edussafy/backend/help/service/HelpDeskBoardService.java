package com.edussafy.backend.help.service;

import com.edussafy.backend.board.dto.BoardCategoryListResponse;
import com.edussafy.backend.board.dto.BoardPostDetailResponse;
import com.edussafy.backend.board.dto.BoardPostListResponse;
import com.edussafy.backend.board.service.BoardQuery;
import com.edussafy.backend.board.service.BoardService;
import org.springframework.stereotype.Service;

@Service
public class HelpDeskBoardService {

    private static final String NOTICE_BOARD_CODE = "notice";
    private static final String FAQ_BOARD_CODE = "faq";

    private final BoardService boardService;

    public HelpDeskBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    public BoardCategoryListResponse noticeCategories() {
        return boardService.getCategories(NOTICE_BOARD_CODE);
    }

    public BoardPostListResponse notices(BoardQuery query) {
        return boardService.getPosts(NOTICE_BOARD_CODE, query);
    }

    public BoardPostDetailResponse notice(long noticeId) {
        return boardService.getPost(NOTICE_BOARD_CODE, noticeId);
    }

    public BoardCategoryListResponse faqCategories() {
        return boardService.getCategories(FAQ_BOARD_CODE);
    }

    public BoardPostListResponse faqs(BoardQuery query) {
        return boardService.getPosts(FAQ_BOARD_CODE, query);
    }

    public BoardPostDetailResponse faq(long faqId) {
        return boardService.getPost(FAQ_BOARD_CODE, faqId);
    }
}
