package com.example.GuardBatXat.security;

import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        // Cấm đăng nhập nếu tài khoản bị Admin khóa
        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản này đã bị khóa!");
        }

        return UserDetailsImpl.build(user);
    }
}