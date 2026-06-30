package com.ultimatedungeon.compat;

/**
 * Provides Folia-compatible scheduling where thread-safe operations are required.
 *
 * <p>Used only when {@link VersionDetector#isFolia()} returns true.
 * Folia requires region-aware task scheduling — global scheduler for
 * non-world tasks, entity scheduler for entity-bound tasks.</p>
 */
public final class FoliaCompatAdapter {
    // Implemented in Milestone 1 — Phase 1 skeleton.
}
