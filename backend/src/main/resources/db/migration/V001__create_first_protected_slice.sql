CREATE TABLE identity_teacher_account (
    id uuid PRIMARY KEY,
    status varchar(32) NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT ck_identity_teacher_account_status
        CHECK (status IN ('ACTIVE', 'DISABLED', 'DELETION_REQUESTED', 'CLOSED'))
);

CREATE TABLE identity_external_identity_link (
    id uuid PRIMARY KEY,
    teacher_account_id uuid NOT NULL,
    issuer text NOT NULL,
    subject text NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT fk_identity_external_identity_teacher
        FOREIGN KEY (teacher_account_id) REFERENCES identity_teacher_account (id),
    CONSTRAINT uq_identity_external_identity_issuer_subject UNIQUE (issuer, subject),
    CONSTRAINT ck_identity_external_identity_issuer_nonempty CHECK (btrim(issuer) <> ''),
    CONSTRAINT ck_identity_external_identity_subject_nonempty CHECK (btrim(subject) <> '')
);

CREATE INDEX ix_identity_external_identity_teacher
    ON identity_external_identity_link (teacher_account_id);

CREATE TABLE identity_teacher_browser_session (
    id uuid PRIMARY KEY,
    teacher_account_id uuid NOT NULL,
    credential_verifier char(64) NOT NULL,
    csrf_verifier char(64) NOT NULL,
    created_at timestamptz NOT NULL,
    last_active_at timestamptz NOT NULL,
    idle_expires_at timestamptz NOT NULL,
    absolute_expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    CONSTRAINT fk_identity_teacher_browser_session_teacher
        FOREIGN KEY (teacher_account_id) REFERENCES identity_teacher_account (id),
    CONSTRAINT uq_identity_teacher_browser_session_credential UNIQUE (credential_verifier),
    CONSTRAINT uq_identity_teacher_browser_session_csrf UNIQUE (csrf_verifier),
    CONSTRAINT uq_identity_teacher_browser_session_id_teacher UNIQUE (id, teacher_account_id),
    CONSTRAINT ck_identity_teacher_browser_session_credential_sha256
        CHECK (credential_verifier ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_identity_teacher_browser_session_csrf_sha256
        CHECK (csrf_verifier ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_identity_teacher_browser_session_activity
        CHECK (last_active_at >= created_at),
    CONSTRAINT ck_identity_teacher_browser_session_idle_expiry
        CHECK (idle_expires_at >= last_active_at),
    CONSTRAINT ck_identity_teacher_browser_session_absolute_expiry
        CHECK (absolute_expires_at >= created_at),
    CONSTRAINT ck_identity_teacher_browser_session_revocation
        CHECK (revoked_at IS NULL OR revoked_at >= created_at)
);

CREATE INDEX ix_identity_teacher_browser_session_teacher
    ON identity_teacher_browser_session (teacher_account_id);

CREATE TABLE lesson_lesson (
    id uuid PRIMARY KEY,
    teacher_account_id uuid NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT fk_lesson_lesson_teacher
        FOREIGN KEY (teacher_account_id) REFERENCES identity_teacher_account (id)
);

CREATE INDEX ix_lesson_lesson_teacher ON lesson_lesson (teacher_account_id);

CREATE TABLE lesson_lesson_version (
    id uuid PRIMARY KEY,
    lesson_id uuid NOT NULL,
    readiness_status varchar(32) NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT fk_lesson_lesson_version_lesson
        FOREIGN KEY (lesson_id) REFERENCES lesson_lesson (id),
    CONSTRAINT ck_lesson_lesson_version_readiness
        CHECK (readiness_status IN ('DRAFT', 'CLASSROOM_READY'))
);

CREATE INDEX ix_lesson_lesson_version_lesson ON lesson_lesson_version (lesson_id);

CREATE TABLE lesson_lesson_scene (
    id uuid PRIMARY KEY,
    lesson_version_id uuid NOT NULL,
    position bigint NOT NULL,
    CONSTRAINT fk_lesson_lesson_scene_version
        FOREIGN KEY (lesson_version_id) REFERENCES lesson_lesson_version (id),
    CONSTRAINT uq_lesson_lesson_scene_position UNIQUE (lesson_version_id, position),
    CONSTRAINT ck_lesson_lesson_scene_position
        CHECK (position BETWEEN 0 AND 9007199254740991)
);

CREATE INDEX ix_lesson_lesson_scene_version ON lesson_lesson_scene (lesson_version_id);

CREATE TABLE lesson_scene_block (
    id uuid PRIMARY KEY,
    lesson_scene_id uuid NOT NULL,
    position bigint NOT NULL,
    block_type varchar(32) NOT NULL,
    plain_text varchar(16384) NOT NULL,
    CONSTRAINT fk_lesson_scene_block_scene
        FOREIGN KEY (lesson_scene_id) REFERENCES lesson_lesson_scene (id),
    CONSTRAINT uq_lesson_scene_block_position UNIQUE (lesson_scene_id, position),
    CONSTRAINT ck_lesson_scene_block_position
        CHECK (position BETWEEN 0 AND 9007199254740991),
    CONSTRAINT ck_lesson_scene_block_type CHECK (block_type = 'PLAIN_TEXT'),
    CONSTRAINT ck_lesson_scene_block_plain_text
        CHECK (char_length(plain_text) BETWEEN 1 AND 16384)
);

CREATE INDEX ix_lesson_scene_block_scene ON lesson_scene_block (lesson_scene_id);

CREATE TABLE classroom_session (
    id uuid PRIMARY KEY,
    teacher_account_id uuid NOT NULL,
    lesson_version_id uuid NOT NULL,
    lifecycle_state varchar(32) NOT NULL,
    current_scene_position bigint NOT NULL,
    revision bigint NOT NULL,
    started_at timestamptz NOT NULL,
    CONSTRAINT fk_classroom_session_teacher
        FOREIGN KEY (teacher_account_id) REFERENCES identity_teacher_account (id),
    CONSTRAINT fk_classroom_session_lesson_version
        FOREIGN KEY (lesson_version_id) REFERENCES lesson_lesson_version (id),
    CONSTRAINT uq_classroom_session_id_teacher UNIQUE (id, teacher_account_id),
    CONSTRAINT ck_classroom_session_lifecycle
        CHECK (lifecycle_state IN ('CREATED', 'READY', 'ACTIVE', 'FAILED', 'EXPIRED')),
    CONSTRAINT ck_classroom_session_position
        CHECK (current_scene_position BETWEEN 0 AND 9007199254740991),
    CONSTRAINT ck_classroom_session_revision
        CHECK (revision BETWEEN 0 AND 9007199254740991)
);

CREATE INDEX ix_classroom_session_teacher ON classroom_session (teacher_account_id);
CREATE INDEX ix_classroom_session_lesson_version ON classroom_session (lesson_version_id);

CREATE TABLE classroom_pairing_grant (
    id uuid PRIMARY KEY,
    classroom_session_id uuid NOT NULL,
    participant_role varchar(32) NOT NULL,
    credential_verifier char(64) NOT NULL,
    issued_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    revoked_at timestamptz,
    CONSTRAINT fk_classroom_pairing_grant_session
        FOREIGN KEY (classroom_session_id) REFERENCES classroom_session (id),
    CONSTRAINT uq_classroom_pairing_grant_credential UNIQUE (credential_verifier),
    CONSTRAINT ck_classroom_pairing_grant_role
        CHECK (participant_role IN ('TEACHER_CONTROLLER', 'CLASSROOM_DISPLAY')),
    CONSTRAINT ck_classroom_pairing_grant_credential_sha256
        CHECK (credential_verifier ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_classroom_pairing_grant_expiry
        CHECK (expires_at = issued_at + interval '5 minutes'),
    CONSTRAINT ck_classroom_pairing_grant_consumption
        CHECK (consumed_at IS NULL OR (consumed_at >= issued_at AND consumed_at < expires_at)),
    CONSTRAINT ck_classroom_pairing_grant_revocation
        CHECK (revoked_at IS NULL OR revoked_at >= issued_at),
    CONSTRAINT ck_classroom_pairing_grant_terminal_state
        CHECK (NOT (consumed_at IS NOT NULL AND revoked_at IS NOT NULL))
);

CREATE INDEX ix_classroom_pairing_grant_session
    ON classroom_pairing_grant (classroom_session_id);

CREATE TABLE identity_participant_session (
    id uuid PRIMARY KEY,
    classroom_session_id uuid NOT NULL,
    participant_role varchar(32) NOT NULL,
    credential_verifier char(64) NOT NULL,
    teacher_account_id uuid,
    teacher_browser_session_id uuid,
    created_at timestamptz NOT NULL,
    expires_at timestamptz,
    revoked_at timestamptz,
    CONSTRAINT fk_identity_participant_session_classroom
        FOREIGN KEY (classroom_session_id) REFERENCES classroom_session (id),
    CONSTRAINT fk_identity_participant_session_teacher
        FOREIGN KEY (teacher_account_id) REFERENCES identity_teacher_account (id),
    CONSTRAINT fk_identity_participant_session_teacher_browser
        FOREIGN KEY (teacher_browser_session_id, teacher_account_id)
        REFERENCES identity_teacher_browser_session (id, teacher_account_id),
    CONSTRAINT fk_identity_participant_session_classroom_owner
        FOREIGN KEY (classroom_session_id, teacher_account_id)
        REFERENCES classroom_session (id, teacher_account_id),
    CONSTRAINT uq_identity_participant_session_credential UNIQUE (credential_verifier),
    CONSTRAINT uq_identity_participant_session_controller_context
        UNIQUE (id, classroom_session_id, participant_role, teacher_account_id, teacher_browser_session_id),
    CONSTRAINT ck_identity_participant_session_role
        CHECK (participant_role IN ('TEACHER_CONTROLLER', 'CLASSROOM_DISPLAY')),
    CONSTRAINT ck_identity_participant_session_credential_sha256
        CHECK (credential_verifier ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_identity_participant_session_role_binding CHECK (
        (participant_role = 'TEACHER_CONTROLLER'
            AND teacher_account_id IS NOT NULL
            AND teacher_browser_session_id IS NOT NULL)
        OR
        (participant_role = 'CLASSROOM_DISPLAY'
            AND teacher_account_id IS NULL
            AND teacher_browser_session_id IS NULL)
    ),
    CONSTRAINT ck_identity_participant_session_expiry
        CHECK (expires_at IS NULL OR expires_at > created_at),
    CONSTRAINT ck_identity_participant_session_revocation
        CHECK (revoked_at IS NULL OR revoked_at >= created_at)
);

CREATE UNIQUE INDEX uq_identity_participant_session_active_role
    ON identity_participant_session (classroom_session_id, participant_role)
    WHERE revoked_at IS NULL;

CREATE INDEX ix_identity_participant_session_classroom
    ON identity_participant_session (classroom_session_id);

CREATE TABLE classroom_accepted_command (
    id uuid PRIMARY KEY,
    classroom_session_id uuid NOT NULL,
    command_id varchar(128) NOT NULL,
    expected_revision bigint NOT NULL,
    command_type varchar(32) NOT NULL,
    action varchar(32) NOT NULL,
    teacher_account_id uuid NOT NULL,
    teacher_browser_session_id uuid NOT NULL,
    controller_participant_session_id uuid NOT NULL,
    controller_role varchar(32) NOT NULL,
    resulting_revision bigint NOT NULL,
    accepted_at timestamptz NOT NULL,
    CONSTRAINT fk_classroom_accepted_command_classroom_owner
        FOREIGN KEY (classroom_session_id, teacher_account_id)
        REFERENCES classroom_session (id, teacher_account_id),
    CONSTRAINT fk_classroom_accepted_command_teacher_browser
        FOREIGN KEY (teacher_browser_session_id, teacher_account_id)
        REFERENCES identity_teacher_browser_session (id, teacher_account_id),
    CONSTRAINT fk_classroom_accepted_command_controller_context
        FOREIGN KEY (
            controller_participant_session_id,
            classroom_session_id,
            controller_role,
            teacher_account_id,
            teacher_browser_session_id
        ) REFERENCES identity_participant_session (
            id,
            classroom_session_id,
            participant_role,
            teacher_account_id,
            teacher_browser_session_id
        ),
    CONSTRAINT uq_classroom_accepted_command_identity
        UNIQUE (classroom_session_id, command_id),
    CONSTRAINT ck_classroom_accepted_command_id_nonempty
        CHECK (btrim(command_id) <> ''),
    CONSTRAINT ck_classroom_accepted_command_type CHECK (command_type = 'DIRECT_ACTION'),
    CONSTRAINT ck_classroom_accepted_command_action CHECK (action = 'NEXT'),
    CONSTRAINT ck_classroom_accepted_command_controller_role
        CHECK (controller_role = 'TEACHER_CONTROLLER'),
    CONSTRAINT ck_classroom_accepted_command_expected_revision
        CHECK (expected_revision BETWEEN 0 AND 9007199254740991),
    CONSTRAINT ck_classroom_accepted_command_resulting_revision
        CHECK (resulting_revision BETWEEN 0 AND 9007199254740991),
    CONSTRAINT ck_classroom_accepted_command_revision_progress
        CHECK (resulting_revision > expected_revision)
);

CREATE INDEX ix_classroom_accepted_command_controller
    ON classroom_accepted_command (controller_participant_session_id);
