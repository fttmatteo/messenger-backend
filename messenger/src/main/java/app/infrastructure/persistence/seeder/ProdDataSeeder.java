package app.infrastructure.persistence.seeder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.model.enums.TrackingSource;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.entities.StatusHistoryEntity;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import app.infrastructure.persistence.repository.DealershipRepository;
import app.infrastructure.persistence.repository.EmployeeRepository;
import app.infrastructure.persistence.repository.PlateRepository;
import app.infrastructure.persistence.repository.ServiceDeliveryRepository;
import app.infrastructure.persistence.repository.TrackingHistoryRepository;

/**
 * Seeder para cargar datos de prueba masivos y realistas en entorno de
 * PRODUCCIÓN.
 * Genera datos inventados para demos o pruebas en entorno productivo.
 */
@Component
@Profile("prod")
public class ProdDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProdDataSeeder.class);
    private final Random random = new Random();

    private final DealershipRepository dealershipRepository;
    private final EmployeeRepository employeeRepository;
    private final PlateRepository plateRepository;
    private final ServiceDeliveryRepository serviceDeliveryRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public ProdDataSeeder(DealershipRepository dealershipRepository,
            EmployeeRepository employeeRepository,
            PlateRepository plateRepository,
            ServiceDeliveryRepository serviceDeliveryRepository,
            TrackingHistoryRepository trackingHistoryRepository,
            PasswordEncoder passwordEncoder) {
        this.dealershipRepository = dealershipRepository;
        this.employeeRepository = employeeRepository;
        this.plateRepository = plateRepository;
        this.serviceDeliveryRepository = serviceDeliveryRepository;
        this.trackingHistoryRepository = trackingHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("⚡ Iniciando carga de datos MASIVA de PRODUCCIÓN (Profile: prod)...");

        loadEmployees();
        loadDealerships();
        loadPlates();
        loadServiceDeliveries();

        logger.info("✅ Carga de datos de PRODUCCIÓN completada.");
    }

    private void loadEmployees() {
        logger.info("  -> Generando empleados...");

        // Admin Principal (El que antes creaba AdminSeeder)
        createEmployee(1000000000L, "Administrador", "3000000000", "Admin123!", Role.ADMIN);

        // Admin Soporte (El que ya teníamos)
        createEmployee(1000000002L, "Admin Soporte", "3001234567", "Admin123!", Role.ADMIN);

        // Mensajeros - Nombres realistas
        String[] firstNames = { "Juan", "Carlos", "Luis", "Pedro", "Jorge", "Andres", "Miguel", "Jose", "David",
                "Daniel", "Maria", "Ana", "Laura", "Sofia", "Andrea" };
        String[] lastNames = { "Rodriguez", "Gomez", "Lopez", "Martinez", "Perez", "Garcia", "Sanchez", "Diaz",
                "Torres", "Ramirez", "Hernandez", "Vargas" };

        for (int i = 0; i < 15; i++) {
            String name = firstNames[random.nextInt(firstNames.length)] + " "
                    + lastNames[random.nextInt(lastNames.length)];
            long doc = 200000000L + i;
            String phone = "31" + random.nextInt(9) + String.format("%07d", random.nextInt(10000000));
            createEmployee(doc, name, phone, "Messenger123!", Role.MESSENGER);
        }
    }

    private void createEmployee(Long doc, String name, String phone, String pass, Role role) {
        if (employeeRepository.findByDocument(doc) == null) {
            EmployeeEntity emp = new EmployeeEntity();
            emp.setDocument(doc);
            emp.setFullName(name);
            emp.setPhone(phone);
            emp.setPassword(passwordEncoder.encode(pass));
            emp.setRole(role);
            employeeRepository.save(emp);
        }
    }

    private void loadDealerships() {
        logger.info("  -> Generando concesionarios en Bogotá...");

        // Datos reales de ubicaciones en Bogotá
        createDealership("Toyota Norte", "Calle 170 # 45-12", "6011111111", "Norte", 4.7523, -74.0456);
        createDealership("Chevrolet Centro", "Cl. 34 # 15-20", "6012222222", "Centro", 4.6212, -74.0723);
        createDealership("Mazda Sur", "Av. Boyacá # 23-45", "6013333333", "Sur", 4.5890, -74.1234);
        // Calle 80 se considera Norte para este propósito
        createDealership("Renault Occidente", "Av. Cali # 80-90", "6014444444", "Norte", 4.6987, -74.1012);
        createDealership("Kia Plaza", "Calle 100 # 19-45", "6015555555", "Norte", 4.6850, -74.0550);
        // Salitre es central
        createDealership("Nissan Salitre", "Av. El Dorado # 68-12", "6016666666", "Centro", 4.6550, -74.1100);
        createDealership("Ford Chapinero", "Cra 7 # 60-15", "6017777777", "Centro", 4.6400, -74.0600);
        createDealership("Hyundai Autopista", "Autopista Norte # 128-30", "6018888888", "Norte", 4.7150, -74.0500);
        createDealership("Volkswagen Suba", "Av. Suba # 100-20", "6019999999", "Norte", 4.6950, -74.0750);
        createDealership("BMW Usaquén", "Cra 9 # 120-10", "6010000000", "Norte", 4.7000, -74.0300);
    }

    private void createDealership(String name, String address, String phone, String zone, Double lat, Double lng) {
        if (dealershipRepository.findAll().stream().noneMatch(d -> d.getName().equals(name))) {
            DealershipEntity deal = new DealershipEntity();
            deal.setName(name);
            deal.setAddress(address);
            deal.setPhone(phone);
            deal.setZone(zone);
            deal.setLatitude(lat);
            deal.setLongitude(lng);
            deal.setIsGeolocated(true);
            dealershipRepository.save(deal);
        }
    }

    private void loadPlates() {
        logger.info("  -> Generando parque automotor...");

        for (int i = 0; i < 25; i++) {
            // Generar placa colombiana ejemplo: ABC-123
            char l1 = (char) ('A' + random.nextInt(26));
            char l2 = (char) ('A' + random.nextInt(26));
            char l3 = (char) ('A' + random.nextInt(26));
            int nums = 100 + random.nextInt(900);
            String plateNum = "" + l1 + l2 + l3 + "-" + nums;

            PlateType type = random.nextBoolean() ? PlateType.CAR : PlateType.MOTORCYCLE;

            // Randomly create if not exists (simple check mainly for re-runs)
            createPlate(plateNum, type);
        }
    }

    private void createPlate(String number, PlateType type) {
        PlateEntity plate = new PlateEntity();
        plate.setPlateNumber(number);
        plate.setPlateType(type);
        // Upload date random in last 6 months
        plate.setUploadDate(LocalDateTime.now().minusDays(random.nextInt(180)));
        try {
            plateRepository.save(plate);
        } catch (Exception e) {
            // Ignore duplicates
        }
    }

    private void loadServiceDeliveries() {
        logger.info("  -> Generando historial de servicios (últimos 30 días)...");

        List<DealershipEntity> dealerships = dealershipRepository.findAll();
        List<EmployeeEntity> allEmployees = employeeRepository.findAll();
        List<EmployeeEntity> messengers = allEmployees.stream().filter(e -> e.getRole() == Role.MESSENGER).toList();
        List<PlateEntity> plates = plateRepository.findAll();

        if (dealerships.isEmpty() || messengers.isEmpty() || plates.isEmpty())
            return;

        // Generar 100 servicios distribuidos
        for (int i = 0; i < 100; i++) {
            EmployeeEntity messenger = messengers.get(random.nextInt(messengers.size()));
            DealershipEntity dealership = dealerships.get(random.nextInt(dealerships.size()));
            PlateEntity plate = plates.get(random.nextInt(plates.size()));

            // Distribución de estados
            // 40% DELIVERED, 20% PENDING, 15% ASSIGNED, 15% RESOLVED, 10% CANCELED
            int r = random.nextInt(100);
            Status status;
            String obs;

            if (r < 40) {
                status = Status.DELIVERED;
                obs = "Entregado a satisfacción.";
            } else if (r < 60) {
                status = Status.PENDING;
                obs = "Pendiente de asignación.";
            } else if (r < 75) {
                status = Status.ASSIGNED;
                obs = "En ruta hacia el concesionario.";
            } else if (r < 90) {
                status = Status.RESOLVED;
                obs = "Incidencia resuelta en punto.";
            } else {
                status = Status.CANCELED;
                obs = "Cancelado por cliente.";
            }

            // Random creation date in last 30 days
            LocalDateTime createdTime = LocalDateTime.now().minusDays(random.nextInt(30))
                    .minusHours(random.nextInt(24));

            // Si es PENDING o ASSIGNED, asegurar que sea reciente (últimos 2 días) para
            // realismo
            if (status == Status.PENDING || status == Status.ASSIGNED) {
                createdTime = LocalDateTime.now().minusHours(random.nextInt(48));
            }

            createService(plate, dealership, messenger, status, obs, createdTime);
        }
    }

    private void createService(PlateEntity plate, DealershipEntity dealership, EmployeeEntity messenger, Status status,
            String observation, LocalDateTime createdTime) {
        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(status);
        service.setObservation(observation);
        service.setCreatedAt(createdTime);

        StatusHistoryEntity pendingHistory = createHistory(service, null, Status.PENDING, createdTime, null);
        List<StatusHistoryEntity> history = new ArrayList<>();
        history.add(pendingHistory);

        // Simulamos flujo de tiempos
        LocalDateTime runningTime = createdTime;

        if (status != Status.PENDING) {
            // PENDING -> ASSIGNED
            runningTime = runningTime.plusMinutes(15 + random.nextInt(45));
            history.add(createHistory(service, Status.PENDING, Status.ASSIGNED, runningTime, null));

            if (status == Status.DELIVERED) {
                runningTime = runningTime.plusMinutes(30 + random.nextInt(90));
                history.add(createHistory(service, Status.ASSIGNED, Status.DELIVERED, runningTime, messenger));
                service.setLockedAt(runningTime);
            } else if (status == Status.RESOLVED) {
                runningTime = runningTime.plusMinutes(40 + random.nextInt(60));
                history.add(createHistory(service, Status.ASSIGNED, Status.RESOLVED, runningTime, messenger));
                service.setLockedAt(runningTime);
            } else if (status == Status.CANCELED) {
                runningTime = runningTime.plusMinutes(5 + random.nextInt(20));
                history.add(createHistory(service, Status.ASSIGNED, Status.CANCELED, runningTime, null));
            }
        }

        service.setHistory(history);
        ServiceDeliveryEntity saved = serviceDeliveryRepository.save(service);

        // Generar tracking si corresponde
        if (status == Status.DELIVERED || status == Status.RESOLVED) {
            // Recuperamos el tiempo final para generar tracking hacia atrás
            LocalDateTime endTime = runningTime;
            // Generar ruta desde ubicación concesionario (aprox) +/- variacion
            createTrackingPath(messenger.getIdEmployee(), saved.getIdServiceDelivery(), dealership.getLatitude(),
                    dealership.getLongitude(), endTime);
        }
    }

    private StatusHistoryEntity createHistory(ServiceDeliveryEntity service, Status prev, Status curr,
            LocalDateTime date, EmployeeEntity changedBy) {
        StatusHistoryEntity history = new StatusHistoryEntity();
        history.setServiceDelivery(service);
        history.setPreviousStatus(prev);
        history.setNewStatus(curr);
        history.setChangeDate(date);
        history.setChangedBy(changedBy);
        return history;
    }

    private void createTrackingPath(Long messengerId, Long serviceId, Double destLat, Double destLng,
            LocalDateTime endTime) {
        // Generar 10 puntos llegando al destino
        // Empezamos 30 mins antes
        LocalDateTime pointTime = endTime.minusMinutes(30);
        double currentLat = destLat - 0.02; // Viene del sur/oeste aprox
        double currentLng = destLng - 0.02;

        for (int i = 0; i < 10; i++) {
            TrackingHistoryEntity track = new TrackingHistoryEntity();
            track.setMessengerId(messengerId);
            track.setServiceDeliveryId(serviceId);
            track.setRecordedAt(pointTime.plusMinutes(i * 3)); // cada 3 min

            // Interpolamos hacia el destino con algo de ruido
            double progress = (double) i / 10.0;
            track.setLatitude(currentLat + (destLat - currentLat) * progress + (random.nextDouble() * 0.001));
            track.setLongitude(currentLng + (destLng - currentLng) * progress + (random.nextDouble() * 0.001));

            track.setSource(TrackingSource.GPS);
            track.setSpeed(20.0 + random.nextDouble() * 40.0);

            trackingHistoryRepository.save(track);
        }
    }
}
