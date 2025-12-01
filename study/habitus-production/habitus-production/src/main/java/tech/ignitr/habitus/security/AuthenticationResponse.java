package tech.ignitr.habitus.security;

import lombok.Builder;
import lombok.Data;
import tech.ignitr.habitus.data.user.User;

@Data
@Builder
public class AuthenticationResponse {

    private String token;
    private User user;
}
