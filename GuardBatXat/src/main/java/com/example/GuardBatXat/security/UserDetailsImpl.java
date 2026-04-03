package com.example.GuardBatXat.security;

import com.example.GuardBatXat.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        // Lấy tên Role từ DB (ví dụ: "ADMIN", "RESCUE_TEAM") thêm tiền tố "ROLE_" theo chuẩn Spring
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; } // Thực tế bạn có thể map với user.getIsActive()
}