package io.github.sipratama.penatika.architecture.fixtures.application;

import javax.sql.DataSource;

public record InvalidPersistenceDependency(DataSource dataSource) {
}
