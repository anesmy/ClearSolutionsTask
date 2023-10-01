package com.nesmy.clearsolutionstask.unitTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nesmy.clearsolutionstask.dto.DataDTO;
import com.nesmy.clearsolutionstask.entity.User;
import com.nesmy.clearsolutionstask.exceptions.ApiError;
import com.nesmy.clearsolutionstask.exceptions.ApiException;
import com.nesmy.clearsolutionstask.utils.StringCodeConstants;
import com.nesmy.clearsolutionstask.web.controller.UserController;
import com.nesmy.clearsolutionstask.web.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    Long userId = 1L;
    private final User defaultUser = new User(userId, "andrii@gmail.com", "Andrii", "Muts", LocalDate.of(1998, 9, 9), "Lviv", "+380977020222");
    private final User invalidDataUser = new User("andrii", "Andrii", "Muts", LocalDate.now().plusDays(10), "Lviv", "+380977020222");
    private final User invalidDataUserPartialUpdate = new User(userId, "andrii", null, null, LocalDate.now().plusDays(10), null, null);
    private final User nullValuesUser = new User(null, null, null, null, null, null);

    User existingUser = new User(userId, "maksym@gmail.com", "Max", "Muts", LocalDate.of(1998, 9, 9), "Lviv", "+380977020222");
    User userAfterPartialUpdate = new User(userId, "updated@gmail.com", "Updated", existingUser.getLastName(), existingUser.getBirthDate(), existingUser.getAddress(), existingUser.getPhoneNumber());
    User updatedUser = new User(userId, "updated@gmail.com", "Updated", "Updated", LocalDate.of(1998, 9, 9), "Updated Address", "+1234567890");
    User userForPartialUpdate = new User(userId, "updated@gmail.com", "Updated", null, null, null, null);
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    public void testSaveWithValidData() throws Exception {
        when(userService.save(defaultUser)).thenReturn(defaultUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(defaultUser))))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("data.email", is(defaultUser.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.firstName", is(defaultUser.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.lastName", is(defaultUser.getLastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.birthDate", is(defaultUser.getBirthDate().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.address", is(defaultUser.getAddress())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.phoneNumber", is(defaultUser.getPhoneNumber())))
                .andReturn();
    }

    @Test
    public void testSaveWithInvalidData() throws Exception {
        when(userService.save(invalidDataUser)).thenReturn(invalidDataUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(invalidDataUser))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testSaveWithNullValues() throws Exception {
        when(userService.save(nullValuesUser)).thenReturn(nullValuesUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(nullValuesUser))))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    public void testUpdateWithValidInput() throws Exception {
        when(userService.update(userId, updatedUser)).thenReturn(updatedUser);

        MvcResult mvcResult = mvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(updatedUser))))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = mvcResult.getResponse().getContentAsString();
        DataDTO<User> responseDTO = objectMapper.readValue(responseJson, new TypeReference<>() {
        });

        assertThat(responseDTO.getData()).isEqualTo(updatedUser);
        verify(userService, times(1)).update(userId, updatedUser);
    }

    @Test
    public void testUpdateWithNotFoundUser() throws Exception {
        when(userService.update(userId, updatedUser)).thenThrow(new ApiException(HttpStatus.NOT_FOUND,
                List.of(new ApiError("userId", StringCodeConstants.NOT_FOUND))));


        mvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(updatedUser))))
                .andExpect(status().isNotFound());

        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        when(userService.save(invalidDataUser)).thenReturn(invalidDataUser);

        mvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(invalidDataUser))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUpdatePartiallyWithValidData() throws Exception {
        when(userService.patch(userId, userForPartialUpdate)).thenReturn(userAfterPartialUpdate);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(userForPartialUpdate))))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("data.email", is(userAfterPartialUpdate.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.firstName", is(userAfterPartialUpdate.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.lastName", is(userAfterPartialUpdate.getLastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("data.birthDate", is(userAfterPartialUpdate.getBirthDate().toString())));
    }

    @Test
    public void testUpdatePartiallyWithNotFoundUser() throws Exception {
        when(userService.patch(userId, userForPartialUpdate)).thenThrow(new ApiException(HttpStatus.NOT_FOUND,
                List.of(new ApiError("userId", StringCodeConstants.NOT_FOUND))));

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(userForPartialUpdate))))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).patch(userId, userForPartialUpdate);
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testUpdatePartiallyWithInvalidData() throws Exception {
        when(userService.patch(userId, invalidDataUserPartialUpdate)).thenThrow(new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                List.of(new ApiError("", ""))));

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DataDTO<>(invalidDataUserPartialUpdate))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testDeleteExistingUser() throws Exception {
        when(userService.deleteById(userId)).thenReturn(true);

        mvc.perform(delete("/users/{id}", userId))
                .andExpect(content().string("User was successfully deleted."))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteNonExistingUser() throws Exception {
        when(userService.deleteById(userId)).thenReturn(false);

        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindByBirthDateBetweenWithValidDates() throws Exception {

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2005, 12, 31);
        List<User> users = new ArrayList<>();

        when(userService.findByBirthDateBetween(startDate, endDate)).thenReturn(users);

        mvc.perform(get("/users")
                        .param("startBirthDate", startDate.toString())
                        .param("endBirthDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("data", hasSize(users.size())));
    }

    @Test
    public void testFindByBirthDateBetweenWithInvalidDates() throws Exception {

        LocalDate startDate = LocalDate.of(2002, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 12, 31);

        when(userService.findByBirthDateBetween(startDate, endDate)).thenThrow(new ApiException(HttpStatus.BAD_REQUEST,
                List.of(new ApiError("", StringCodeConstants.NO_DATA_SUBMITTED))));

        mvc.perform(get("/users")
                        .param("startBirthDate", startDate.toString())
                        .param("endBirthDate", endDate.toString()))
                .andExpect(status().isBadRequest());
    }
}
