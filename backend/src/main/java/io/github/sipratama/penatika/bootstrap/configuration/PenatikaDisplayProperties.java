package io.github.sipratama.penatika.bootstrap.configuration;

import java.time.Duration;

public final class PenatikaDisplayProperties {

    private final Liveness liveness = new Liveness();

    public Liveness getLiveness() {
        return liveness;
    }

    public static final class Liveness {

        private Duration heartbeatInterval = Duration.ofSeconds(15);
        private Duration deadTimeout = Duration.ofSeconds(45);

        public Duration getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public void setHeartbeatInterval(Duration heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }

        public Duration getDeadTimeout() {
            return deadTimeout;
        }

        public void setDeadTimeout(Duration deadTimeout) {
            this.deadTimeout = deadTimeout;
        }

        public void validate() {
            if (heartbeatInterval == null || heartbeatInterval.isZero() || heartbeatInterval.isNegative()) {
                throw new IllegalStateException(
                        "penatika.display.liveness.heartbeat-interval must be strictly positive");
            }
            if (deadTimeout == null || deadTimeout.isZero() || deadTimeout.isNegative()) {
                throw new IllegalStateException(
                        "penatika.display.liveness.dead-timeout must be strictly positive");
            }
            if (deadTimeout.compareTo(heartbeatInterval.multipliedBy(3)) < 0) {
                throw new IllegalStateException(
                        "penatika.display.liveness.dead-timeout must be at least 3x the heartbeat interval");
            }
        }
    }
}
