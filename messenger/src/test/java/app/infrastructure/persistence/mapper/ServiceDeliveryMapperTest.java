package app.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDeliveryMapper Unit Tests")
class ServiceDeliveryMapperTest {

    @Mock
    private PlateMapper plateMapper;
    @Mock
    private DealershipMapper dealershipMapper;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private ServiceDeliveryMapper serviceDeliveryMapper;

    private ServiceDelivery service;
    private Signature sharedSignature;

    @BeforeEach
    void setUp() {
        sharedSignature = new Signature();
        sharedSignature.setIdSignature(1L);
        sharedSignature.setSignaturePath("path/to/signature.png");

        service = new ServiceDelivery();
        service.setIdServiceDelivery(100L);
        service.setPlate(new Plate());
        service.setSignature(sharedSignature);

        StatusHistory history = new StatusHistory();
        history.setIdStatusHistory(200L);
        history.setSignature(sharedSignature);

        service.setHistory(Collections.singletonList(history));

        when(plateMapper.toEntity(any())).thenReturn(new PlateEntity());
    }

    @Test
    @DisplayName("Debe mapear la misma instancia de SignatureEntity cuando el objeto de dominio es el mismo")
    /**
     * Verifica que la misma instancia de SignatureEntity se mapee cuando el objeto
     * de dominio es el mismo.
     */
    void shouldMapSameSignatureInstanceWhenDomainSignatureIsShared() {
        ServiceDeliveryEntity entity = serviceDeliveryMapper.toEntity(service);

        assertNotNull(entity.getSignature());
        assertNotNull(entity.getHistory().get(0).getSignature());

        assertSame(entity.getSignature(), entity.getHistory().get(0).getSignature(),
                "SignatureEntity instances should be the same to avoid Hibernate merge issues");

        assertSame(1L, entity.getSignature().getIdSignature());
    }
}
