package com.philomath.controller;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Payload;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Global exception handler
@RestControllerAdvice(basePackages = "com.philomath.controller")
public class GlobalExceptionHandler {

    // Handle MethodArgumentNotValidException
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("exception", ex.getClass().getName() + " - " + ex.getMessage());
        return errors;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(HandlerMethodValidationException ex) {
        Map<String, String> errorMessages = new HashMap<>();
        ex.visitResults(new HandlerMethodValidationException.Visitor() {

            @Override
            public void requestParam(RequestParam requestParam, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void requestPart(RequestPart requestPart, ParameterErrors errors) {
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void requestHeader(RequestHeader requestHeader, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void cookieValue(CookieValue cookieValue, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void matrixVariable(MatrixVariable matrixVariable, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void modelAttribute(ModelAttribute modelAttribute, ParameterErrors errors) {
                // Iterate over List<ObjectError> from getAllErrors()
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void pathVariable(PathVariable pathVariable, ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            @Override
            public void requestBody(RequestBody requestBody, ParameterErrors errors) {
                for (ObjectError error : errors.getAllErrors()) {
                    errorMessages.put(error.getObjectName(), error.getDefaultMessage());
                }
            }

            @Override
            public void other(ParameterValidationResult result) {
                addMessagesFromResult(result);
            }

            private void addMessagesFromResult(ParameterValidationResult result) {
                List<? extends MessageSourceResolvable> errors = result.getResolvableErrors();
                for (MessageSourceResolvable error : errors) {
                    errorMessages.put(error.getClass().getName(), error.getDefaultMessage());
                }
            }
        });
        return errorMessages;
    }

    // Handle MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
//        errors.put(ex.getObjectName(), ex.getTitleMessageCode());

        ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(err -> err.unwrap(ConstraintViolation.class) != null)
                .map(err -> err.unwrap(ConstraintViolation.class))
                .collect(Collectors.toList()).forEach(cv -> {
                    Optional<Class<? extends Payload>> payloadClass = cv.getConstraintDescriptor().getPayload().stream().findFirst();
                    String payloadClassname = payloadClass.map(Class::getSimpleName).orElse(null);
                    // Combine the payload class name with the message
                    String errorMessage = payloadClassname != null ?
                            payloadClassname + ": " + cv.getMessage() : cv.getMessage();
                    errors.put(cv.getPropertyPath().toString(), errorMessage);
                });
        return errors;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            // Get the payload class
            Optional<Class<? extends Payload>> payloadClass = cv.getConstraintDescriptor().getPayload().stream().findFirst();
            String payloadClassname = payloadClass.map(Class::getSimpleName).orElse(null);
            // Combine the payload class name with the message
            String errorMessage = payloadClassname != null ?
                    payloadClassname + ": " + cv.getMessage() : cv.getMessage();
            errors.put(cv.getPropertyPath().toString(), errorMessage);
        });
        return errors;
    }
}
