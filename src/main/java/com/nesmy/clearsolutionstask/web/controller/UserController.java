package com.nesmy.clearsolutionstask.web.controller;

import com.nesmy.clearsolutionstask.dto.DataDTO;
import com.nesmy.clearsolutionstask.entity.User;
import com.nesmy.clearsolutionstask.exceptions.ApiError;
import com.nesmy.clearsolutionstask.exceptions.ApiException;
import com.nesmy.clearsolutionstask.utils.StringCodeConstants;
import com.nesmy.clearsolutionstask.web.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    public ResponseEntity<DataDTO<User>> create(@RequestBody DataDTO<@Valid User> body) {
        User user = body.getData();
        User newUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DataDTO<>(newUser));
    }


    @PutMapping("/{id}")
    public ResponseEntity<DataDTO<User>> update(@PathVariable Long id, @RequestBody DataDTO<@Valid User> body) {
        User updatedUser = userService.update(id, body.getData());
        return ResponseEntity.status(HttpStatus.OK).body(new DataDTO<>(updatedUser));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DataDTO<User>> updatePartially(@PathVariable Long id, @RequestBody DataDTO<User> body) {
        User updatedUser = userService.patch(id, body.getData());
        return ResponseEntity.status(HttpStatus.OK).body(new DataDTO<>(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boolean isDeleted = userService.deleteById(id);
        if (isDeleted) {
            return new ResponseEntity<>("User was successfully deleted.", HttpStatus.OK);
        } else {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    List.of(new ApiError("userId", StringCodeConstants.NOT_FOUND)));
        }
    }

    @GetMapping("")
    public ResponseEntity<DataDTO<List<User>>> findByBirthDateBetween(
            @RequestParam LocalDate startBirthDate,
            @RequestParam LocalDate endBirthDate) {

        List<User> users = userService.findByBirthDateBetween(startBirthDate, endBirthDate);
        return ResponseEntity.status(HttpStatus.OK).body(new DataDTO<>(users));
    }
}
