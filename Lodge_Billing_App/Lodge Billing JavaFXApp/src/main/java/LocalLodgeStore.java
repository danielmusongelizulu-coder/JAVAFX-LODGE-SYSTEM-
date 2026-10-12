import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

final class LocalLodgeStore {
    private final Path dataFile = Path.of(
            System.getProperty("user.home"), ".muzu-lodge", "lodge-data.properties");

    void load(List<Bill> bills, List<Reservation> reservations) throws IOException {
        Properties data = new Properties();
        if (Files.exists(dataFile)) {
            try (var input = Files.newInputStream(dataFile)) {
                data.load(input);
            }
        }
        int billCount = Integer.parseInt(data.getProperty("bill.count", "0"));
        int reservationCount = Integer.parseInt(data.getProperty("reservation.count", "0"));
        for (int i = 0; i < billCount; i++) {
            String key = "bill." + i + ".";
            bills.add(new Bill(data.getProperty(key + "id"), data.getProperty(key + "guest"),
                    data.getProperty(key + "room"), Integer.parseInt(data.getProperty(key + "nights")),
                    Double.parseDouble(data.getProperty(key + "total")),
                    Double.parseDouble(data.getProperty(key + "paid")),
                    Double.parseDouble(data.getProperty(key + "balance"))));
        }
        for (int i = 0; i < reservationCount; i++) {
            String key = "reservation." + i + ".";
            reservations.add(new Reservation(data.getProperty(key + "id"), data.getProperty(key + "guest"),
                    data.getProperty(key + "room"), data.getProperty(key + "checkIn"),
                    Integer.parseInt(data.getProperty(key + "nights")), data.getProperty(key + "notes"),
                    data.getProperty(key + "status"), parseTime(data.getProperty(key + "checkedInAt")),
                    parseTime(data.getProperty(key + "checkedOutAt"))));
        }
    }

    void save(List<Bill> bills, List<Reservation> reservations) throws IOException {
        Properties data = new Properties();
        data.setProperty("bill.count", Integer.toString(bills.size()));
        for (int i = 0; i < bills.size(); i++) {
            Bill bill = bills.get(i);
            String key = "bill." + i + ".";
            data.setProperty(key + "id", bill.getBillId());
            data.setProperty(key + "guest", bill.getGuestName());
            data.setProperty(key + "room", bill.getRoomType());
            data.setProperty(key + "nights", Integer.toString(bill.getNights()));
            data.setProperty(key + "total", Double.toString(bill.getTotalAmount()));
            data.setProperty(key + "paid", Double.toString(bill.getPaidAmount()));
            data.setProperty(key + "balance", Double.toString(bill.getBalanceAmount()));
        }
        data.setProperty("reservation.count", Integer.toString(reservations.size()));
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);
            String key = "reservation." + i + ".";
            data.setProperty(key + "id", reservation.getReservationId());
            data.setProperty(key + "guest", reservation.getGuestName());
            data.setProperty(key + "room", reservation.getRoomType());
            data.setProperty(key + "checkIn", reservation.getCheckIn());
            data.setProperty(key + "nights", Integer.toString(reservation.getNights()));
            data.setProperty(key + "notes", reservation.getNotes());
            data.setProperty(key + "status", reservation.getStatus());
            data.setProperty(key + "checkedInAt", formatTime(reservation.getCheckedInTime()));
            data.setProperty(key + "checkedOutAt", formatTime(reservation.getCheckedOutTime()));
        }

        Files.createDirectories(dataFile.getParent());
        Path temporaryFile = Files.createTempFile(dataFile.getParent(), "lodge-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                data.store(output, "MUZU Lodge local guest and reservation records");
            }
            try {
                Files.move(temporaryFile, dataFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private LocalDateTime parseTime(String value) {
        return value == null || value.isEmpty() ? null : LocalDateTime.parse(value);
    }
}
