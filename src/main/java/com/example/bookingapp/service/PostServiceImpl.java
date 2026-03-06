//package com.example.bookingapp.service;
//
//import com.example.cruddemo.dto.PostDTO;
//import com.example.cruddemo.entity.Post;
//import com.example.cruddemo.form.PostCreateForm;
//import com.example.cruddemo.form.PostFilterForm;
//import com.example.cruddemo.form.PostUpdateForm;
//import com.example.cruddemo.mapper.PostMapper;
//import com.example.cruddemo.repository.PostRepository;
//import com.example.cruddemo.services.PostServices;
//import com.example.cruddemo.specification.PostSpecification;
//import lombok.AllArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@Primary
//@AllArgsConstructor
//public class PostServiceImpl implements PostServices {
//    private PostRepository postRepository;
//    private ModelMapper modelMapper;
//
//    @Override
//    public Page<PostDTO> findAll(PostFilterForm form, Pageable pageable) {
//        var spec = PostSpecification.buildSpec(form);
//        return postRepository.findAll(spec, pageable)
//                // đầu vào là post entity, đầu ra là post dto
//                .map(post -> modelMapper.map(post, PostDTO.class));
//    }
//
//    @Override
//    public PostDTO findById(UUID id) {
//        return postRepository.findById(id)
//                .map(post -> modelMapper.map(post, PostDTO.class)).orElse(null);
//    }
//
//    @Override
//    public PostDTO create(PostCreateForm form) {
//        var post = modelMapper.map(form, Post.class);
//        var savedPost = postRepository.save(post);
//        return modelMapper.map(savedPost, PostDTO.class);
//    }
//
//    @Override
//    public PostDTO update(UUID id, PostUpdateForm form) {
//        var post = postRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Post not found with id = " + id));
//        PostMapper.map(form, post); // ánh xạ dữ liệu update vào entity cũ
//        var savedPost = postRepository.save(post);
//        return PostMapper.map(savedPost);
//    }
//
//    @Override
//    public void delete(UUID id) {
//        if (!postRepository.existsById(id)) {
//            throw new RuntimeException("Post not found with id = " + id);
//        }
//        postRepository.deleteById(id);
//    }
//}
////    @Override
////    public UserDTO update(Long id, UserForm form) {
////        User user = userRepository.findById(id)
////                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
////
////        if (!user.getUsername().equals(form.getUsername()) &&
////            userRepository.existsByUsername(form.getUsername())) {
////            throw new RuntimeException("Username already exists: " + form.getUsername());
////        }
////
////        user.setUsername(form.getUsername());
////        if (form.getPassword() != null && !form.getPassword().isEmpty()) {
////            user.setPassword(form.getPassword());
////        }
////        user.setFullname(form.getFullname());
////        user.setRole(form.getRole());
////
////        User updatedUser = userRepository.save(user);
////        return convertToDTO(updatedUser);
////    }
