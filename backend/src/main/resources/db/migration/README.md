# Flyway Migration Location

Flyway SQL migrations live in this directory.

No Penatika domain migration exists yet. The first `V001` migration must own
a real approved physical schema change; never create a placeholder migration.
After a migration is applied to a shared environment, do not rewrite its
meaning. Add a new corrective migration instead.
