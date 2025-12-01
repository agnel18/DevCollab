package tech.ignitr.habitus.web.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.ignitr.habitus.data.user.User;
import tech.ignitr.habitus.security.AuthService;
import tech.ignitr.habitus.security.AuthenticationResponse;
import tech.ignitr.habitus.service.user.UserService;

import java.util.UUID;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping(path="/")
    public ResponseEntity<AuthenticationResponse> putUser(@RequestBody RegisterRequest request){
        return authService.registerUser(request);
    }

    @GetMapping(path="/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody LoginRequest request){
        return authService.authenticateUser(request);
    }

    @GetMapping(path = "/")
    public ResponseEntity<User> getUser(@RequestParam("id") UUID id){
        return userService.getUser(id);
    }

    @PutMapping(path="/")
    public ResponseEntity<User> putUser(@RequestBody UserRequest request){
        return userService.putUser(request);
    }

    @DeleteMapping(path="/")
    public ResponseEntity<Void> deleteUser(@RequestParam("id") UUID id){
        return userService.deleteUser(id);
    }

}
