package com.example.straffic.board.controller;

import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardDetailController {
    private final BoardService boardService;

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        BoardEntity b = boardService.getAndIncreaseViews(id);
        BoardEntity prev = boardService.getPrevious(id);
        BoardEntity next = boardService.getNext(id);
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isAuthor = auth != null && b.getAuthor() != null && b.getAuthor().getId().equals(auth.getName());
        String authName = auth != null ? auth.getName() : null;
        java.util.List<com.example.straffic.board.entity.CommentEntity> commentsSorted = new java.util.ArrayList<>(b.getComments());
        commentsSorted.sort((c1, c2) -> {
            int p1 = c1.isPinned() ? 0 : 1;
            int p2 = c2.isPinned() ? 0 : 1;
            if (p1 != p2) return p1 - p2;
            return c1.getCreatedAt().compareTo(c2.getCreatedAt());
        });
        model.addAttribute("board", b);
        model.addAttribute("previousPost", prev);
        model.addAttribute("nextPost", next);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("authName", authName);
        model.addAttribute("commentsSorted", commentsSorted);
        return "board/detail";
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        byte[] data = boardService.getImageData(id);
        MediaType mediaType = MediaType.IMAGE_JPEG;
        try {
            BoardEntity b = boardService.get(id);
            if (b.getImageContentType() != null) {
                mediaType = MediaType.parseMediaType(b.getImageContentType());
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok().contentType(mediaType).body(data);
    }
}
