package app.infrastructure.persistence.mapper;

import app.domain.model.Signature;
import app.infrastructure.persistence.entities.SignatureEntity;

public class SignatureMapper {
    public static SignatureEntity toEntity(Signature domain) {
        if (domain == null)
            return null;
        SignatureEntity entity = new SignatureEntity();
        entity.setIdSignature(domain.getIdSignature());
        entity.setSignaturePath(domain.getSignaturePath());
        entity.setUploadDate(domain.getUploadDate());
        entity.setEmployee(EmployeeMapper.toEntity(domain.getEmployee()));
        return entity;
    }

    public static Signature toDomain(SignatureEntity entity) {
        if (entity == null)
            return null;
        Signature signature = new Signature();
        signature.setIdSignature(entity.getIdSignature());
        signature.setSignaturePath(entity.getSignaturePath());
        signature.setUploadDate(entity.getUploadDate());
        signature.setEmployee(EmployeeMapper.toDomain(entity.getEmployee()));
        return signature;
    }
}