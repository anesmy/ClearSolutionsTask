package com.nesmy.clearsolutionstask.web.service;

import com.nesmy.clearsolutionstask.repository.UserRepository;
import com.nesmy.clearsolutionstask.entity.User;
import com.nesmy.clearsolutionstask.exceptions.ApiError;
import com.nesmy.clearsolutionstask.exceptions.ApiException;
import com.nesmy.clearsolutionstask.utils.StringCodeConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Value("${user.min-age}")
    private int minAge;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User save(User user) {
        ApiException apiException = validateUserIsNull(user);
        if (apiException == null) {
            // requirement 2.1 ...users who are more than [18] years old. I would use more than or equal to 18
            if (calculateAge(user.getBirthDate()) > minAge)
                return userRepository.save(user);
            else {
                apiException = new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        List.of(new ApiError("birthDate", StringCodeConstants.BIRTHDATE_IS_LESS_THAN_18)));
            }
        }
        throw apiException;
    }

    private int calculateAge(LocalDate birthDate) {
        Period period = Period.between(birthDate, LocalDate.now());
        return period.getYears();
    }

    @Override
    public boolean deleteById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<User> findByBirthDateBetween(LocalDate startBirthDate, LocalDate endBirthDate) {
        if (startBirthDate.isBefore(endBirthDate)) {
            return userRepository.findByBirthDateBetween(startBirthDate, endBirthDate);
        } else
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ApiError("", StringCodeConstants.START_DATE_IS_NOT_BEFORE_END_DATE)));
    }

    public User update(Long id, User user) {
        ApiException apiException = validateUserIsNull(user);
        if (apiException == null) {
            if (Objects.equals(id, user.getUserId())) {
                if (findById(id) != null) {
                    return userRepository.save(user);
                } else
                    apiException = new ApiException(HttpStatus.NOT_FOUND,
                        List.of(new ApiError("userId", StringCodeConstants.NOT_FOUND)));

            } else {
                apiException = new ApiException(HttpStatus.BAD_REQUEST,
                        List.of(new ApiError("userId", StringCodeConstants.KEY_FIELD_PARAMETERS_MISMATCH)));
            }
        }
        throw apiException;
    }

    @Override
    public User patch(Long id, User user) {
        ApiException apiException = validateUserIsNull(user);
        if (apiException == null) {

            if (Objects.equals(id, user.getUserId())) {
                User userToUpdate = findById(id);
                if (userToUpdate != null) {
                    updateFieldsForPatch(user, userToUpdate);
                    validateUserForPatch(userToUpdate);
                    return userRepository.save(userToUpdate);
                } else
                    apiException = new ApiException(HttpStatus.NOT_FOUND,
                            List.of(new ApiError("userId", StringCodeConstants.NOT_FOUND)));

            } else {
                apiException = new ApiException(HttpStatus.BAD_REQUEST,
                        List.of(new ApiError("userId", StringCodeConstants.KEY_FIELD_PARAMETERS_MISMATCH)));
            }

        }
        throw apiException;
    }

    private void updateFieldsForPatch(User source, User target) {
        String updatedEmail = source.getEmail();
        if (isNotEmpty(updatedEmail)) target.setEmail(updatedEmail);

        String updatedFirstName = source.getFirstName();
        if (isNotEmpty(updatedFirstName)) target.setFirstName(updatedFirstName);

        String updatedLastName = source.getLastName();
        if (isNotEmpty(updatedLastName)) target.setLastName(updatedLastName);

        LocalDate updatedBirthDate = source.getBirthDate();
        if (isNotEmpty(updatedBirthDate)) target.setBirthDate(updatedBirthDate);

        String updatedAddress = source.getAddress();
        if (isNotEmpty(updatedAddress)) target.setAddress(updatedAddress);

        String updatedPhoneNumber = source.getPhoneNumber();
        if (isNotEmpty(updatedPhoneNumber)) target.setPhoneNumber(updatedPhoneNumber);
    }

    private void validateUserForPatch(User user) throws ConstraintViolationException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty())
            throw new ConstraintViolationException(violations);
    }

    private <T> boolean isNotEmpty(T field) {
        return field != null && !field.equals("");
    }

    private ApiException validateUserIsNull(User user) {
        if (user == null
                || (user.getUserId() == null && user.getEmail() == null && user.getFirstName() == null
                    && user.getLastName() == null && user.getBirthDate() == null
                    && user.getAddress() == null && user.getPhoneNumber() == null)) {
            return new ApiException(HttpStatus.BAD_REQUEST,
                    List.of(new ApiError("", StringCodeConstants.NO_DATA_SUBMITTED)));
        } else
            return null;
    }
}
