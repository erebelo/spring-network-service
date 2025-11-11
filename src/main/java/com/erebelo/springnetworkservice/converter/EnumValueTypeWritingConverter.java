package com.erebelo.springnetworkservice.converter;

import com.erebelo.springnetworkservice.domain.enumeration.type.EnumValueType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EnumValueTypeWritingConverter implements Converter<EnumValueType, String> {

    @Override
    public String convert(EnumValueType source) {
        return source.getValue();
    }
}
