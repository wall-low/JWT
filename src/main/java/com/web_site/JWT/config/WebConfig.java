package com.web_site.JWT.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Проставляет кодировку в Content-Type для JSON-ответов.
 *
 * По стандарту JSON всегда в UTF-8, поэтому Spring не пишет charset в заголовок.
 * Часть браузеров без явного указания угадывает кодировку неверно, и русский текст
 * в сообщениях об ошибках превращается в кракозябры.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jackson) {
                jackson.setSupportedMediaTypes(
                        List.of(new MediaType("application", "json", StandardCharsets.UTF_8))
                );
            }
        }
    }
}
