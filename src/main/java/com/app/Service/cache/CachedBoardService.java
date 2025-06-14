package com.app.Service.cache;

import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;
import com.app.Service.BoardService;
import com.app.exception.sub.BoardNotFoundException;
import com.app.helper.CachedServiceHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Primary
public class CachedBoardService extends CachedServiceHelper<Board> implements BoardService {

    private final BoardService boardService;

    protected CachedBoardService(RedisTemplate<String, Board> boardRedisTemplate, @Qualifier("boardServiceImpl") BoardService boardService) {
        super(boardRedisTemplate);
        this.boardService = boardService;
    }

    @Override
    public Board save(BoardRequest boardRequest) {
        var board = boardService.save(boardRequest);
        var cached = super.getOrLoad("board:" + board.getId(), () -> board, Duration.ofHours(2));
        return cached.orElse(board);
    }

    @Override
    public Board addPinToBoard(Long pinId, Long boardId) {
        return boardService.addPinToBoard(pinId, boardId);
    }

    @Override
    public Board deletePinFromBoard(Long pinId, Long boardId) {
        return boardService.deletePinFromBoard(pinId, boardId);
    }

    @Override
    public Board update(Long id, String name) {
        var board = boardService.update(id, name);
        super.delete("board:" + board.getId());
        var cached = super.getOrLoad("board:" + board.getId(), () -> board, Duration.ofHours(2));
        return cached.orElse(board);
    }

    @Override
    public Board findById(Long id) {
        var cached = super.getOrLoad("board:" + id, () -> boardService.findById(id), Duration.ofHours(2));
        return cached.orElseThrow(() -> new BoardNotFoundException("Board not found with a id: " + id));
    }

    @Override
    public List<Board> findAllByUserId(Long userId, int limit, int offset) {
        return super.getListOrLoad("user:" + userId + ":board", () -> boardService.findAllByUserId(userId, limit, offset), limit, offset, Duration.ofHours(2));
    }

    @Override
    public void deleteIfUserMatches(Long id) {
        super.delete("board:" + id);
    }
}
