package io.github.sipratama.penatika.architecture.fixtures.application;

import io.github.sipratama.penatika.architecture.fixtures.adapter.InvalidAdapterDependency;
import org.springframework.http.ResponseEntity;

public record InvalidApplicationDependency(
        InvalidAdapterDependency adapterDependency,
        ResponseEntity<String> springDependency) {
}
