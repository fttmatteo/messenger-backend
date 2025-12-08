package app.domain.services;

@Service
public class SearchPlate {

    @Autowired
    private PlatePort platePort;

    public Plate findById(Long idPlate) {
        Plate plate = platePort.findById(idPlate);
        if (plate == null) {
            throw new RuntimeException("Placa no encontrada.");
        }
        return plate;
    }

    public Plate findByPlateNumber(String plateNumber) {
        Plate plate = platePort.findByPlateNumber(plateNumber);
        if (plate == null) {
            throw new RuntimeException("Placa no encontrada.");
        }
        return plate;
    }
}
