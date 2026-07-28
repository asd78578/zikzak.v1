package com.example.zikzak.component;

import com.example.zikzak.dto.UserResponse;
import com.example.zikzak.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername()
        );
    }

}
