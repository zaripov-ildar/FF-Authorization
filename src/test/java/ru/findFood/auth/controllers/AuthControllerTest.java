package ru.findFood.auth.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.findFood.auth.dtos.AuthenticationRequest;
import ru.findFood.auth.dtos.AuthenticationResponse;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.entities.Role;
import ru.findFood.auth.repositories.AuthUserRepository;
import ru.findFood.auth.repositories.RoleRepository;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    private final String EXISTING_EMAIL = "employee@mail.com";
    private final String NEW_EMAIL = "manager@mail.com";
    private final String CORRECT_PASSWORD = "correct password";
    private final String WRONG_PASSWORD = "wrong password";


    @Value("${security_params.clientRole}")
    private String clientRole;

    @Value("${security_params.restaurantRole}")
    private String restaurantRole;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthUserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    private final String authUrl = "/api/v1/authenticate";
    private final String userRegUrl = "/api/v1/register/user";
    private final String restaurantRegUrl = "/api/v1/register/restaurant";
    private final String refreshUrl = "/api/v1/refreshJWT";

    @BeforeEach
    public void setUp() {
        initPersonRepository();
    }

    @Test
    void authenticateExistingAccount() throws Exception {
        checkAuthRequest(authUrl, EXISTING_EMAIL, CORRECT_PASSWORD, status().isOk());
        checkAuthRequest(authUrl, NEW_EMAIL, WRONG_PASSWORD, status().isForbidden());
    }

    @Test
    void authenticateNotExistingAccount() throws Exception {
        checkAuthRequest(authUrl, NEW_EMAIL, CORRECT_PASSWORD, status().isForbidden());
    }

    @Test
    void authenticateWithWrongPassword() throws Exception {
        checkAuthRequest(authUrl, EXISTING_EMAIL, WRONG_PASSWORD, status().isForbidden());
    }

    @Test
    void registerWithExistingEmail() throws Exception {
        checkAuthRequest(userRegUrl, EXISTING_EMAIL, CORRECT_PASSWORD, status().isBadRequest());
        checkAuthRequest(restaurantRegUrl, EXISTING_EMAIL, CORRECT_PASSWORD, status().isBadRequest());
    }

    @Test
    void registerWithNotExistingEmail() throws Exception {
        checkAuthRequest(userRegUrl, NEW_EMAIL, CORRECT_PASSWORD, status().isOk());
        checkAuthRequest(restaurantRegUrl, NEW_EMAIL, CORRECT_PASSWORD, status().isOk());
    }

    @Test
    void refreshGoodJWT() throws Exception {
        //given
        AuthenticationResponse response = getAuthenticationResponse();

        //when
        MvcResult mvcResult = mockMvc.perform(
                        post(refreshUrl)
                                .header("Authorization", "Bearer " + response.refreshToken())
                )
                .andExpect(status().isOk())
                .andReturn();
        response = getAuthenticationResponse(mvcResult);

        //then
        assertNotNull(response);
        assertFalse(response.refreshToken().isEmpty());
    }

    @Test
    void refreshExpiredJWT() throws Exception {
        String EXPIRED_JWT = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXBsb3llZUBtYWlsLmNvbSIsImlhdCI6MTY4MjU4Mzc3MiwiZXhwIjoxNjgyNTgzODMyfQ.D5vKxumWS_en1SAirbzAs-POA2kGK47nnf4NzNQ_-Q0";
        checkRefreshToken(EXPIRED_JWT, status().isUnauthorized());
    }

    @Test
    void refreshChangedJWT() throws Exception {
        String FAKED_JWT = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXBsb3llZUBtYWlsLmNvbSIsImlhdCI6MTY4MjU4MjE0MCwiZXhwIjoxNjg0MTg2OTQwfQ.i-VyiDE7Y65bdrla2OCZptj4BVNOMpz8a_gFxF66qJ0";
        checkRefreshToken(FAKED_JWT, status().isUnauthorized());
    }

    private void checkRefreshToken(String token, ResultMatcher matcher) throws Exception {
        mockMvc.perform(
                        post(refreshUrl)
                                .header("Authorization", token)
                )
                .andExpect(matcher);
    }

    private AuthenticationResponse getAuthenticationResponse() throws Exception {
        MvcResult result = checkAuthRequest(authUrl, "employee@mail.com", "correct password", status().isOk());
        return getAuthenticationResponse(result);
    }

    private static AuthenticationResponse getAuthenticationResponse(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        String stringResult = result.getResponse().getContentAsString();
        ObjectMapper om = new ObjectMapper();
        return om.readValue(stringResult, AuthenticationResponse.class);
    }

    private MvcResult checkAuthRequest(String url, String email, String password, ResultMatcher resultMatcher) throws Exception {
        initRoleRepository();
        ObjectMapper obj = new ObjectMapper();
        AuthenticationRequest authRequest = new AuthenticationRequest(email, password);
        String request = obj.writeValueAsString(authRequest);
        return mockMvc
                .perform(
                        post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(resultMatcher)
                .andReturn();
    }

    private void initPersonRepository() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        List<Role> roles = List.of(new Role(clientRole), new Role(restaurantRole));
        AuthUser authUser = AuthUser
                .builder()
                .email("employee@mail.com")
                .password(encoder.encode("correct password"))
                .roles(roles)
                .build();
        given(userRepository.findByEmail(authUser.getEmail())).willReturn(Optional.of(authUser));
        given(userRepository.existsByEmail(authUser.getEmail())).willReturn(true);
    }

    private void initRoleRepository() {
        given(roleRepository.findByTitle(clientRole)).willReturn(Optional.of(new Role(clientRole)));
        given(roleRepository.findByTitle(restaurantRole)).willReturn(Optional.of(new Role(restaurantRole)));
    }
}