-- ═════════════════════════════════════════════════════════════════════════════
-- UltimateDungeon  |  V1__initial_schema.sql
-- Initial database schema. Applied automatically on first run.
-- Compatible with both SQLite and MySQL.
-- ═════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS ud_player_stats (
    uuid              VARCHAR(36)   NOT NULL PRIMARY KEY,
    player_name       VARCHAR(16)   NOT NULL,
    dungeons_completed INT          NOT NULL DEFAULT 0,
    bosses_defeated    INT          NOT NULL DEFAULT 0,
    monsters_killed    INT          NOT NULL DEFAULT 0,
    death_count        INT          NOT NULL DEFAULT 0,
    fastest_run_ms     BIGINT                DEFAULT NULL,
    highest_difficulty VARCHAR(32)           DEFAULT NULL,
    rewards_earned     INT          NOT NULL DEFAULT 0,
    first_seen         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ud_dungeon_records (
    id              @AUTO_PK@,
    player_uuid     VARCHAR(36)   NOT NULL,
    theme           VARCHAR(32)   NOT NULL,
    difficulty      VARCHAR(32)   NOT NULL,
    completed       BOOLEAN       NOT NULL DEFAULT FALSE,
    duration_ms     BIGINT                 DEFAULT NULL,
    party_size      INT           NOT NULL DEFAULT 1,
    boss_killed     VARCHAR(64)            DEFAULT NULL,
    completed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_uuid) REFERENCES ud_player_stats(uuid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ud_reward_log (
    id              @AUTO_PK@,
    player_uuid     VARCHAR(36)   NOT NULL,
    reward_type     VARCHAR(32)   NOT NULL,
    reward_event    VARCHAR(32)   NOT NULL,
    dungeon_id      BIGINT                 DEFAULT NULL,
    collected       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_uuid) REFERENCES ud_player_stats(uuid) ON DELETE CASCADE,
    FOREIGN KEY (dungeon_id)  REFERENCES ud_dungeon_records(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ud_party_log (
    id              @AUTO_PK@,
    leader_uuid     VARCHAR(36)   NOT NULL,
    member_uuids    TEXT          NOT NULL,
    dungeon_id      BIGINT                 DEFAULT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disbanded_at    TIMESTAMP              DEFAULT NULL,
    FOREIGN KEY (dungeon_id) REFERENCES ud_dungeon_records(id) ON DELETE SET NULL
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_dungeon_records_player ON ud_dungeon_records(player_uuid);
CREATE INDEX IF NOT EXISTS idx_dungeon_records_theme  ON ud_dungeon_records(theme);
CREATE INDEX IF NOT EXISTS idx_reward_log_player      ON ud_reward_log(player_uuid);
CREATE INDEX IF NOT EXISTS idx_reward_log_collected   ON ud_reward_log(collected);
