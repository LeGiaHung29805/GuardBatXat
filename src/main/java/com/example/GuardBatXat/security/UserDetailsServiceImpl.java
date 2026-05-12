package com.example.GuardBatXat.security;

import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // BẮT BUỘC IMPORT CÁI NÀY

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional // QUAN TRỌNG: Mở luồng DB để lấy được Role mà không bị lỗi Lazy Loading
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        // 1. Tìm user trong bảng batxat_users
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + identifier));

        // 2. DÙNG ĐÚNG CLASS CỦA BẠN (UserDetailsImpl) THAY VÌ CLASS CỦA SPRING
        return UserDetailsImpl.build(user);
    }
}