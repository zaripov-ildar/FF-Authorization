package ru.findFood.auth.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
}
