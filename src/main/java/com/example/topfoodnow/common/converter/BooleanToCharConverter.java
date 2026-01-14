package com.example.topfoodnow.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BooleanToCharConverter implements AttributeConverter<Boolean, String> {

    // 將 Java 實體屬性轉換為資料庫欄位 (boolean -> String "Y" / "N")
    @Override
    public String convertToDatabaseColumn(Boolean aBoolean) {
        return (aBoolean == null) ? null : (aBoolean ? "Y" : "N");
    }

    // 將資料庫欄位值轉換為 Java 實體屬性 (String "Y" / "N" -> boolean)
    @Override
    public Boolean convertToEntityAttribute(String s) {
        return s == null ? null : "Y".equalsIgnoreCase(s);
    }
}
