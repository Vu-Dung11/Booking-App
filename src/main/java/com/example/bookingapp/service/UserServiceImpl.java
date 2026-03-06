//package com.example.bookingapp.service;
//
//import com.example.cruddemo.dto.UserDTO;
//import com.example.cruddemo.entity.User;
//import com.example.cruddemo.form.UserCreateForm;
//import com.example.cruddemo.repository.UserRepository;
//import com.example.cruddemo.services.UserService;
//import lombok.AllArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//
//@Service
//@AllArgsConstructor
//public class UserServiceImpl implements UserService, UserDetailsService {
//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder; // thư viện để mã hóa mật khẩu
//    private ModelMapper modelMapper;
//
//
//
//    @Override
//    public UserDTO create(UserCreateForm form) {
//        // map form thành một entity
//        var user = modelMapper.map(form, User.class);
//        // mã hóa mật khẩu
//        var endcodedPassword = passwordEncoder.encode(form.getPassword());
//        //set lại mật khẩu cho user
//        user.setPassword(endcodedPassword);
//        // lưu entity vào database
//        var savedUser = userRepository.save(user);
//        // trả về dto, bằng cách map từ save sang dto
//        return modelMapper.map(savedUser, UserDTO.class);
//    }
//
//    @Override
//    public Page<UserDTO> findAll(Pageable pageable) {
//        var user = userRepository.findAll(pageable);
//        return user.map(user1 -> modelMapper.map(user1, UserDTO.class));
//    }
//
//
//    @Override
//    // username truyền vào có thể là vừa username vừa email đều nhận
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        var user = userRepository.findByUsernameOrEmail(username, username);
//        if (user == null) {
//            throw new UsernameNotFoundException(username);
//        }
//        var role = user.getRole().name();
//        var authorities = AuthorityUtils.createAuthorityList(role);
//        return new org.springframework.security.core.userdetails.User(
//                username,
//                user.getPassword(),
//                authorities
//        );
//    }
//}
