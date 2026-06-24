package dev.opinion2nd.antiespguard.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Anti seed-cracker helper.
 *
 * <p>The server sends a hashed world seed inside the login/respawn packets.
 * Seed-cracker tools (e.g. SeedcrackerX) use that hash purely as a verification
 * oracle for reverse-engineered candidates. Replacing it with a stable random
 * value removes the oracle without any gameplay effect — the client only uses
 * the seed for cosmetic biome-border noise.</p>
 */
public final class SeedScrambler {

    private SeedScrambler() {
    }

    /** A fresh random replacement seed hash. */
    public static long randomSeed() {
        return ThreadLocalRandom.current().nextLong();
    }
}
