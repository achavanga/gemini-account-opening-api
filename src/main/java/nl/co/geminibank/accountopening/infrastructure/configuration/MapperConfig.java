package nl.co.geminibank.accountopening.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Optional;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Add a custom converter for Optional fields
        modelMapper.addConverter((Converter<Optional<Double>, Double>) context -> {
            return context.getSource().orElse(null); // Return null if empty
        });

        modelMapper.addConverter((Converter<Optional<String>, String>) context -> {
            return context.getSource().orElse(null); // Return null if empty
        });

        modelMapper.addConverter((Converter<Optional<Boolean>, Boolean>) context -> {
            return context.getSource().orElse(null); // Return null if empty
        });

        modelMapper.addConverter((Converter<String, Long>) context -> {
            try {
                return Long.parseLong(context.getSource()); // Try parsing the string as Long
            } catch (NumberFormatException e) {
                return null; // Return null or handle the exception as needed
            }
        });

        return modelMapper;
    }

    @Bean
    @Primary
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }
}
