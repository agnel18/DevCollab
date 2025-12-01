package tech.ignitr.habitus.web.user;

import lombok.Data;

@Data
public class RegisterRequest {

        private String name;
        private String email;
        private String password;
    }
