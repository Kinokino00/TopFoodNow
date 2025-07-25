package com.example.topfoodnow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String name;
    private Boolean enabled;

    private Integer roleId;
    private String roleName;

    private String ytUrl;
    private String igUrl;
}