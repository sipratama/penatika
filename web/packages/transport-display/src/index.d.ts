export interface paths {
    "/api/classroom-display-participants": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Establish a Classroom Display participant session
         * @description Redeems a valid CLASSROOM_DISPLAY PairingGrant and creates a distinct role-bound participant browser session. Teacher authentication and CSRF material are not required because the grant authorizes only the bounded Display participant establishment; it never grants Teacher authority. An existing active Display is not silently replaced.
         */
        post: operations["establishClassroomDisplayParticipant"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/display-snapshot": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve the authoritative Classroom Display snapshot
         * @description Returns the complete current classroom-safe projection at one backend-authoritative Classroom Session revision. The caller must hold a currently active, non-revoked CLASSROOM_DISPLAY participant browser session bound to the path Classroom Session. A TEACHER_CONTROLLER participant, TeacherBrowserSession, pairing credential, cached state, or knowledge of the ClassroomSessionId does not authorize this operation. The backend validates current participant role, binding, revocation/replacement state, and Classroom Session accessibility on every request.
         *     This response is a full state-replacement boundary. After validating the canonical standalone schema, the Display replaces its cached projection and adopts the returned revision. The snapshot always represents current backend authority at response time; it does not regress to the historical resultingRevision of an earlier command replay. Retrieving the snapshot alone does not restore student-facing mutation eligibility. When used for reconciliation with an active authorized SSE stream, the Display must validate and apply it as full replacement, then acknowledge its revision through the display-synchronization operation. No CSRF material is required because this GET is safe and read-only.
         */
        get: operations["getClassroomDisplaySnapshot"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/display-events": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Stream authoritative Classroom Display projections
         * @description Establishes the one-way Classroom Display Server-Sent Events stream. The caller must hold a currently active, non-revoked, non-replaced CLASSROOM_DISPLAY participant browser session bound to the path Classroom Session. A TEACHER_CONTROLLER participant, TeacherBrowserSession, pairing credential, cached state, Last-Event-ID, or knowledge of the ClassroomSessionId does not authorize this operation. The backend validates current participant role, binding, revocation/replacement state, and Classroom Session accessibility before establishing the stream. No CSRF material is required because this GET is safe and read-only.
         *     On every successful initial or reconnecting request, the first state-bearing event is the complete current backend-authoritative Display projection. This applies when Last-Event-ID is absent, behind, equal to, or ahead of the current backend revision. The backend never trusts the client-supplied value over current authority, replays historical events, or promises delta catch-up. Receiving, validating against the canonical standalone Classroom Display projection schema, and applying that event as a full replacement is the client reconciliation barrier. EventSource open alone is not synchronization, and successful server dispatch alone does not prove browser receipt, schema validation, or local application. After receiving, validating, and applying the complete projection, the Display explicitly acknowledges that revision through PUT /api/classroom-sessions/{classroomSessionId}/display-synchronization. The backend restores eligibility for a new student-facing mutation only after accepting that acknowledgement for the current revision on the participant's current active authorized stream. The acknowledgement is application-level reconciliation evidence, not proof that pixels were rendered.
         *     Each state-bearing event is named display-projection. Its SSE id is the base-10 decimal representation of data.revision, and its data is JSON that validates against ../schemas/classroom-display-projection.schema.json. Within one active stream, later state-bearing events follow authoritative session order and must not carry a lower revision; duplicate same-revision full projections are safe and idempotent. Revision values may be skipped because not every future authoritative revision must change the Display projection. Accepted commands remain synchronous HTTP operations, and this stream carries resulting projections only.
         *     Heartbeats, when emitted, are comment-only SSE frames such as ": keep-alive". They have no event name, id, projection, revision, or synchronization meaning and do not update Last-Event-ID. No SSE retry field or heartbeat interval is contracted. After a successful stream response begins, Problem Details are never sent as SSE data. If participant authority becomes invalid or the stream cannot continue safely, the backend stops emitting state and terminates the stream; a later reconnect request is authorized again through the normal HTTP boundary. Stream termination invalidates the accepted synchronization state, so the reconnected Display must receive, validate, apply, and acknowledge the current full projection again.
         */
        get: operations["streamClassroomDisplayEvents"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/classroom-sessions/{classroomSessionId}/display-synchronization": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Acknowledge current Classroom Display synchronization
         * @description Updates the current Display participant's synchronization state after the Display has received a complete authoritative projection, validated it against the canonical ClassroomDisplayProjection schema, and applied it as full replacement. The request requires only an active, non-revoked, non-replaced CLASSROOM_DISPLAY ParticipantBrowserSession bound to the path Classroom Session. It does not require or confer TeacherBrowserSession, TeacherAccountId, PairingGrantToken, command authority, or Teacher privilege. A TEACHER_CONTROLLER participant does not authorize this operation.
         *     The acknowledgement is accepted only when the supplied revision equals the current authoritative Display projection revision, an active authorized Display SSE stream exists for the same participant, and that exact complete projection revision was successfully dispatched on that participant's current active stream. After authentication, authorization, CSRF, and Classroom Session accessibility checks pass, stale, ahead, undispatched, or post-termination acknowledgements cannot restore synchronization and share one non-disclosing conflict response. Revoked or replaced participant credentials fail the normal participant session or authority checks instead. An equivalent retry of an already acknowledged current revision is idempotent and returns the same success without another classroom mutation.
         *     Acceptance restores the Display synchronization gate for the current revision and active stream only. It does not advance the Classroom Session revision or mutate lesson/content state. The acknowledgement no longer satisfies the gate when a new authoritative student-facing mutation advances the Display projection revision, the SSE stream terminates, the participant is revoked or replaced, the Classroom Session ends, or current participant authority is otherwise lost. A reconnect requires a fresh acknowledgement after the current full projection is received, validated, and applied. Acceptance proves application-level reconciliation of the revision, not visual pixel rendering.
         */
        put: operations["acknowledgeClassroomDisplaySynchronization"];
        post?: never;
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
    establishClassroomDisplayParticipant: {
        parameters: {
            query?: never;
            header?: never;
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
            /** @description Classroom Display participant session established. No participant resource read operation or Location header is defined. */
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
                         * @description Confirms the server-bound Classroom Display authority.
                         * @constant
                         */
                        participantRole: "CLASSROOM_DISPLAY";
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
            /** @description The supplied PairingGrant cannot establish the requested participant. Unknown, expired, consumed, revoked, replayed, wrong-role, ended-session, and otherwise invalid grants share this non-disclosing outcome. */
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
    getClassroomDisplaySnapshot: {
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
            /** @description Complete current classroom-safe authoritative Display projection. */
            200: {
                headers: {
                    /** @description Prevents storage of security-sensitive response material. */
                    "Cache-Control"?: "no-store";
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        /**
                         * @description Version of the complete Classroom Display projection shape, including its scene and block union. A new field, changed field meaning, or newly supported block type requires deliberate version evolution and consumer rollout because this safety boundary is closed.
                         * @constant
                         */
                        schemaVersion: "1.0";
                        /** @description Opaque identifier for one Classroom Session. It is not globally meaningful authority; the backend still validates authentication, ownership, participant authority, and session state. */
                        classroomSessionId: string;
                        /** @description Current backend-authoritative Classroom Session revision represented by this complete projection. It is not a scene position, SSE sequence, command count, database row version, or timestamp. */
                        revision: number;
                        scene: {
                            /** @description Opaque stable identity of the current scene within the Classroom Session's immutable LessonVersion. Consumers must not infer database identity, ordering, authorization, or revision from it. */
                            sceneId: string;
                            /** @description Zero-based ordinal of the current scene within the immutable LessonVersion selected for this Classroom Session. It is deterministic for that LessonVersion, is not a total-scene count, and is independent of the Classroom Session revision. */
                            position: number;
                            /** @description Ordered allow-listed blocks required to render the current classroom scene. Unsupported block types fail validation rather than becoming executable or generic payloads. */
                            blocks: {
                                /**
                                 * @description The only block type supported by projection schema version 1.0.
                                 * @constant
                                 */
                                type: "PLAIN_TEXT";
                                /** @description Classroom-facing plain text rendered as data with normal output encoding. It is not Markdown, HTML, a template, code, script, or another executable representation. */
                                text: string;
                            }[];
                        };
                        $defs: {
                            ClassroomDisplayScene: {
                                /** @description Opaque stable identity of the current scene within the Classroom Session's immutable LessonVersion. Consumers must not infer database identity, ordering, authorization, or revision from it. */
                                sceneId: string;
                                /** @description Zero-based ordinal of the current scene within the immutable LessonVersion selected for this Classroom Session. It is deterministic for that LessonVersion, is not a total-scene count, and is independent of the Classroom Session revision. */
                                position: number;
                                /** @description Ordered allow-listed blocks required to render the current classroom scene. Unsupported block types fail validation rather than becoming executable or generic payloads. */
                                blocks: {
                                    /**
                                     * @description The only block type supported by projection schema version 1.0.
                                     * @constant
                                     */
                                    type: "PLAIN_TEXT";
                                    /** @description Classroom-facing plain text rendered as data with normal output encoding. It is not Markdown, HTML, a template, code, script, or another executable representation. */
                                    text: string;
                                }[];
                            };
                            PlainTextBlock: {
                                /**
                                 * @description The only block type supported by projection schema version 1.0.
                                 * @constant
                                 */
                                type: "PLAIN_TEXT";
                                /** @description Classroom-facing plain text rendered as data with normal output encoding. It is not Markdown, HTML, a template, code, script, or another executable representation. */
                                text: string;
                            };
                        };
                    };
                };
            };
            /** @description The Display snapshot path identifier is malformed. */
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
                     *       "correlationId": "corr_display_snapshot_validation_01",
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
            /** @description No usable participant browser session is available for Display access. Missing, invalid, expired, revoked, replaced, or otherwise unusable participant credentials share this non-disclosing outcome. */
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
                     *       "detail": "An active Classroom Display participant session is required.",
                     *       "code": "DISPLAY_SESSION_REQUIRED",
                     *       "correlationId": "corr_display_session_01"
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
            /** @description The participant browser session is valid but does not carry the CLASSROOM_DISPLAY authority required by this operation. The response does not reveal the participant identity, its actual role, another Classroom Session, or another Teacher. */
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
                     *       "detail": "Active Classroom Display authority is required for this request.",
                     *       "code": "DISPLAY_AUTHORITY_REQUIRED",
                     *       "correlationId": "corr_display_authority_01"
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
            /** @description The requested Classroom Session does not exist or is not accessible to the active Display participant. Foreign and nonexistent session identifiers share this non-disclosing outcome. */
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
                     *       "correlationId": "corr_display_classroom_lookup_01"
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
    streamClassroomDisplayEvents: {
        parameters: {
            query?: never;
            header?: {
                /**
                 * @description Last successfully observed Display SSE event id for this Classroom Session, parsed using the canonical Revision schema. The backend emits each SSE id in canonical unsigned base-10 decimal form, which native EventSource reconnect may echo automatically. No additional lexical identity rule is imposed on manually constructed header text beyond successful parsing as a Revision; numeric Revision meaning, not textual spelling, controls reconciliation. The value is advisory context only: it is not authentication, authorization, mutation permission, expectedRevision, proof of client ownership, or a request for historical state. A valid value that is behind, equal to, or ahead of backend authority still yields the current full authoritative projection. A value that cannot be parsed within the Revision bounds is rejected with REQUEST_VALIDATION_FAILED before stream establishment.
                 * @example 5
                 */
                "Last-Event-ID"?: number;
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
        requestBody?: never;
        responses: {
            /** @description Authorized Server-Sent Events stream. OpenAPI represents the stream entity truthfully as text rather than as one JSON response object. Each display-projection event's data field is independently a JSON Classroom Display projection governed by ../schemas/classroom-display-projection.schema.json, and the event id must equal that projection's revision in decimal form. */
            200: {
                headers: {
                    /** @description Prevents storage of security-sensitive response material. */
                    "Cache-Control"?: "no-store";
                    [name: string]: unknown;
                };
                content: {
                    /**
                     * @example id: 5
                     *     event: display-projection
                     *     data: {"schemaVersion":"1.0","classroomSessionId":"classroom_session_example_01","revision":5,"scene":{"sceneId":"scene_example_02","position":1,"blocks":[{"type":"PLAIN_TEXT","text":"Bandingkan 1/2 dan 3/4."}]}}
                     */
                    "text/event-stream": string;
                };
            };
            /** @description The Display events path identifier or Last-Event-ID header is malformed or outside its contract bounds. Validation fails before an SSE stream is established. */
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
            /** @description No usable participant browser session is available for Display access. Missing, invalid, expired, revoked, replaced, or otherwise unusable participant credentials share this non-disclosing outcome. */
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
                     *       "detail": "An active Classroom Display participant session is required.",
                     *       "code": "DISPLAY_SESSION_REQUIRED",
                     *       "correlationId": "corr_display_session_01"
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
            /** @description The participant browser session is valid but does not carry the CLASSROOM_DISPLAY authority required by this operation. The response does not reveal the participant identity, its actual role, another Classroom Session, or another Teacher. */
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
                     *       "detail": "Active Classroom Display authority is required for this request.",
                     *       "code": "DISPLAY_AUTHORITY_REQUIRED",
                     *       "correlationId": "corr_display_authority_01"
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
            /** @description The requested Classroom Session does not exist or is not accessible to the active Display participant. Foreign and nonexistent session identifiers share this non-disclosing outcome. */
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
                     *       "correlationId": "corr_display_classroom_lookup_01"
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
    acknowledgeClassroomDisplaySynchronization: {
        parameters: {
            query?: never;
            header: {
                /** @description Fixed non-secret custom-header guard for this cookie-authenticated Display state-changing request. The backend validates the value together with strict trusted-origin/CORS policy to prevent ordinary ambient cross-site form submission. It is not authentication, authorization, a session credential, a pairing secret, command authority, or revision authority. Missing or invalid material is rejected with CSRF_REJECTED. */
                "X-Penatika-Display-Intent": "synchronize";
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
                /**
                 * @example {
                 *       "revision": 5
                 *     }
                 */
                "application/json": {
                    /** @description Current authoritative Display projection revision that this Display received, schema-validated, and applied as full replacement on its current active SSE stream. It is not expectedRevision, command authority, Last-Event-ID history authority, event identity beyond this acknowledgement check, or proof of rendered pixels. */
                    revision: number;
                };
            };
        };
        responses: {
            /** @description Synchronization acknowledged for the current authoritative Display projection revision on the participant's current active SSE stream. No Classroom Session revision or lesson/content state is changed. */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description The Display synchronization path or request body fails structural validation. */
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
            /** @description No usable participant browser session is available for Display access. Missing, invalid, expired, revoked, replaced, or otherwise unusable participant credentials share this non-disclosing outcome. */
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
                     *       "detail": "An active Classroom Display participant session is required.",
                     *       "code": "DISPLAY_SESSION_REQUIRED",
                     *       "correlationId": "corr_display_session_01"
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
            /** @description The request lacks the required Display custom-header CSRF guard or the valid participant browser session does not carry active same-session CLASSROOM_DISPLAY authority. The response does not disclose the actual participant role, participant identity, another Classroom Session, or another Teacher. */
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
            /** @description The requested Classroom Session does not exist or is not accessible to the active Display participant. Foreign and nonexistent session identifiers share this non-disclosing outcome. */
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
                     *       "correlationId": "corr_display_classroom_lookup_01"
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
            /** @description The acknowledgement does not match the current authorized Display stream and projection state. Stale, ahead, non-current, undispatched, post-termination, wrong-session, or otherwise mismatched acknowledgements share this non-disclosing response. The response does not expose the current revision, participant identity, actual role, or stream internals, and synchronization is not restored. */
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
                     *       "detail": "The Display synchronization acknowledgement is not valid for current state.",
                     *       "code": "DISPLAY_SYNCHRONIZATION_CONFLICT",
                     *       "correlationId": "corr_display_sync_conflict_01"
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
}
