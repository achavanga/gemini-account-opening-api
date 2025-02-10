package nl.co.geminibank.accountopening.boundary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.co.geminibank.accountopening.control.service.AccountOpeningService;
import nl.co.geminibank.accountopening.model.AddressDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestResponseDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestStartDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountOpeningController.class)
class AccountOpeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountOpeningService accountOpeningService;

     private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    @Test
    void shouldStartAccountRegistration() throws Exception {
        AddressDTO addressDTO = new AddressDTO("Street 1","2","9499 CV","City");
        // Creating and setting up the CustomerRequestStartDTO
        CustomerRequestStartDTO customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setName("John Doe");
        customerRequestStartDTO.setAddress(addressDTO); // Set the AddressDTO in the request
        customerRequestStartDTO.setDateOfBirth(LocalDate.of(1990, 5, 20));

        CustomerRequestResponseDTO responseDTO = new CustomerRequestResponseDTO();

        when(accountOpeningService.startAccountRegistration(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/v1/api/customers/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestStartDTO)))
                .andExpect(status().isCreated())  // HTTP Status 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(accountOpeningService, times(1)).startAccountRegistration(any());
    }

    @Test
    void shouldPauseAccountRegistration() throws Exception {
        String requestId = "12345";
        CustomerRequestResponseDTO responseDTO = new CustomerRequestResponseDTO();

        when(accountOpeningService.pauseAccountRegistration(requestId)).thenReturn(responseDTO);

        mockMvc.perform(put("/v1/api/customers/{requestId}/pause", requestId))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(accountOpeningService, times(1)).pauseAccountRegistration(requestId);
    }
    @Test
    void shouldResumeAccountRegistration() throws Exception {
        String requestId = "12345";
        AddressDTO addressDTO = new AddressDTO("Street 1","2","9499 CV","City");
        // Creating and setting up the CustomerRequestStartDTO
        CustomerRequestStartDTO customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setName("John Doe");
        customerRequestStartDTO.setAddress(addressDTO); // Set the AddressDTO in the request
        customerRequestStartDTO.setDateOfBirth(LocalDate.of(1990, 5, 20));
        CustomerRequestUpdateDTO responseDTO = new CustomerRequestUpdateDTO();

        when(accountOpeningService.resumeAccountRegistration(eq(requestId), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/v1/api/customers/{requestId}/resume", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestStartDTO)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(accountOpeningService, times(1)).resumeAccountRegistration(eq(requestId), any());
    }
    //  Fetching customer request details
    @Test
    void shouldGetCustomerRequestDetails_whenRequestExists() throws Exception {
        String requestId = "12345";

        mockMvc.perform(get("/v1/api/customers/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }
}