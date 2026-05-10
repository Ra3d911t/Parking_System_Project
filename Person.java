/**
 * Abstract base class representing any person in the system.
 * Demonstrates OOP: Abstraction + Inheritance
 */
public abstract class Person {

    protected String username;
    protected String passwordHash;

    public Person(String username, String passwordHash) {
        this.username     = username;
        this.passwordHash = passwordHash;
    }

    // ── Abstract methods – every subclass must implement ──
    public abstract String getRole();
    public abstract boolean checkPassword(String password);

    // ── Concrete shared methods ──
    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    @Override
    public String toString() {
        return "Person{username='" + username + "', role=" + getRole() + "}";
    }
}
