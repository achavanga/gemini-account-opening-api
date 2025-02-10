package nl.co.geminibank.accountopening.boundary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.co.geminibank.accountopening.control.exception.ValidationException;
import nl.co.geminibank.accountopening.control.service.ValidationService;
import nl.co.geminibank.accountopening.model.ApiCustomersValidatePostRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ValidationController.class)
class ValidationControllerTest {
    private static final String EMAIL_FIELD = "email";
    private static final String EMAIL_VALUE = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationService validationService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    @Test
    void shouldValidateFieldSuccessfully() throws Exception {
        ApiCustomersValidatePostRequestDTO requestDTO = new ApiCustomersValidatePostRequestDTO();
        requestDTO.setField(Optional.of(EMAIL_FIELD));
        requestDTO.setValue(Optional.of(EMAIL_VALUE));

        mockMvc.perform(post("/v1/api/customers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("The email is valid."));

        verify(validationService).validateField(EMAIL_FIELD, EMAIL_VALUE);
    }

    @Test
    void shouldReturnBadRequestWhenBothFieldAndValueAreMissing() throws Exception {
        ApiCustomersValidatePostRequestDTO requestDTO = new ApiCustomersValidatePostRequestDTO();

        mockMvc.perform(post("/v1/api/customers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenValueIsMissing() throws Exception {
        ApiCustomersValidatePostRequestDTO requestDTO = new ApiCustomersValidatePostRequestDTO();
        requestDTO.setField(Optional.of(EMAIL_FIELD));
        requestDTO.setValue(Optional.empty());

        mockMvc.perform(post("/v1/api/customers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.error").value("Value is required for field: email"));
    }

    @Test
    void shouldReturnBadRequestWhenFieldIsMissing() throws Exception {
        ApiCustomersValidatePostRequestDTO requestDTO = new ApiCustomersValidatePostRequestDTO();
        requestDTO.setField(Optional.empty());
        requestDTO.setValue(Optional.of(EMAIL_VALUE));

        mockMvc.perform(post("/v1/api/customers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.error").value("Field is required"));
    }

    @Test
    void shouldThrowValidationExceptionForInvalidField() throws Exception {
        String invalidField = "unknownField";
        ApiCustomersValidatePostRequestDTO requestDTO = new ApiCustomersValidatePostRequestDTO();
        requestDTO.setField(Optional.of(invalidField));
        requestDTO.setValue(Optional.of("unknown value"));

        doThrow(new ValidationException(invalidField, "Invalid field name: " + invalidField))
                .when(validationService).validateField(eq(invalidField), anyString());

        mockMvc.perform(post("/v1/api/customers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.error").value("Invalid field name: " + invalidField));
    }
}