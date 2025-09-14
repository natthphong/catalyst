/*
 * Copyright (c) 2568. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.core;

public interface CatalystServerConfig {
    /** Port to bind. */
    default int port() { return 8080; }

    /** ServerSocket backlog. */
    default int backlog() { return 128; }

    /** Max readable HTTP request body in bytes. */
    default int maxRequestSize() { return 16 * 1024 * 1024; } // 16MB

    /** Core worker threads. */
    default int coreThreads() { return Math.max(2, Runtime.getRuntime().availableProcessors()); }

    /** Max worker threads for bursts. */
    default int maxThreads() { return Math.max(coreThreads() * 4, 32); }

    /** Idle keep-alive (seconds) for worker threads. */
    default long keepAliveSeconds() { return 60L; }

    /** Optional: read timeout millis (0 = no SO_TIMEOUT). */
    default int socketReadTimeoutMs() { return 0; }
}
