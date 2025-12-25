package ggm.board.domain.post.controller;

import ggm.board.domain.auth.entity.CustomUserDetails;
import ggm.board.domain.member.dto.MemberDTO;
import ggm.board.domain.member.entity.Member;
import ggm.board.domain.member.service.MemberService;
import ggm.board.domain.post.dto.BoardDTO;
import ggm.board.domain.post.dto.PostDTO;
import ggm.board.domain.post.dto.ReplyDTO;
import ggm.board.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {
    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("")
    public String boardPage(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<PostDTO> searchPostResult = postService.findAllPosts(pageRequest);
        List<PostDTO> postList = searchPostResult.getContent();
        int totalPages = searchPostResult.getTotalPages();

        BoardDTO boardDTO = new BoardDTO();
        MemberDTO memberDTO = new MemberDTO();

        boardDTO.setPostList(postList);
        boardDTO.setCurrentPage(page);
        boardDTO.setTotalPages(totalPages);

        model.addAttribute("boardDTO", boardDTO);

        if (customUserDetails != null && customUserDetails.isAccountNonExpired()) {
            long userid = customUserDetails.getId();
            Member member = memberService.getMemberUserById(userid);
            memberDTO.setId(userid);
            memberDTO.setName(member.getName());
        }
        model.addAttribute("memberDTO", memberDTO);

        return "board/index";
    }

    @GetMapping("/post")
    public String boardId(@RequestParam(value = "postId") long postId,
                          @AuthenticationPrincipal CustomUserDetails customUserDetails,
                          Model model) {
        PostDTO postDTO = postService.findByIdWithDetails(postId);
        model.addAttribute("postDTO", postDTO);
        model.addAttribute("replyDTO", ReplyDTO.builder().postId(postDTO.getId()).authorId(postDTO.getAuthorId()).build());

        MemberDTO memberDTO = new MemberDTO();
        if (customUserDetails != null && customUserDetails.isAccountNonExpired()) {
            long userid = customUserDetails.getId();
            Member member = memberService.getMemberUserById(userid);
            memberDTO.setId(userid);
            memberDTO.setName(member.getName());
        }
        model.addAttribute("memberDTO", memberDTO);
        return "board/post/index";
    }

    @GetMapping("/post/create")
    public String createPostPage(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (customUserDetails == null) {
            redirectAttributes.addAttribute("redirectUri", "/board/post/create");
            return "redirect:/auth/loginPage";
        }
        model.addAttribute("postDTO", new PostDTO());

        return "board/post/create";
    }

    @PostMapping("/post/create")
    public String createPost(PostDTO postDTO,
                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        postDTO.setAuthorId(customUserDetails.getId());
        postDTO.setAuthorName(customUserDetails.getUsername());

        PostDTO savedPostDTO = postService.savePost(postDTO);

        redirectAttributes.addAttribute("postId", savedPostDTO.getId());
        return "redirect:/board/post";
    }

    @GetMapping("/post/edit")
    public String editPostPage(@RequestParam(value = "postId") long postId,
                               Model model) {
        PostDTO postDTO = postService.findByIdWithDetails(postId);
        model.addAttribute("postDTO", postDTO);

        return "board/post/edit";
    }

    @PostMapping("/post/edit")
    public String editPostSave(@RequestParam(value = "postId") long postId,
                               @Validated PostDTO postDTO,
                               @AuthenticationPrincipal CustomUserDetails customUserDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        PostDTO updatedPostDTO = postService.updatePost(postDTO, customUserDetails);
        redirectAttributes.addAttribute("postId", updatedPostDTO.getId());
        return "redirect:/board/post";
    }

    @GetMapping("/post/delete")
    public String deletePost(@RequestParam(value = "postId") long postId,
                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        postService.deletePost(postId, customUserDetails);

        return "redirect:/board";
    }

    @PostMapping("/post/reply")
    public String postReply(@Validated ReplyDTO replyDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (bindingResult.hasErrors()) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.out.println("오류 코드: " + error.getCode());
                System.out.println("오류 메시지: " + error.getDefaultMessage());
            }
            return "redirect:/board";
        }

        if (customUserDetails == null) {
            redirectAttributes.addAttribute("redirectUri", "/board/post?postId=${replyDTO.postId}");
            return "redirect:/auth/loginPage";
        }

        replyDTO.setAuthorId(customUserDetails.getId());
        postService.saveReply(replyDTO);

        redirectAttributes.addAttribute("postId", replyDTO.getPostId());
        return "redirect:/board/post";
    }

    @GetMapping("/post/reply/delete")
    public String deltePostReply(@RequestParam(value = "postId") long postId,
                                 @RequestParam(value = "replyId") long replyId,
                                 @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                 RedirectAttributes redirectAttributes) {

        postService.deleteReply(replyId, customUserDetails);

        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/board/post";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleException(NoHandlerFoundException exception, Model model) {
        model.addAttribute("page", 0);
        return "redirect:/board";
    }
}
