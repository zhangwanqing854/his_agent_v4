package com.hospital.handover.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

public class EntityFieldMapper {

    private static final Logger logger = LoggerFactory.getLogger(EntityFieldMapper.class);

    public static <T> void mapFields(T entity, Map<String, Object> data) {
        if (entity == null || data == null || data.isEmpty()) {
            return;
        }

        Class<?> entityClass = entity.getClass();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            try {
                String setterName = "set" + capitalize(fieldName);
                Method setter = findSetter(entityClass, setterName, value.getClass());

                if (setter != null) {
                    Object convertedValue = convertValue(value, setter.getParameterTypes()[0]);
                    if (convertedValue != null) {
                        setter.invoke(entity, convertedValue);
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not set field {} on {}: {}", fieldName, entityClass.getSimpleName(), e.getMessage());
            }
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                result.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return result.toString();
    }

    private static Method findSetter(Class<?> entityClass, String setterName, Class<?> valueType) {
        Method[] methods = entityClass.getMethods();

        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (isAssignable(valueType, paramType)) {
                    return method;
                }
            }
        }

        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }

        return null;
    }

    private static boolean isAssignable(Class<?> from, Class<?> to) {
        if (to.isAssignableFrom(from)) {
            return true;
        }

        if (from == String.class) {
            return to == String.class || to == Long.class || to == long.class ||
                   to == Integer.class || to == int.class;
        }

        return false;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (value instanceof String) {
            String strValue = (String) value;

            if (targetType == Long.class || targetType == long.class) {
                try {
                    return Long.parseLong(strValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            if (targetType == Integer.class || targetType == int.class) {
                try {
                    return Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        if (targetType == LocalDateTime.class && value instanceof String) {
            return FieldMappingProcessor.parseDateTime((String) value);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof String) {
                String strValue = (String) value;
                return "Y".equalsIgnoreCase(strValue) || "1".equals(strValue) || 
                       "true".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue);
            }
            if (value instanceof Boolean) {
                return value;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
        }

        if (targetType == String.class) {
            return value.toString();
        }

        return null;
    }
}