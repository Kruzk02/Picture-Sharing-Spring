package com.app.Service;

import com.app.DAO.SubCommentDao;
import com.app.DTO.SubCommentDTO;
import com.app.Model.SubComment;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubCommentService {

    private final SubCommentDao subCommentDao;
    private final ModelMapper modelMapper;
    private final RedisTemplate<Object,Object> redisTemplate;

    public SubComment save(SubCommentDTO subCommentDTO) {
        SubComment subComment = modelMapper.map(subCommentDTO, SubComment.class);
        System.out.println(subComment.getId());
        redisTemplate.opsForValue().set("subComment:" + subComment.getId(),subComment, Duration.ofHours(2));
        return subCommentDao.save(subComment);
    }

    public List<SubComment> findAllByCommentId(Long commentId) {
        List<SubComment> subComments = subCommentDao.findAllByCommentId(commentId);
        List<Object> cacheKeys = subComments.stream()
                .map(subComment -> "subComment:" + subComment.getId())
                .collect(Collectors.toList());

        List<Object> cacheSubComment = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0;i < Objects.requireNonNull(cacheSubComment).size();i++) {
            if (cacheSubComment.get(i) == null) {
                redisTemplate.opsForValue().set(cacheKeys.get(i),subComments.get(i),Duration.ofHours(2));
            }
        }
        return subComments;
    }

    public SubComment findById(Long id) {
        SubComment cacheSubComment = (SubComment) redisTemplate.opsForValue().get("subComment:" + id);
        if (cacheSubComment != null) {
            return cacheSubComment;
        }

        SubComment subComment = subCommentDao.findById(id);
        if (subComment != null) {
            redisTemplate.opsForValue().set("subComment" + id,subComment,Duration.ofHours(2));
        }
        return subComment;
    }

    public void deleteById(Long id) {
        SubComment subComment = subCommentDao.findById(id);
        if (subComment != null) {
            subCommentDao.deleteById(subComment.getId());
        }
    }
}
