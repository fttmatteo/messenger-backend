package app.adapter.in.rest.location;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkLocationsRequest {

    @NotEmpty(message = "La lista de UUIDs no puede estar vacía")
    private List<String> uuids;

    public List<String> getUuids() {
        return uuids;
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }
}
