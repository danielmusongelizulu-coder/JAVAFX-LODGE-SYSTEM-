import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private static final DateTimeFormatter EVENT_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String reservationId;
    private final String guestName;
    private final String roomType;
    private final String checkIn;
    private final int nights;
    private final String notes;
    private String status = "Confirmed";
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;

    public Reservation(String reservationId, String guestName, String roomType, String checkIn,
                       int nights, String notes) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.nights = nights;
        this.notes = notes;
    }

    public Reservation(String reservationId, String guestName, String roomType, String checkIn,
                       int nights, String notes, String status, LocalDateTime checkedInAt,
                       LocalDateTime checkedOutAt) {
        this(reservationId, guestName, roomType, checkIn, nights, notes);
        if (!"Confirmed".equals(status) && !"Checked In".equals(status) && !"Checked Out".equals(status)) {
            throw new IllegalArgumentException("Unsupported reservation status: " + status);
        }
        this.status = status;
        this.checkedInAt = checkedInAt;
        this.checkedOutAt = checkedOutAt;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public int getNights() {
        return nights;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public String getCheckedInAt() {
        return formatEventTime(checkedInAt);
    }

    public String getCheckedOutAt() {
        return formatEventTime(checkedOutAt);
    }

    public LocalDateTime getCheckedInTime() {
        return checkedInAt;
    }

    public LocalDateTime getCheckedOutTime() {
        return checkedOutAt;
    }

    public boolean isConfirmed() {
        return "Confirmed".equals(status);
    }

    public boolean isCheckedIn() {
        return "Checked In".equals(status);
    }

    public void checkIn() {
        if (!isConfirmed()) {
            throw new IllegalStateException("Only confirmed reservations can be checked in.");
        }
        checkedInAt = LocalDateTime.now();
        status = "Checked In";
    }

    public void checkOut() {
        if (!isCheckedIn()) {
            throw new IllegalStateException("Only checked-in guests can be checked out.");
        }
        checkedOutAt = LocalDateTime.now();
        status = "Checked Out";
    }

    private String formatEventTime(LocalDateTime eventTime) {
        return eventTime == null ? "—" : eventTime.format(EVENT_TIME_FORMAT);
    }
}
