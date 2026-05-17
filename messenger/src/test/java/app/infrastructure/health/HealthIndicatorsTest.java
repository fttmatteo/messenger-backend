package app.infrastructure.health;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@ExtendWith(MockitoExtension.class)
class HealthIndicatorsTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;
    @Mock
    private RedisConnection redisConnection;
    @Mock
    private GeoApiContext geoApiContext;
    @InjectMocks
    private RedisHealthIndicator redisHealthIndicator;
    @InjectMocks
    private GoogleMapsHealthIndicator googleMapsHealthIndicator;

    @Test
    void redisHealthShouldBeUpWhenPingReturnsPong() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Health health = redisHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("PONG", health.getDetails().get("response"));
    }

    @Test
    void redisHealthShouldBeDownOnException() {
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis down"));

        Health health = redisHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("Redis down"));
    }

    @Test
    void googleMapsHealthShouldBeUpWhenApiResponds() throws Exception {
        try (MockedStatic<GeocodingApi> mockedGeocodingApi = mockStatic(GeocodingApi.class)) {
            GeocodingResult[] results = new GeocodingResult[] { new GeocodingResult() };
            com.google.maps.GeocodingApiRequest pendingRequest = mock(com.google.maps.GeocodingApiRequest.class);

            mockedGeocodingApi.when(() -> GeocodingApi.geocode(any(), anyString()))
                    .thenReturn(pendingRequest);
            when(pendingRequest.await()).thenReturn(results);

            Health health = googleMapsHealthIndicator.health();

            assertEquals(Status.UP, health.getStatus());
            assertEquals("Connected", health.getDetails().get("status"));
        }
    }
}
