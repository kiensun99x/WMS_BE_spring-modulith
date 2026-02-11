package com.rk.WMS.batch.service.mail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class MailTemplateReader {

    /**
     * Đọc nội dung mail template và inject dữ liệu động.
     *
     * @param path   đường dẫn file template trong classpath
     * @param params map dữ liệu dùng để replace placeholder trong template
     * @return nội dung mail sau khi đã được render
     */
    public String read(String path, Map<String, Object> params) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                content = content.replace(
                        "{{" + entry.getKey() + "}}",
                        String.valueOf(entry.getValue())
                );
            }

            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read mail template: " + path, e);
        }
    }
}

