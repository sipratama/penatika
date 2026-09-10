# Flyway Migration Location

Flyway SQL migrations live in this directory.

`V001__create_first_protected_slice.sql` establishes the first approved
physical product schema for the protected vertical slice. It contains schema
and constraints only; no Teacher, identity, lesson, or classroom seed rows.

After a migration is applied to a shared environment, do not rewrite its
meaning. Add a new corrective migration instead.
