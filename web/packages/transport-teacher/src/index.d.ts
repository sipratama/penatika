export interface paths {
    "/api/teacher-session": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Bootstrap the authenticated Teacher browser session
         * @description Confirms that the opaque backend-managed browser session currently resolves to an active authorized TeacherAccount and returns the CSRF material required for state-changing Teacher requests. The response intentionally exposes no TeacherAccount identifier, OIDC identity, provider token, profile, institution, or account-lifecycle detail.
         */
        get: operations["getAuthenticatedTeacherSession"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Start a Classroom Session
         * @description Creates backend-authoritative Classroom Session state from an existing immutable classroom-ready LessonVersion. The authenticated Teacher must be authorized for the referenced LessonVersion; possession of its identifier does not prove ownership, authorization, or readiness. Nonexistent and unauthorized LessonVersion references share the same non-disclosing response. No Location header is defined because this contract does not yet define a Classroom Session read operation.
         */
        post: operations["startClassroomSession"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/pairing-grants": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Create a role-bound PairingGrant
         * @description Creates an opaque, single-use, revocable PairingGrant bound to the authorized Classroom Session and requested participant role. The grant expires exactly five minutes after backend issuance. The backend owns expiry, consumption, revocation, session-state, and role-slot checks. Controller and Display grants may be issued and redeemed in either order; neither role depends on the other already existing.
         */
        post: operations["createPairingGrant"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/pairing-grants/{pairingGrantId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Revoke a PairingGrant
         * @description Makes the identified PairingGrant unusable. After authorization for the parent Classroom Session succeeds, revocation is retry-safe and returns the same result when the grant is already absent, expired, consumed, or revoked. This prevents the non-secret PairingGrantId from becoming an existence oracle. The pairingToken is never accepted in a URL.
         */
        delete: operations["revokePairingGrant"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/teacher-controller-participants": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Establish a Teacher Controller participant session
         * @description Redeems a valid TEACHER_CONTROLLER PairingGrant and creates a distinct role-bound participant browser session. The active authenticated Teacher must also be authorized for the Classroom Session bound to the grant. The PairingGrant alone never authenticates the Teacher. An existing active Controller is not silently replaced.
         */
        post: operations["establishTeacherControllerParticipant"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/controller-state": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve authoritative Controller reconciliation state
         * @description Returns the minimal current backend-authoritative Classroom Session state required by the first-slice Teacher Controller to reconcile its revision context before creating another command. The request requires both a current authenticated Teacher browser session and a current, active, non-revoked TEACHER_CONTROLLER participant browser session bound to the path Classroom Session. The backend also verifies current Teacher authorization for that session. Either cookie alone, a CLASSROOM_DISPLAY participant, a pairing credential, cached state, or knowledge of the ClassroomSessionId is insufficient. No CSRF material is required because this GET is safe and read-only.
         *     After reconnect, after backend-authority loss, or after an unseen command is rejected as STALE_REVISION, the Controller retrieves this state and replaces its cached revision context before creating a new CommandId. The returned revision is current backend authority at response time; it does not authorize a later mutation or bypass the command operation's current authentication, authorization, Display delivery eligibility, session-state, identity, or revision checks. An accepted command with uncertain acknowledgement is instead retried using its existing CommandId and original logical request.
         *     Scene position, classroom-safe Display content, Display connection or synchronization status, participant identifiers, pairing state, TeacherAccount identity, AI state, and private diagnostics are omitted because this bounded reconciliation consumer requires only session identity and authoritative revision. Richer Controller-private projection remains outside this first contract slice.
         */
        get: operations["getClassroomControllerState"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/commands": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Submit a deterministic Classroom Session command
         * @description Submits the first supported deterministic, non-generative classroom command: DIRECT_ACTION with action NEXT. The request requires current authenticated Teacher authority, active TEACHER_CONTROLLER participant authority bound to the path Classroom Session, current Teacher authorization for that session, and valid CSRF material. A CLASSROOM_DISPLAY participant cannot authorize this operation.
         *     Command identity is scoped by (ClassroomSessionId, CommandId). For an unseen CommandId, the backend validates current mutation eligibility and requires expectedRevision to equal the current authoritative revision before applying at most one authoritative mutation effect. A new student-facing mutation is rejected unless an active authorized Display SSE stream exists and that Display has an accepted acknowledgement for the current authoritative Display projection revision. Session lifecycle and backend authority must also permit mutation. Logical request equivalence consists only of the path ClassroomSessionId plus commandId, expectedRevision, commandType, and action. Browser-session cookies, CSRF material, correlation identifiers, and other transport metadata are evaluated separately and are not part of command equivalence.
         *     Current authentication, session authorization, Controller authority, and CSRF are evaluated on every request before an accepted command outcome is disclosed. An equivalent retry of an already accepted command returns its original outcome without another mutation, even if the Classroom Session revision has since advanced or the Display later became unavailable. Reusing an accepted CommandId with a different expectedRevision is the currently reachable conflicting-reuse case. Logical equivalence also includes commandType and action so future compatible command expansion cannot silently reinterpret an existing identity. Under the current closed request schema, any commandType other than DIRECT_ACTION or action other than NEXT is rejected as structural validation and does not reach command-identity conflict evaluation.
         *     Clients must not create or queue new commands while backend authority is unreachable; only a command already sent with uncertain acknowledgement may be retried using its existing identity. Only a command that crosses the authorized boundary and is accepted reserves its command identity; unauthenticated, unauthorized, CSRF-rejected, or structurally invalid attempts do not reserve arbitrary CommandIds. A Controller reconciles the current revision through GET /api/classroom-sessions/{classroomSessionId}/controller-state before creating a new command after reconnect or STALE_REVISION.
         */
        post: operations["submitClassroomSessionCommand"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: never;
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    getAuthenticatedTeacherSession: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description The Teacher browser session is authenticated and currently usable. */
            200: {
                headers: {
                    /** @description Prevents storage of the session-bound CSRF material. */
                    "Cache-Control"?: "no-store";
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /** @description Opaque session-bound CSRF material safe for delivery to authenticated Teacher Web JavaScript. It is submitted only in the X-Penatika-CSRF request header and has no authentication or authorization meaning by itself. */
                        csrfToken: string;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    startClassroomSession: {
        parameters: {
            query?: never;
            header: {
                /** @description Opaque synchronizer token obtained from GET /api/teacher-session and bound to the same authenticated Teacher browser session. The backend validates it together with strict trusted-origin/CORS policy. It must not be placed in a URL, logged, treated as authentication or authorization, or reused as a session, correlation, or command value. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-CSRF": string;
            };
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": {
                    /** @description Opaque identifier for one immutable lesson version. Consumers must not infer storage technology, ordering, ownership, or authorization from its representation. */
                    lessonVersionId: string;
                };
            };
        };
        responses: {
            /** @description Classroom Session created with authoritative revision. */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /** @description Opaque identifier for one Classroom Session. It is not globally meaningful authority; the backend still validates authentication, ownership, participant authority, and session state. */
                        classroomSessionId: string;
                        /** @description Opaque identifier for one immutable lesson version. Consumers must not infer storage technology, ordering, ownership, or authorization from its representation. */
                        lessonVersionId: string;
                        /** @description Backend-authoritative revision that increases monotonically within one Classroom Session and is used for stale/conflict detection and synchronization. It is not globally unique, does not imply an initial value, and is not necessarily equal to an event count. The maximum preserves exact integer representation in browser JavaScript. */
                        revision: number;
                    };
                };
            };
            /** @description The request body is malformed or fails structural validation. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authenticated request is missing valid session-bound CSRF material. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Forbidden",
                     *       "status": 403,
                     *       "detail": "The request did not include valid CSRF protection.",
                     *       "code": "CSRF_REJECTED",
                     *       "correlationId": "corr_csrf_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The LessonVersion does not exist or the authenticated Teacher is not authorized to know it exists. The response deliberately does not distinguish those cases. */
            404: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Not Found",
                     *       "status": 404,
                     *       "detail": "The requested lesson version was not found.",
                     *       "code": "LESSON_VERSION_NOT_FOUND",
                     *       "correlationId": "corr_lesson_lookup_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authorized LessonVersion exists but is not classroom-ready and therefore cannot start a Classroom Session. */
            409: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Conflict",
                     *       "status": 409,
                     *       "detail": "The lesson version is not ready for classroom use.",
                     *       "code": "LESSON_VERSION_NOT_READY",
                     *       "correlationId": "corr_lesson_readiness_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    createPairingGrant: {
        parameters: {
            query?: never;
            header: {
                /** @description Opaque synchronizer token obtained from GET /api/teacher-session and bound to the same authenticated Teacher browser session. The backend validates it together with strict trusted-origin/CORS policy. It must not be placed in a URL, logged, treated as authentication or authorization, or reused as a session, correlation, or command value. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-CSRF": string;
            };
            path: {
                /**
                 * @description Opaque Classroom Session identifier. The backend still verifies the operation's required Teacher or participant authorization and current authoritative session state.
                 * @example classroom_session_example_01
                 */
                classroomSessionId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": {
                    /**
                     * @description Role and purpose to which a PairingGrant or established participant session is bound. The two values have distinct trust boundaries and cannot be interchanged during redemption.
                     * @enum {string}
                     */
                    participantRole: "TEACHER_CONTROLLER" | "CLASSROOM_DISPLAY";
                };
            };
        };
        responses: {
            /** @description PairingGrant created. The pairingToken is the one-time secret and expires at expiresAt, exactly five minutes after issuance. */
            201: {
                headers: {
                    /** @description Prevents storage of security-sensitive response material. */
                    "Cache-Control"?: "no-store";
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /** @description Opaque non-secret identifier for one PairingGrant resource. It exposes no database, ordering, authorization, token, or credential semantics. */
                        pairingGrantId: string;
                        /** @description Opaque high-entropy one-time PairingGrant secret generated by the backend. Clients must not parse, log, place in URLs or query strings, capture in analytics, retain in ordinary history, include in projections or diagnostics, or reuse it after redemption. It is not Teacher authentication or a permanent participant credential. */
                        pairingToken: string;
                        /**
                         * @description Role and purpose to which a PairingGrant or established participant session is bound. The two values have distinct trust boundaries and cannot be interchanged during redemption.
                         * @enum {string}
                         */
                        participantRole: "TEACHER_CONTROLLER" | "CLASSROOM_DISPLAY";
                        /**
                         * Format: date-time
                         * @description RFC 3339 backend machine timestamp normalized to UTC and encoded with a trailing Z. It carries no local-timezone business semantics.
                         */
                        expiresAt: string;
                    };
                };
            };
            /** @description The PairingGrant creation path or request body fails structural validation. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authenticated request is missing valid session-bound CSRF material. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Forbidden",
                     *       "status": 403,
                     *       "detail": "The request did not include valid CSRF protection.",
                     *       "code": "CSRF_REJECTED",
                     *       "correlationId": "corr_csrf_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The Classroom Session does not exist or the authenticated Teacher is not authorized to know it exists. The response deliberately does not distinguish those cases. */
            404: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Not Found",
                     *       "status": 404,
                     *       "detail": "The requested classroom session was not found.",
                     *       "code": "CLASSROOM_SESSION_NOT_FOUND",
                     *       "correlationId": "corr_classroom_lookup_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description Current authoritative Classroom Session state prevents grant issuance, or an active participant already occupies the requested role. Existing participants are never silently replaced. */
            409: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    revokePairingGrant: {
        parameters: {
            query?: never;
            header: {
                /** @description Opaque synchronizer token obtained from GET /api/teacher-session and bound to the same authenticated Teacher browser session. The backend validates it together with strict trusted-origin/CORS policy. It must not be placed in a URL, logged, treated as authentication or authorization, or reused as a session, correlation, or command value. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-CSRF": string;
            };
            path: {
                /**
                 * @description Opaque Classroom Session identifier. The backend still verifies the operation's required Teacher or participant authorization and current authoritative session state.
                 * @example classroom_session_example_01
                 */
                classroomSessionId: string;
                /**
                 * @description Non-secret PairingGrant resource identifier used only for authorized revocation. It is not the one-time pairingToken.
                 * @example pairing_grant_example_01
                 */
                pairingGrantId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description PairingGrant is unusable; no response body is returned. */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description A PairingGrant revocation path identifier is malformed. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authenticated request is missing valid session-bound CSRF material. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Forbidden",
                     *       "status": 403,
                     *       "detail": "The request did not include valid CSRF protection.",
                     *       "code": "CSRF_REJECTED",
                     *       "correlationId": "corr_csrf_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The Classroom Session does not exist or the authenticated Teacher is not authorized to know it exists. The response deliberately does not distinguish those cases. */
            404: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Not Found",
                     *       "status": 404,
                     *       "detail": "The requested classroom session was not found.",
                     *       "code": "CLASSROOM_SESSION_NOT_FOUND",
                     *       "correlationId": "corr_classroom_lookup_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    establishTeacherControllerParticipant: {
        parameters: {
            query?: never;
            header: {
                /** @description Opaque synchronizer token obtained from GET /api/teacher-session and bound to the same authenticated Teacher browser session. The backend validates it together with strict trusted-origin/CORS policy. It must not be placed in a URL, logged, treated as authentication or authorization, or reused as a session, correlation, or command value. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-CSRF": string;
            };
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": {
                    /** @description Opaque high-entropy one-time PairingGrant secret generated by the backend. Clients must not parse, log, place in URLs or query strings, capture in analytics, retain in ordinary history, include in projections or diagnostics, or reuse it after redemption. It is not Teacher authentication or a permanent participant credential. */
                    pairingToken: string;
                };
            };
        };
        responses: {
            /** @description Teacher Controller participant session established. No participant resource read operation or Location header is defined. */
            201: {
                headers: {
                    /**
                     * @description Sets the opaque __Host-penatika-participant cookie. The runtime value is generated by the backend and must use Path=/, Secure, HttpOnly, no Domain attribute, and an appropriate SameSite policy with Strict preferred. Browser JavaScript must not read or parse this header.
                     * @example __Host-penatika-participant=participant_session_example_not_a_secret; Path=/; Secure; HttpOnly; SameSite=Strict
                     */
                    "Set-Cookie"?: string;
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /** @description Opaque identifier for one Classroom Session. It is not globally meaningful authority; the backend still validates authentication, ownership, participant authority, and session state. */
                        classroomSessionId: string;
                        /**
                         * @description Confirms the server-bound Teacher Controller authority.
                         * @constant
                         */
                        participantRole: "TEACHER_CONTROLLER";
                    };
                };
            };
            /** @description The PairingGrant redemption body fails structural validation. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authenticated Controller redemption lacks valid CSRF material or the PairingGrant cannot authorize this Teacher and requested role. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description An active participant already occupies the requested role. The existing participant remains authoritative and is not silently replaced. */
            409: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    getClassroomControllerState: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                /**
                 * @description Opaque Classroom Session identifier. The backend still verifies the operation's required Teacher or participant authorization and current authoritative session state.
                 * @example classroom_session_example_01
                 */
                classroomSessionId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Minimal current authoritative revision state for the authorized Teacher Controller and Classroom Session. */
            200: {
                headers: {
                    /** @description Prevents storage of security-sensitive response material. */
                    "Cache-Control"?: "no-store";
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "classroomSessionId": "classroom_session_example_01",
                     *       "revision": 7
                     *     }
                     */
                    "application/json": {
                        /** @description Opaque identifier for one Classroom Session. It is not globally meaningful authority; the backend still validates authentication, ownership, participant authority, and session state. */
                        classroomSessionId: string;
                        /** @description Current backend-authoritative Classroom Session revision used to replace the Controller's cached revision context before it creates a new command. It is not mutation authorization, scene position, Display synchronization proof, an event count, or a timestamp. */
                        revision: number;
                    };
                };
            };
            /** @description The Controller reconciliation Classroom Session path identifier is malformed. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Bad Request",
                     *       "status": 400,
                     *       "detail": "One or more request fields are invalid.",
                     *       "code": "REQUEST_VALIDATION_FAILED",
                     *       "correlationId": "corr_controller_state_validation_01",
                     *       "fieldErrors": [
                     *         {
                     *           "field": "classroomSessionId",
                     *           "code": "INVALID_VALUE",
                     *           "message": "The field value does not satisfy its contract."
                     *         }
                     *       ]
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The authenticated Teacher request lacks a current active TEACHER_CONTROLLER participant session bound to the path Classroom Session. Missing, invalid, expired, revoked, replaced, wrong-role, or wrong-session participant authority shares this non-disclosing outcome. The response does not reveal another participant, role-slot owner, Classroom Session, or Teacher. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Forbidden",
                     *       "status": 403,
                     *       "detail": "Active Controller authority is required for this request.",
                     *       "code": "CONTROLLER_AUTHORITY_REQUIRED",
                     *       "correlationId": "corr_controller_state_authority_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The Classroom Session does not exist or the authenticated Teacher is not authorized to know it exists. The response deliberately does not distinguish those cases. */
            404: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Not Found",
                     *       "status": 404,
                     *       "detail": "The requested classroom session was not found.",
                     *       "code": "CLASSROOM_SESSION_NOT_FOUND",
                     *       "correlationId": "corr_classroom_lookup_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
    submitClassroomSessionCommand: {
        parameters: {
            query?: never;
            header: {
                /** @description Opaque synchronizer token obtained from GET /api/teacher-session and bound to the same authenticated Teacher browser session. The backend validates it together with strict trusted-origin/CORS policy. It must not be placed in a URL, logged, treated as authentication or authorization, or reused as a session, correlation, or command value. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-CSRF": string;
            };
            path: {
                /**
                 * @description Opaque Classroom Session identifier. The backend still verifies the operation's required Teacher or participant authorization and current authoritative session state.
                 * @example classroom_session_example_01
                 */
                classroomSessionId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": {
                    /** @description Caller-supplied opaque identity created before the first attempt of one logical state-changing command. Clients should generate collision-resistant values without relying on a UUID or other client-parseable format. Idempotency is scoped by (ClassroomSessionId, CommandId), and the same textual value in a different Classroom Session has no semantic relationship. The value remains stable only for an equivalent retry or uncertain-acknowledgement reconciliation. It is not globally unique authority, a correlation identifier, authentication, authorization, or a credential. Accepted command identity and its original outcome remain reconcilable for at least the active/reconcilable lifetime of the owning Classroom Session; exact post-session persistence and cleanup remain deferred. */
                    commandId: string;
                    /** @description Authoritative Classroom Session revision the Controller believes it is acting against. For an unseen command it must equal the current backend-authoritative revision. For an equivalent replay of an accepted command it remains the original value used to establish logical request equivalence. */
                    expectedRevision: number;
                    /**
                     * @description Explicitly selects the deterministic, non-generative direct-action command class. It does not enter AI generation or publication.
                     * @constant
                     */
                    commandType: "DIRECT_ACTION";
                    /**
                     * @description Advances the authoritative lesson or scene position using the only direct action supported by this contract slice.
                     * @constant
                     */
                    action: "NEXT";
                };
            };
        };
        responses: {
            /** @description Command accepted or an equivalent previously accepted command replayed. Both cases return that command's original authoritative outcome. resultingRevision is the revision immediately resulting from the accepted command and might no longer be the Classroom Session's latest revision when an older command is replayed. */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /** @description Opaque identifier for one Classroom Session. It is not globally meaningful authority; the backend still validates authentication, ownership, participant authority, and session state. */
                        classroomSessionId: string;
                        /** @description Caller-supplied opaque identity created before the first attempt of one logical state-changing command. Clients should generate collision-resistant values without relying on a UUID or other client-parseable format. Idempotency is scoped by (ClassroomSessionId, CommandId), and the same textual value in a different Classroom Session has no semantic relationship. The value remains stable only for an equivalent retry or uncertain-acknowledgement reconciliation. It is not globally unique authority, a correlation identifier, authentication, authorization, or a credential. Accepted command identity and its original outcome remain reconcilable for at least the active/reconcilable lifetime of the owning Classroom Session; exact post-session persistence and cleanup remain deferred. */
                        commandId: string;
                        /** @description Authoritative Classroom Session revision immediately resulting from this accepted command. It is greater than the command's expectedRevision for a newly accepted command, but is not guaranteed to equal expectedRevision plus one. Equivalent replay returns this original value even if the session later advanced further. */
                        resultingRevision: number;
                    };
                };
            };
            /** @description The Classroom Session command path or request body fails structural validation. Only DIRECT_ACTION with action NEXT is supported. */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description No valid authenticated Teacher browser session is available. Missing, expired, revoked, or otherwise invalid sessions and sessions whose TeacherAccount is no longer active use the same non-disclosing outcome. */
            401: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Unauthorized",
                     *       "status": 401,
                     *       "detail": "An authenticated Teacher session is required.",
                     *       "code": "TEACHER_SESSION_REQUIRED",
                     *       "correlationId": "corr_teacher_session_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The request lacks valid CSRF material or active TEACHER_CONTROLLER participant authority for the path Classroom Session. The response does not disclose participant identity, role-slot ownership, another Classroom Session, or another Teacher. */
            403: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description The Classroom Session does not exist or the authenticated Teacher is not authorized to know it exists. The response deliberately does not distinguish those cases. */
            404: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example {
                     *       "type": "about:blank",
                     *       "title": "Not Found",
                     *       "status": 404,
                     *       "detail": "The requested classroom session was not found.",
                     *       "code": "CLASSROOM_SESSION_NOT_FOUND",
                     *       "correlationId": "corr_classroom_lookup_01"
                     *     }
                     */
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
            /** @description Current authoritative command identity, revision, or mutation state conflicts with the request. No new mutation occurs. STALE_REVISION does not include currentRevision or a projection in Problem Details; reconciliation uses the Controller-authorized controller-state read before a new command is created. This contract does not grant Controller participants access to the classroom-safe Display snapshot or Display SSE projection. Under the current closed command schema, COMMAND_ID_REUSE_CONFLICT is reachable when an accepted CommandId is reused with a different expectedRevision; unsupported commandType or action values remain REQUEST_VALIDATION_FAILED. */
            409: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/problem+json": {
                        /**
                         * Format: uri-reference
                         * @description RFC 9457 problem type URI reference. Use about:blank when no more specific registered problem type applies.
                         */
                        type: string;
                        /** @description Short human-readable summary for the problem type. Clients must not parse this value for application logic. */
                        title: string;
                        /** @description HTTP status code generated for this occurrence. It mirrors the HTTP response status and does not replace it. */
                        status: number;
                        /** @description Optional safe human-readable explanation specific to this occurrence. It must not expose internal or sensitive diagnostics. */
                        detail?: string;
                        /**
                         * Format: uri-reference
                         * @description Optional RFC 9457 URI reference identifying this problem occurrence. It is not an authorization or resource-ownership guarantee.
                         */
                        instance?: string;
                        /** @description Required machine-stable Penatika error discriminator. Values are introduced by later operation contracts; clients must not parse title or detail for logic. */
                        code: string;
                        /** @description Privacy-safe operational reference used to correlate a request or cross-boundary workflow. It is not a business identifier, CommandId, session credential, authorization input, or idempotency guarantee. */
                        correlationId?: string;
                        /** @description Optional bounded request-validation details. A server may report a safe subset when more than 20 fields fail; entries never contain rejected values or internal diagnostics. */
                        fieldErrors?: {
                            /** @description Client-visible request field name or field path. It must not contain a rejected value, credential, or raw request content. */
                            field: string;
                            /** @description Machine-stable validation discriminator. Allowed values are defined by the operation contracts that use this structure; clients must not parse message for logic. */
                            code: string;
                            /** @description Safe human-readable validation explanation. It must not expose raw values, stack traces, exception names, SQL, provider payloads, credentials, or private diagnostics. */
                            message: string;
                        }[];
                    } & {
                        [key: string]: unknown;
                    };
                };
            };
        };
    };
}
