package tech.ignitr.habitus.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class DatabaseException extends Exception  {

    String errorMessage;
    HttpStatus httpStatus;
}
