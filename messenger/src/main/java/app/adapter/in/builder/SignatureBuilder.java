package app.adapter.in.builder;

import app.domain.model.Signature;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SignatureBuilder {

    public Signature build(String signatureUrl) {
        if (signatureUrl == null || signatureUrl.trim().isEmpty()) {
            return null;
        }
        Signature signature = new Signature();
        signature.setSignaturePath(signatureUrl);
        signature.setUploadDate(LocalDateTime.now());
        return signature;
    }
}