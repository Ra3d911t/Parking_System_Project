import java.util.Base64;

/**
 * Represents a system user.
 */
public class User extends Person implements Reportable {

    private Role role;

    public enum Role {
        ADMIN, ENTRY, EXIT
    }

    // ── Constructor for NEW user (plain password → gets hashed) ──
    public User(String username, String password, Role role) {
        super(username, hashPassword(password));
        this.role = role;
    }

    // ── Constructor for LOADING from file (password already hashed) ──
    public User(String username, String passwordHash, Role role, boolean isHash) {
        super(username, passwordHash);
        this.role = role;
    }

    // ── Password hashing (Base64 encoding) ──
    private static String hashPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // ── Abstract method implementations (from Person) ──
    @Override
    public String getRole() {
        return role.name();
    }

    @Override
    public boolean checkPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    // ── Reportable interface implementation ──
    @Override
    public String generateReport() {
        return String.format("User: %-15s | Role: %s", username, role);
    }

    // ── Additional getters ──
    public Role getRoleEnum() { return role; }

    public void setPassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return username + "," + passwordHash + "," + role;
    }
}