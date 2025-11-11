package com.erebelo.springnetworkservice.converter;

import com.erebelo.springnetworkservice.domain.enumeration.type.EnumValueType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

@ReadingConverter
public class EnumValueTypeReadingConverter implements ConverterFactory<String, EnumValueType> {

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T extends EnumValueType> Converter<String, T> getConverter(@NonNull final Class<T> targetType) {
        return source -> {
            if (!targetType.isEnum()) {
                throw new IllegalStateException(
                        String.format("The targetType [%s] have to be an enum.", targetType.getSimpleName()));
            }

            try {
                return (T) getEnumType(targetType, source);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        };
    }

    private <T extends EnumValueType> EnumValueType getEnumType(Class<T> targetType, String source)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EnumValueType enumType;

        try {
            Method fromValueMethod = targetType.getDeclaredMethod("fromValue", String.class);
            enumType = (EnumValueType) fromValueMethod.invoke(null, source);
        } catch (NoSuchMethodException e) {
            Method valuesMethod = targetType.getDeclaredMethod("values");

            enumType = Arrays.stream((EnumValueType[]) valuesMethod.invoke(null))
                    .filter(obj -> obj.getValue().equalsIgnoreCase(source)).findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("The value [%s] doesn't match with any instance of the enum [%s]", source,
                                    targetType.getSimpleName())));
        }

        return enumType;
    }
}
