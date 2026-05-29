package app.adapter.out.config;

import app.domain.ports.AppConfigPort;
import app.infrastructure.config.WhatsAppConfig;
import org.springframework.stereotype.Component;

@Component
public class AppConfigAdapter implements AppConfigPort {

    private final WhatsAppConfig config;

    public AppConfigAdapter(WhatsAppConfig config) {
        this.config = config;
    }

    @Override
    public String getFrontendUrl() {
        return config.getFrontendUrl();
    }
}
