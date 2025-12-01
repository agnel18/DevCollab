package tech.ignitr.habitus.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tech.ignitr.habitus.configuration.DatabaseException;
import tech.ignitr.habitus.data.user.User;
import tech.ignitr.habitus.data.user.UserRepository;
import tech.ignitr.habitus.web.user.LoginRequest;
import tech.ignitr.habitus.web.user.RegisterRequest;
import tech.ignitr.habitus.web.user.UserRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    /**
     * @param id the id identifying the user whose data is required.
     * @return response containing the user's data
     */
    @Override
    public ResponseEntity<User> getUser(UUID id) {
        try{
            return ResponseEntity.ok(repository.findById(id)
                .orElseThrow(()-> new DatabaseException("User not found", HttpStatus.NOT_FOUND)));
        }catch(DatabaseException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * @param id the id identifying the user whose data shall be updated
     * @return response containing the newly created user.
     */
    @Override
    public ResponseEntity<User> putUser( UserRequest requestBody) {
        try{
            return ResponseEntity.ok(updateUser(requestBody));
        } catch(DatabaseException e){
            return ResponseEntity.status(e.getHttpStatus()).build();
        }
    }

    /**
     * @param id, the id identifying the user to be deleted.
     * @return response containing the status of the deletion.
     */
    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        try{
            repository.delete(repository.findById(id)
                    .orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NOT_FOUND)));
            repository.flush();
            return ResponseEntity.ok().build();
        } catch(DatabaseException e){
            return ResponseEntity.status(e.getHttpStatus()).build();
        }
    }

    /**
     * @param request, the request containing the username and password of the user.
     * @return response containing the JWT token of the user.
     */
    @Override
    public ResponseEntity<String> registerUser(RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body("Test");
    }

    /**
     * @param request, the request containing the username and password of the user.
     * @return response containing the JWT token of the user.
     */
    @Override
    public ResponseEntity<String> loginUser(LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body("Test");
    }


    /**
     *
     * @param id
     * @return
     */
    private User updateUser(UserRequest requestBody) throws DatabaseException {
        return repository.findById(requestBody.getId())
                .orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NOT_FOUND));
    }

}
