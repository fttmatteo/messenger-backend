package app.domain.ports;

import java.util.List;

import app.domain.model.Signature;

public interface SignaturePort {
    Signature save(Signature signature);

    Signature update(Signature signature);

    void deleteById(Long idSignature);

    Signature findById(Long idSignature);

    List<Signature> findAll();

    List<Signature> findByServiceDeliveryId(Long serviceDeliveryId);
}