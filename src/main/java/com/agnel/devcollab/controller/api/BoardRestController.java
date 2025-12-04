package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Board;
import com.agnel.devcollab.entity.BoardColumn;
import com.agnel.devcollab.repository.BoardRepository;
import com.agnel.devcollab.repository.ColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@SuppressWarnings("null")
public class BoardRestController {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @GetMapping
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoard(@PathVariable long id) {
        return boardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardRepository.save(board);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable long id, @RequestBody Board updates) {
        return boardRepository.findById(id)
                .map(board -> {
                    if (updates.getName() != null) board.setName(updates.getName());
                    if (updates.getDescription() != null) board.setDescription(updates.getDescription());
                    if (updates.getColor() != null) board.setColor(updates.getColor());
                    return ResponseEntity.ok(boardRepository.save(board));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable long id) {
        if (boardRepository.existsById(id)) {
            boardRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{boardId}/columns")
    public List<BoardColumn> getBoardColumns(@PathVariable long boardId) {
        return columnRepository.findByBoardIdOrderByPosition(boardId);
    }

    @PostMapping("/{boardId}/columns")
    public ResponseEntity<BoardColumn> addColumn(@PathVariable long boardId, @RequestBody BoardColumn column) {
        return boardRepository.findById(boardId)
                .map(board -> {
                    column.setBoard(board);
                    // Auto-assign position as next in sequence
                    int maxPosition = board.getColumns() != null 
                        ? board.getColumns().stream().mapToInt(BoardColumn::getPosition).max().orElse(-1) 
                        : -1;
                    column.setPosition(maxPosition + 1);
                    BoardColumn saved = columnRepository.save(column);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{boardId}/columns/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable long boardId, @PathVariable long columnId) {
        if (!boardRepository.existsById(boardId)) {
            return ResponseEntity.notFound().build();
        }
        if (columnRepository.existsById(columnId)) {
            columnRepository.deleteById(columnId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{boardId}/columns/{columnId}")
    public ResponseEntity<BoardColumn> updateColumn(@PathVariable long boardId, @PathVariable long columnId, @RequestBody BoardColumn updates) {
        if (!boardRepository.existsById(boardId)) {
            return ResponseEntity.notFound().build();
        }
        return columnRepository.findById(columnId)
                .map(column -> {
                    if (updates.getName() != null) column.setName(updates.getName());
                    if (updates.getBgColor() != null) column.setBgColor(updates.getBgColor());
                    if (updates.getPosition() >= 0) column.setPosition(updates.getPosition());
                    return ResponseEntity.ok(columnRepository.save(column));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/columns/{columnId}")
    public ResponseEntity<BoardColumn> updateColumnLegacy(@PathVariable long columnId, @RequestBody BoardColumn updates) {
        return columnRepository.findById(columnId)
                .map(column -> {
                    if (updates.getName() != null) column.setName(updates.getName());
                    if (updates.getBgColor() != null) column.setBgColor(updates.getBgColor());
                    if (updates.getPosition() >= 0) column.setPosition(updates.getPosition());
                    return ResponseEntity.ok(columnRepository.save(column));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
