package io.github.sipratama.penatika.bootstrap.configuration;

public final class PenatikaPersistenceProperties {

    private boolean enabled;
    private String jdbcUrl = "";
    private String username = "";
    private String password = "";
    private boolean migrationsEnabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMigrationsEnabled() {
        return migrationsEnabled;
    }

    public void setMigrationsEnabled(boolean migrationsEnabled) {
        this.migrationsEnabled = migrationsEnabled;
    }

    public void validateForActivation() {
        requireNonBlank(jdbcUrl, "penatika.persistence.jdbc-url");
        requireNonBlank(username, "penatika.persistence.username");
        requireNonBlank(password, "penatika.persistence.password");
    }

    private static void requireNonBlank(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required property '" + propertyName + "' must not be blank when persistence is enabled");
        }
    }
}
