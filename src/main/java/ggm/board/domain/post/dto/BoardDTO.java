package ggm.board.domain.post.dto;

import ggm.board.domain.post.entity.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardDTO {
    private List<PostDTO>  postList;

    private int currentPage;

    private int totalPages;
}
