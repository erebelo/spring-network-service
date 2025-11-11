package com.erebelo.springnetworkservice.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UploadSerializerUtil {

    public static byte[] toJsonLines(Object input) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (input instanceof List<?>) {
                for (Object obj : (List<?>) input) {
                    writeJsonLine(obj, baos);
                }
            } else {
                writeJsonLine(input, baos);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error serializing object(s) to JSON lines", e);
        }
    }

    private static void writeJsonLine(Object obj, ByteArrayOutputStream baos) throws IOException {
        byte[] json = ObjectMapperUtil.objectMapper.writeValueAsBytes(obj);
        baos.write(json);
        baos.write('\n');
    }

    public static byte[] toCsvLines(Map<String, String> rows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (Map.Entry<String, String> entry : rows.entrySet()) {
                String key = entry.getKey() != null ? entry.getKey() : "";
                String value = entry.getValue() != null ? entry.getValue() : "";
                String line = key + "," + value + "\n";
                baos.write(line.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error serializing to CSV lines", e);
        }
    }
}
