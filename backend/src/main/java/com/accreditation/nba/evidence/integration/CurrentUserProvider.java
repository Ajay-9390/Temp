package com.accreditation.nba.evidence.integration;

/**
 * Supplies the acting user's identity. This is the single seam that will be replaced when
 * real authentication (JWT/Keycloak) is introduced. During this development phase there is
 * no auth: a mock identity is returned (optionally from request headers).
 */
public interface CurrentUserProvider {

    /** Stable identifier of the current user (e.g. "mock-user"). Never null. */
    String currentUserId();

    /** Display name of the current user. Never null. */
    String currentUserName();
}
