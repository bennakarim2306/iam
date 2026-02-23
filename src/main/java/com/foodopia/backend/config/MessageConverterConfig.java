package com.foodopia.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration to allow Jackson to handle application/octet-stream
 * This is a workaround for clients (like React Native/Expo) that send
 * JSON parts in multipart/form-data with incorrect content-type
 */
@Configuration
public class MessageConverterConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        for (org.springframework.http.converter.HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                
                // Get current supported media types
                List<MediaType> supportedMediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
                
                // Add application/octet-stream as a supported media type for JSON deserialization
                // This allows JSON parts sent as octet-stream to be deserialized properly
                supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                
                jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
            }
        }
    }
}
