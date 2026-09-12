package io.github.sipratama.penatika.bootstrap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("penatika")
public final class PenatikaProperties {

    private PenatikaEnvironment environment = PenatikaEnvironment.LOCAL;
    private final PenatikaPersistenceProperties persistence = new PenatikaPersistenceProperties();
    private final PenatikaDisplayProperties display = new PenatikaDisplayProperties();

    public PenatikaEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(PenatikaEnvironment environment) {
        this.environment = environment;
    }

    public PenatikaPersistenceProperties getPersistence() {
        return persistence;
    }

    public PenatikaDisplayProperties getDisplay() {
        return display;
    }
}
