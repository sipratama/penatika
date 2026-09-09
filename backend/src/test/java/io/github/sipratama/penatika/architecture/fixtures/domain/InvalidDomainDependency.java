package io.github.sipratama.penatika.architecture.fixtures.domain;

import io.github.sipratama.penatika.architecture.fixtures.application.InvalidApplicationDependency;
import org.springframework.http.ResponseEntity;

public record InvalidDomainDependency(
        InvalidApplicationDependency applicationDependency,
        ResponseEntity<String> springDependency) {
}
