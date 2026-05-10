import java.util.*;
import java.io.*;

public class ParkingService {

    private List<ParkingSpot> spots;
    private List<Ticket> activeTickets;
    private List<Ticket> completedTickets;
    private List<User> users;

    private int ticketCounter;
    private double totalRevenue;
    private int freeSpotsCount;

    private static final String USERS_FILE = "users.txt";
    private static final String TICKETS_FILE = "tickets.txt";
    private static final String SPOTS_FILE = "spots.txt";
    private static final String ACTIVE_TICKETS_FILE = "active_tickets.txt";

    public ParkingService() {
        spots = new ArrayList<>();
        activeTickets = new ArrayList<>();
        completedTickets = new ArrayList<>();
        users = new ArrayList<>();
        ticketCounter = 1;
        totalRevenue = 0;
        freeSpotsCount = 0;

        loadAllData();

        if (users.isEmpty()) {
            users.add(new User("admin", "123456", User.Role.ADMIN));
            users.add(new User("entry", "1234", User.Role.ENTRY));
            users.add(new User("exit", "1234", User.Role.EXIT));
            saveUsersToFile();
        }

        if (spots.isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                spots.add(new ParkingSpot(i));
            }
            freeSpotsCount = 10;
            saveSpotsToFile();
        }
    }

    private void loadAllData() {
        loadUsersFromFile();
        loadSpotsFromFile();
        loadActiveTicketsFromFile();
        loadCompletedTicketsFromFile();
        calculateTicketCounter();
        updateFreeSpotsCount();
    }

    private void calculateTicketCounter() {
        int maxId = 0;
        for (Ticket t : activeTickets) {
            if (t.getTicketId() > maxId) maxId = t.getTicketId();
        }
        for (Ticket t : completedTickets) {
            if (t.getTicketId() > maxId) maxId = t.getTicketId();
        }
        ticketCounter = maxId + 1;
    }

    private void updateFreeSpotsCount() {
        freeSpotsCount = 0;
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) freeSpotsCount++;
        }
    }

    private void saveActiveTicketsToFile() {
        try (FileWriter writer = new FileWriter(ACTIVE_TICKETS_FILE)) {
            for (Ticket ticket : activeTickets) {
                writer.write(ticket.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving active tickets: " + e.getMessage());
        }
    }

    private void loadActiveTicketsFromFile() {
        File file = new File(ACTIVE_TICKETS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            activeTickets.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 7) {
                    boolean isActive = Boolean.parseBoolean(p[6]);
                    if (isActive) {
                        Ticket t = new Ticket(
                                Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                                p[3], p[4], Double.parseDouble(p[5]), true
                        );
                        activeTickets.add(t);

                        for (ParkingSpot spot : spots) {
                            if (spot.getId() == t.getSpotId()) {
                                spot.occupy();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading active tickets: " + e.getMessage());
        }
    }

    public void saveUsersToFile() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            for (User user : users) {
                writer.write(user.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            users.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    users.add(new User(parts[0], parts[1], User.Role.valueOf(parts[2]), true));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public void saveSpotsToFile() {
        try (PrintWriter writer = new PrintWriter(SPOTS_FILE)) {
            writer.println(spots.size());
            for (ParkingSpot spot : spots) {
                writer.println(spot.getId() + "," + spot.isOccupied());
            }
        } catch (IOException e) {
            System.err.println("Error saving spots: " + e.getMessage());
        }
    }

    public void loadSpotsFromFile() {
        File file = new File(SPOTS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextInt()) {
                int total = scanner.nextInt();
                scanner.nextLine();
                spots.clear();
                for (int i = 0; i < total; i++) {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (!line.isEmpty() && line.contains(",")) {
                            String[] parts = line.split(",");
                            int id = Integer.parseInt(parts[0]);
                            boolean occupied = Boolean.parseBoolean(parts[1]);
                            spots.add(new ParkingSpot(id, occupied));
                        } else {
                            spots.add(new ParkingSpot(i + 1));
                        }
                    } else {
                        spots.add(new ParkingSpot(i + 1));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading spots: " + e.getMessage());
        }
    }

    public void saveCompletedTicketToFile(Ticket ticket) {
        try (FileWriter writer = new FileWriter(TICKETS_FILE, true)) {
            writer.write(ticket.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error saving ticket: " + e.getMessage());
        }
    }

    public void loadCompletedTicketsFromFile() {
        File file = new File(TICKETS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            completedTickets.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 7) {
                    boolean isActive = Boolean.parseBoolean(p[6]);
                    if (!isActive) {
                        Ticket t = new Ticket(
                                Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                                p[3], p[4], Double.parseDouble(p[5]), false
                        );
                        completedTickets.add(t);
                        totalRevenue += t.getFees();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading completed tickets: " + e.getMessage());
        }
    }

    public Ticket parkCar(String plateNumber) {
        if (!Ticket.isValidPlate(plateNumber)) {
            throw new IllegalArgumentException("Invalid plate number. Use 3-15 letters/numbers");
        }

        String normalizedPlate = plateNumber.toUpperCase().trim();

        for (Ticket active : activeTickets) {
            if (active.getPlateNumber().equals(normalizedPlate)) {
                throw new IllegalArgumentException("This car is already parked at spot #" + active.getSpotId());
            }
        }

        if (freeSpotsCount == 0) {
            return null;
        }

        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                spot.occupy();
                freeSpotsCount--;
                Ticket ticket = new Ticket(ticketCounter++, normalizedPlate, spot.getId());
                activeTickets.add(ticket);
                saveActiveTicketsToFile();
                saveSpotsToFile();
                return ticket;
            }
        }

        return null;
    }

    public double exitCar(int ticketId) {
        Iterator<Ticket> iterator = activeTickets.iterator();
        while (iterator.hasNext()) {
            Ticket ticket = iterator.next();
            if (ticket.getTicketId() == ticketId) {
                ticket.setExitTime();
                long hours = ticket.calculateParkingDuration();
                double fees = (hours <= 1) ? 10 : 10 + (hours - 1) * 5;
                ticket.setFees(fees);
                totalRevenue += fees;

                for (ParkingSpot spot : spots) {
                    if (spot.getId() == ticket.getSpotId()) {
                        spot.free();
                        freeSpotsCount++;
                        break;
                    }
                }

                iterator.remove();
                completedTickets.add(ticket);

                saveCompletedTicketToFile(ticket);
                saveActiveTicketsToFile();
                saveSpotsToFile();

                return fees;
            }
        }
        return -1;
    }

    public Ticket findActiveTicketByPlate(String plateNumber) {
        for (Ticket ticket : activeTickets) {
            if (ticket.getPlateNumber().equals(plateNumber.toUpperCase().trim())) {
                return ticket;
            }
        }
        return null;
    }

    public String getTicketDetails(int ticketId) {
        for (Ticket ticket : activeTickets) {
            if (ticket.getTicketId() == ticketId) {
                return String.format(
                        "Ticket #%d\n" +
                                "Plate: %s\n" +
                                "Spot: #%d\n" +
                                "Entry Time: %s",
                        ticket.getTicketId(), ticket.getPlateNumber(),
                        ticket.getSpotId(), ticket.getEntryTime()
                );
            }
        }
        return null;
    }
public void addSpot() {
    int maxId = 0;

    for (ParkingSpot spot : spots) {
        if (spot.getId() > maxId) {
            maxId = spot.getId();
        }
    }

    int newId = maxId + 1;

    spots.add(new ParkingSpot(newId));
    freeSpotsCount++;
    saveSpotsToFile();
}

    public boolean removeSpot(int spotId) {
        // Find the spot to remove
        ParkingSpot targetSpot = null;
        for (ParkingSpot spot : spots) {
            if (spot.getId() == spotId) {
                targetSpot = spot;
                break;
            }
        }

        if (targetSpot == null) {
            return false; // Spot not found
        }

        // Check if spot is occupied
        if (targetSpot.isOccupied()) {
            return false; // Cannot delete occupied spot
        }

        // Remove the spot
        boolean removed = spots.remove(targetSpot);
        if (removed) {
            saveSpotsToFile();
            updateFreeSpotsCount();
        }
        return removed;
    }

    public int getTotalSpots() {
        return spots.size();
    }

    public int getFreeSpots() {
        return freeSpotsCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public List<Ticket> getActiveTickets() {
        return new ArrayList<>(activeTickets);
    }

    public List<Ticket> getCompletedTickets() {
        return new ArrayList<>(completedTickets);
    }

    public List<ParkingSpot> getSpots() {
        return new ArrayList<>(spots);
    }

    public boolean addUser(String username, String password, User.Role role) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty()) return false;

        for (User user : users) {
            if (user.getUsername().equals(username)) return false;
        }
        users.add(new User(username, password, role));
        saveUsersToFile();
        return true;
    }

    public boolean removeUser(String username) {
        long adminCount = users.stream()
                .filter(u -> u.getRoleEnum() == User.Role.ADMIN)
                .count();

        User targetUser = null;
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                targetUser = u;
                break;
            }
        }

        if (targetUser == null) return false;

        if (targetUser.getRoleEnum() == User.Role.ADMIN && adminCount <= 1) {
            return false;
        }

        boolean removed = users.remove(targetUser);
        if (removed) saveUsersToFile();
        return removed;
    }

    public boolean updateUser(String username, String newPassword, User.Role role) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(newPassword);
                }
                user.setRole(role);
                saveUsersToFile();
                return true;
            }
        }
        return false;
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.checkPassword(password)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public boolean hasAdmin() {
        return users.stream().anyMatch(u -> u.getRoleEnum() == User.Role.ADMIN);
    }
}