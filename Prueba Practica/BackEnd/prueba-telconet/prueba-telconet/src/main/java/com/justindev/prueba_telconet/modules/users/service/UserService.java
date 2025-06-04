package com.justindev.prueba_telconet.modules.users.service;

import com.justindev.prueba_telconet.application.exceptions.ClientException;
import com.justindev.prueba_telconet.application.utils.EmailManager;
import com.justindev.prueba_telconet.modules.users.dto.*;
import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.model.Authorities;
import com.justindev.prueba_telconet.modules.users.model.CodeForgetPassword;
import com.justindev.prueba_telconet.modules.users.model.enums.UserRoles;
import com.justindev.prueba_telconet.modules.users.repository.AuthorityRepository;
import com.justindev.prueba_telconet.modules.users.repository.CodeForgetPasswordRepository;
import com.justindev.prueba_telconet.modules.users.repository.UserRepository;
import com.justindev.prueba_telconet.modules.users.repository.UsersHistoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.passay.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Validated
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final EmailManager emailManager;
    private final Random random = new Random();
    private final CodeForgetPasswordRepository codeForgerPasswordRepository;
    private final UsersHistoryRepository usersHistoryRepository;

    private static final String USER_NOT_FOUND = "User not found";

    public void registerUser(@Valid UserRegisterDto dto) {
        var user = buildUser(dto);
        user.setRoles(Set.of(getAuthority(UserRoles.USER)));
        emailManager.sendEmail(user.getPersonalEmail(), "Welcome to our service", "Hello " + user.getName() + ", your account has been created successfully. Your username is: " + user.getUsername() + ", your platform email is: " + user.getEmail() + ". Additionally your password is your identification number, Please change your password as soon as possible.");
        userRepository.save(user);
    }

    public void registerAdmin(@Valid UserRegisterDto dto) {
        var user = buildUser(dto);
        user.setRoles(Set.of(getAuthority(UserRoles.ADMIN)));
        emailManager.sendEmail(user.getPersonalEmail(), "Welcome to our service", "Hello " + user.getName() + ", your account has been created successfully. Your username is: " + user.getUsername() + ", your platform email is: " + user.getEmail() + ". Additionally your password is your identification number, Please change your password as soon as possible.");
        userRepository.save(user);
    }

    private Authorities getAuthority(UserRoles role) {
        return authorityRepository.findByAuthority(role).orElseThrow(() -> new ClientException("Role not found", HttpStatus.NOT_FOUND));
    }

    private AppUser buildUser(@Valid UserRegisterDto dto) {
        var username = generateUniqueUsername(dto.getName(), dto.getLastname());
        var platformEmail = generateUniqueEmail(dto.getName(), dto.getLastname());
        validateRegisterUser(dto);
        return AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(dto.getIdentification()))
                .email(platformEmail)
                .personalEmail(dto.getPersonalEmail())
                .address(dto.getAddress())
                .name(dto.getName())
                .lastname(dto.getLastname())
                .identification(dto.getIdentification())
                .phone(dto.getPhone())
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
    }

    private void validateRegisterUser(UserRegisterDto dto) {
        if (isValidIdentification(dto.getIdentification())) {
            if (userRepository.existsByIdentification(dto.getIdentification())) {
                throw new ClientException("Identification already exists", HttpStatus.CONFLICT);
            }
        } else {
            throw new ClientException("Identification is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUpdateUser(AppUser user) {
        if (user.getUsername()!= null && userRepository.existsByPersonalEmail(user.getPersonalEmail())) {
            throw new ClientException("Email already exists", HttpStatus.CONFLICT);
        }

        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            throw new ClientException("Username already exists", HttpStatus.CONFLICT);
        }

        if (user.getUsername()!= null && !isValidUsername(user.getUsername())) {
            throw new ClientException("Username is invalid", HttpStatus.CONFLICT);
        }

    }

    private boolean isPasswordInvalid(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 20),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        ));
        RuleResult result = validator.validate(new PasswordData(password));
        return !result.isValid();
    }

    public void updateUser(@Valid UpdateUserDto dto, Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (dto.getUsername()!= null) user.setUsername(dto.getUsername());
        if (dto.getPersonalEmail()!= null) user.setEmail(dto.getPersonalEmail());
        if (dto.getAddress()!= null) user.setAddress(dto.getAddress());
        if (dto.getName()!= null) user.setName(dto.getName());
        if (dto.getLastname()!= null) user.setLastname(dto.getLastname());
        if (dto.getPhone()!= null) user.setPhone(dto.getPhone());
        validateUpdateUser(user);
        userRepository.save(user);
    }

    public void resetPassword(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (user.getIdentification() == null) {
            throw new ClientException("User does not have an identification number to reset the password", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(user.getIdentification()));
        userRepository.save(user);
    }



    public void sendCodeChangePassword(String email) {
        registerRecoveryCode(email);
        emailManager.sendEmail(email, "Change password", "Your code is: " + generateCode() + " this code will expire in 5 minutes");
    }

    public void changePassword(@Valid ChangePasswordDto dto, String code, String email) {
        var user = userRepository.findByPersonalEmailOrEmail(email).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (verifyCode(code, email)){
            verifyPasswords(dto.getNewPassword(), dto.getConfirmPassword());
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
        }
    }

    private void verifyPasswords(String password, String confirmPassword) {
        if (isPasswordInvalid(password)) {
            throw new ClientException("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character", HttpStatus.BAD_REQUEST);
        }
        if (!password.equals(confirmPassword)) {
            throw new ClientException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
    }

    public boolean verifyCode(String code, String email) {
        var verify = codeForgerPasswordRepository.findByCodeAndUserEmail(code, email).orElseThrow(() -> new ClientException("Code not found for this user", HttpStatus.NOT_FOUND));
        if (verify.getGeneratedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new ClientException("Code expired", HttpStatus.BAD_REQUEST);

        }
        return true;
    }


    private void registerRecoveryCode(String email) {
        var user = userRepository.findByPersonalEmailOrEmail(email).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var code = CodeForgetPassword.builder()
                .user(user)
                .generatedAt(LocalDateTime.now())
                .code(generateCode())
                .build();

        codeForgerPasswordRepository.save(code);
    }


    private String generateUniqueEmail(String name, String lastname) {
        // Inicial del primer nombre
        String initial = name.trim().split("\\s+")[0].substring(0, 1);
        // Primer apellido
        String firstLastname = lastname.trim().split("\\s+")[0];
        // Unir, limpiar y minúsculas
        String emailUser = (initial + firstLastname)
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();
        String domain = "@mail.com";
        String email = emailUser + domain;
        int suffix = 1;
        // Verificar unicidad
        while (userRepository.existsByEmail(email)) {
            email = emailUser + suffix + domain;
            suffix++;
        }
        return email;
    }


    //Generar  un codigo random de 6 digitos
    private String generateCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    private String generateUniqueUsername(String name, String lastname) {
        // Eliminar signos y espacios, unir nombre y apellido
        String base = (name + lastname)
                .replaceAll("[^a-zA-Z0-9]", "") // Solo letras y números
                .toLowerCase();

        // Asegurar al menos una mayúscula y un número
        StringBuilder username = new StringBuilder();
        if (base.isEmpty()) base = "user";
        username.append(Character.toUpperCase(base.charAt(0)));
        if (base.length() > 1) username.append(base.substring(1));
        if (!base.matches(".*\\d.*")) username.append("1"); // Añadir número si no hay

        // Ajustar longitud mínima y máxima
        while (username.length() < 8) username.append("x");
        if (username.length() > 20) username.setLength(20);

        String candidate = username.toString();
        int suffix = 2;
        // Verificar unicidad
        while (userRepository.existsByUsername(candidate)) {
            String temp = candidate;
            // Quitar sufijo anterior si lo hay
            temp = temp.replaceAll("\\d+$", "");
            String newCandidate = temp + suffix;
            if (newCandidate.length() > 20) {
                newCandidate = newCandidate.substring(0, 20 - String.valueOf(suffix).length()) + suffix;
            }
            candidate = newCandidate;
            suffix++;
        }
        return candidate;
    }


    public boolean isValidUsername(String username) {
        if (!username.matches("^[a-zA-Z0-9]+$")) return false;
        if (userRepository.existsByUsername(username)) return false;
        if (!username.matches(".*\\d.*")) return false;
        if (!username.matches(".*[A-Z].*")) return false;
        return username.length() >= 8 && username.length() <= 20;
    }

    private boolean isValidIdentification(String identification) {

        if (identification == null || identification.length() != 10) return false;

        if (!identification.matches("\\d{10}")) return false;

        return !identification.matches(".*(\\d)\\1{3,}.*");
    }

    public List<MySessionsDto> getMySessions(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ClientException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return usersHistoryRepository.findAllByAppUserId(user.getId())
                .stream()
                .map(history -> MySessionsDto.builder()
                        .loginTime(history.getLoginDate())
                        .logoutTime(history.getLogoutDate())
                        .active(history.isActive())
                        .build())
                .toList();
    }

    public Page<WhoAmIDto> getAllUsers(String identification,int page, int size) {
        return userRepository.findAllByRoles(identification,Set.of(getAuthority(UserRoles.USER)) ,PageRequest.of(page, size))
                .map(user -> WhoAmIDto.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .identification(user.getIdentification())
                        .personalEmail(user.getPersonalEmail())
                        .lastname(user.getLastname())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .address(user.getAddress())
                        .roles(user.getRoles().stream().map(role -> role.getAuthority().name()).toList())
                        .build());
    }




}
