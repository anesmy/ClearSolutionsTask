package com.nesmy.clearsolutionstask.web.service;

import com.nesmy.clearsolutionstask.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface UserService {

    User findById(Long id);

    User save(User user);

    boolean deleteById(Long id);

    List<User> findByBirthDateBetween(LocalDate startBirthDate, LocalDate endBirthDate);

    User update(Long id, User user);

    User patch(Long id, User user);
}
