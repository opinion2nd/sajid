package com.ultimatedungeon.database.schema;

/**
 * Marker class for the schema package.
 *
 * <p>SQL migration scripts are located in
 * {@code src/main/resources/database/schema/} and loaded at runtime
 * by {@link com.ultimatedungeon.database.MigrationRunner} via
 * {@link Class#getResourceAsStream(String)}.</p>
 *
 * <p>Naming convention: {@code V{version}__{description}.sql}</p>
 */
public final class SchemaResources {
    private SchemaResources() {}

    /** Resource path prefix for all migration scripts. */
    public static final String SCHEMA_PATH = "/database/schema/";
}
