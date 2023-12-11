package site.sac.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import site.sac.dto.PostDTO;
import site.sac.dto.UsersDTO;
import site.sac.mapper.PostMapper;
import site.sac.service.PostService;
import site.sac.service.UserLikeBoardService;
import site.sac.service.UsersService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private UserLikeBoardService userLikeBoardService;
    @Autowired
    private UsersService usersService;

    @PostMapping("/posts")
    public ResponseEntity<String> postInsert(RequestEntity<PostDTO> postDTO){
        String accessToken = postDTO.getHeaders().getFirst("accessToken");
        if(accessToken !=null && usersService.isExistToken(accessToken)){
            postDTO.getBody().setUser_id(usersService.findUserIdByToken(accessToken));
            postService.register(postDTO.getBody());
            return ResponseEntity.ok().body("insert success");
        }
        else return ResponseEntity.status(500).body("insert fail");
    }

    @GetMapping("/posts")
    public ResponseEntity<Map<String,Object>> getAllPost(){
        List<PostDTO> posts = postService.getAllPost();
        Map<String,Object> result = new HashMap<>();

        result.put("posts", posts);
        result.put("count", posts.size());
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> getPostDetail(RequestEntity<String> requestEntity, @PathVariable Long postId){
        String accessToken = requestEntity.getHeaders().getFirst("accessToken");

        if(accessToken !=null && usersService.isExistToken(accessToken)){
            PostDTO postDetail = postService.getPostDetail(postId);
            return new ResponseEntity<>(postDetail, HttpStatus.OK);
        }
        else return ResponseEntity.status(500).body(null);

    }
    @GetMapping("/posts/{boardId}")
    public ResponseEntity<Map<String,Object>> getAllPostByBoardId(RequestEntity<PostDTO> postDTO, @PathVariable Long boardId){
        List<PostDTO> posts = postService.getPostsByBoardId(boardId);
        String accessToken = postDTO.getHeaders().getFirst("accessToken");
        if (posts==null){
            return ResponseEntity.status(500).body(null);
        }
        if(accessToken !=null && usersService.isExistToken(accessToken)){
            Map<String,Object> result = new HashMap<>();
            result.put("posts", posts);
            result.put("count", posts.size());
            return ResponseEntity.ok().body(result);
        }
        else return ResponseEntity.status(500).body(null);
    }

    @GetMapping("/posts/like")
    public ResponseEntity<Map<String,Object>> getAllPostByUserId(RequestEntity<String> requestEntity){
        String accessToken = requestEntity.getHeaders().getFirst("accessToken");
        long userId = usersService.findUserIdByToken(accessToken);
        List<String> userLikeBoard = userLikeBoardService.getAllByUserId(userId);
        List<PostDTO> posts = postService.getAllPostByUserLikeBoard(userLikeBoard);

        if (userLikeBoard==null){
            return ResponseEntity.notFound().build();
        }
        Map<String,Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("count", posts.size());
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/posts/test")
    public ResponseEntity<UsersDTO> testPost(RequestEntity<UsersDTO> users){
        log.info(users.toString());
        log.info("--------------");
        log.info(users.getHeaders().toString());
        return ResponseEntity.ok().body(users.getBody());
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> postEdit(RequestEntity<PostDTO> postDTO, @PathVariable Long postId){
        String accessToken = postDTO.getHeaders().getFirst("accessToken");
        long userId = usersService.findUserIdByToken(accessToken);

        if(accessToken !=null && usersService.isExistToken(accessToken)){
            if (postService.getPostDetail(postId).user_id == userId){
                PostDTO post = postService.postEdit(postDTO.getBody());
                return ResponseEntity.ok().body(post);
            } else return ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> postDelete(RequestEntity<PostDTO> postDTO, @PathVariable Long postId){
        String accessToken = postDTO.getHeaders().getFirst("accessToken");
        long userId = usersService.findUserIdByToken(accessToken);

        if(accessToken !=null && usersService.isExistToken(accessToken)){
            if (postService.getPostDetail(postId).user_id == userId){
                postService.delete(postId);
                return ResponseEntity.ok().body(null);
            } else return ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }
}
