-- ═════════════════════════════════════════════════════════════════════════════
-- UltimateDungeon  |  V2__add_statistics.sql
-- Adds additional statistics columns introduced in version 1.1.
-- ═════════════════════════════════════════════════════════════════════════════

ALTER TABLE ud_player_stats ADD COLUMN IF NOT EXISTS traps_triggered  INT NOT NULL DEFAULT 0;
ALTER TABLE ud_player_stats ADD COLUMN IF NOT EXISTS puzzles_solved    INT NOT NULL DEFAULT 0;
ALTER TABLE ud_player_stats ADD COLUMN IF NOT EXISTS secrets_found     INT NOT NULL DEFAULT 0;
ALTER TABLE ud_player_stats ADD COLUMN IF NOT EXISTS waves_completed   INT NOT NULL DEFAULT 0;
