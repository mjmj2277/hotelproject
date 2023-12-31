package hotelspring.hotelproject.service;

import hotelspring.hotelproject.domain.Board;
import hotelspring.hotelproject.dto.BoardDto;
import hotelspring.hotelproject.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    private BoardRepository boardRepository;
    private static final int BLOCK_PAGE_NUM_COUNT = 5; //블럭에 존재하는 페이지 수
    private static final int PAGE_POST_COUNT = 4; //한 페이지에 존재하는 게시글 수


    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public Long savePost(BoardDto boardDto) {
         return boardRepository.save(boardDto.toEntity()).getBoardId();
    }

    // 받은 페이지 번호의 게시글만 보여지도록 한다
    @Transactional
    public List<BoardDto> getBoardlist(Integer pageNum) {

        Page<Board> page = boardRepository
                .findAll(PageRequest
                        .of(pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.ASC,"createdDate")));

        List<Board> boards = page.getContent();
        List<BoardDto> boardDtoList = new ArrayList<>();

        for(Board board : boards) {
            boardDtoList.add(this.convertEntityToDto(board));
        }
        return boardDtoList;
    }

    public Integer[] getPageList(Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        // 총 게시글 수
        Double postsTotalCount = Double.valueOf(this.getBoardCount());

        // 총 게시글 수를 기준으로 계산한 마지막 페이지 번호 계산
        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));

        // 현재 페이지를 기준으로 블럭의 마지막 페이지 번호 계산
        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT - 1)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT - 1
                : totalLastPageNum - 1;

        // 페이지 시작 번호 조정
        curPageNum = (curPageNum<=3) ? 1 : curPageNum - 2;

        //페이지 번호 할당
        for(int val = curPageNum, i=0; val <= blockLastPageNum; val++, i++) {
            pageList[i] = val;
        }

        return pageList;
    }

    @Transactional
    public Long getBoardCount() {
        return boardRepository.count();
    }

    // 각 게시글의 정보를 가져오는 기능
    @Transactional
    public BoardDto getPost(Long boardId) {
        Optional<Board> boardWrapper = boardRepository.findById(boardId);
        Board board = boardWrapper.get();

        BoardDto boardDto = BoardDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .createdDate(board.getCreatedDate())
                .build();

        return boardDto;
    }

    @Transactional
    public void deletePost(Long boardId) {
        boardRepository.deleteById(boardId);
    }

    @Transactional
    public List<BoardDto> searchPosts(String keyword) {
        List<Board> boards = boardRepository.findByTitleContaining(keyword);
        List<BoardDto> boardDtoList = new ArrayList<>();

        if(boards.isEmpty()) return boardDtoList;

        for(Board board : boards) {
            boardDtoList.add(this.convertEntityToDto(board));
        }

        return boardDtoList;
    }

    private BoardDto convertEntityToDto(Board board) {
        return BoardDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .createdDate(board.getCreatedDate())
                .build();
    }


}
