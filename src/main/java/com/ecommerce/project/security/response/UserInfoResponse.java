package com.ecommerce.project.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

    private long id;
    private String jwtToken;
    private String username;
    private List<String> roles;

}
