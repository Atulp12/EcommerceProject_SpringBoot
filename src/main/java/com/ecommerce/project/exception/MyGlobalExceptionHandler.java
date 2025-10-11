package com.ecommerce.project.exception;


import com.ecommerce.project.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String,String>> myMethodArgumentNotValid(MethodArgumentNotValidException e){
            Map<String,String> mpp = new HashMap<>();
            e.getBindingResult().getAllErrors().forEach(err->{
                String fieldName = ((FieldError) err).getField();
                String message = err.getDefaultMessage();
                mpp.put(fieldName,message);
            });

            return new ResponseEntity<>(mpp, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException e){
            String message = e.getMessage();
            ApiResponse apiResponse = new ApiResponse(message,false);
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ApiResponse> apiException(ApiException e){
            String message = e.getMessage();
            ApiResponse apiResponse = new ApiResponse(message,false);
            return new ResponseEntity<ApiResponse>(apiResponse,HttpStatus.BAD_REQUEST);
        }
}
