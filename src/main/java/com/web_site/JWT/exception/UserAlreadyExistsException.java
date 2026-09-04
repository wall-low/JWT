package com.web_site.JWT.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("Пользователь с логином '" + username + "' уже существует");
    }
}
