package com.example.assignment.tools;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class InternationalizationUtil {

    private InternationalizationUtil() {
    }

    /**
     * Retrieve the message for the given Locale and the given key
     *
     * @param messageSource used for resolving messages
     * @param key           key of the message
     * @return the corresponding message
     */
    public static String getMessageByKey(MessageSource messageSource, String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
