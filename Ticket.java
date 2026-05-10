import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Ticket {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private int ticketId;
    private String plateNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int spotId;
    private double fees;
    private boolean isActive;

    public Ticket(int ticketId, String plateNumber, int spotId) {
        this.ticketId = ticketId;
        this.plateNumber = plateNumber.toUpperCase().trim();
        this.spotId = spotId;
        this.entryTime = LocalDateTime.now();
        this.isActive = true;
    }

    public Ticket(int ticketId, String plateNumber, int spotId,
                  String entryTimeStr, String exitTimeStr, double fees, boolean isActive) {
        this.ticketId = ticketId;
        this.plateNumber = plateNumber;
        this.spotId = spotId;
        this.entryTime = LocalDateTime.parse(entryTimeStr, FORMATTER);
        this.fees = fees;
        this.isActive = isActive;

        if (exitTimeStr != null && !exitTimeStr.isEmpty() && !"null".equals(exitTimeStr)) {
            this.exitTime = LocalDateTime.parse(exitTimeStr, FORMATTER);
        }
    }

    public void setExitTime() {
        this.exitTime = LocalDateTime.now();
        this.isActive = false;
    }

    public long calculateParkingDuration() {
        if (exitTime == null) {
            throw new IllegalStateException("Exit time not set");
        }
        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        if (minutes % 60 > 0) hours++;
        if (hours == 0) hours = 1;
        return hours;
    }

    public static boolean isValidPlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) return false;
        String cleanPlate = plate.trim().toUpperCase();
        return cleanPlate.matches("^[A-Z0-9\\s\\-]{3,15}$");
    }

    public int getTicketId() { return ticketId; }
    public int getSpotId() { return spotId; }
    public String getPlateNumber() { return plateNumber; }
    public double getFees() { return fees; }
    public boolean isActive() { return isActive; }

    public String getEntryTime() {
        return entryTime.format(FORMATTER);
    }

    public String getExitTime() {
        return exitTime != null ? exitTime.format(FORMATTER) : "";
    }

    public void setFees(double fees) { this.fees = fees; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("%d,%s,%d,%s,%s,%.2f,%b",
                ticketId, plateNumber, spotId, getEntryTime(),
                getExitTime(), fees, isActive);
    }
}