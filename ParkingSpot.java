public class ParkingSpot {

    private int id;
    private boolean occupied;

    public ParkingSpot(int id) {
        this.id = id;
        this.occupied = false;
    }

    public ParkingSpot(int id, boolean occupied) {
        this.id = id;
        this.occupied = occupied;
    }

    public int getId() {
        return id;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void occupy() {
        this.occupied = true;
    }

    public void free() {
        this.occupied = false;
    }

    @Override
    public String toString() {
        return "Spot #" + id + " - " + (occupied ? "Occupied" : "Free");
    }
}