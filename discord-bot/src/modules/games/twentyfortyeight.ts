import { ActionRowBuilder, ButtonBuilder, ButtonStyle, EmbedBuilder } from "discord.js";

export interface Game2048 {
  id: string;
  playerId: string;
  grid: number[][]; // 4x4, 0 = empty
  score: number;
  over: boolean;
}

const SIZE = 4;
const games = new Map<string, Game2048>();

function spawnTile(grid: number[][]) {
  const empty: [number, number][] = [];
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      if (grid[r][c] === 0) empty.push([r, c]);
    }
  }
  if (empty.length === 0) return;
  const [r, c] = empty[Math.floor(Math.random() * empty.length)];
  grid[r][c] = Math.random() < 0.9 ? 2 : 4;
}

export function createGame2048(playerId: string): Game2048 {
  const id = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const grid = Array.from({ length: SIZE }, () => new Array(SIZE).fill(0));
  spawnTile(grid);
  spawnTile(grid);
  const game: Game2048 = { id, playerId, grid, score: 0, over: false };
  games.set(id, game);
  return game;
}

export function getGame2048(id: string): Game2048 | undefined {
  return games.get(id);
}

function collapseLine(line: number[]): { line: number[]; scoreGained: number; moved: boolean } {
  const original = [...line];
  const values = line.filter((v) => v !== 0);
  const merged: number[] = [];
  let scoreGained = 0;
  for (let i = 0; i < values.length; i++) {
    if (i < values.length - 1 && values[i] === values[i + 1]) {
      const mergedValue = values[i] * 2;
      merged.push(mergedValue);
      scoreGained += mergedValue;
      i++;
    } else {
      merged.push(values[i]);
    }
  }
  while (merged.length < SIZE) merged.push(0);
  const moved = original.some((v, i) => v !== merged[i]);
  return { line: merged, scoreGained, moved };
}

function rotateGrid(grid: number[][]): number[][] {
  const result = Array.from({ length: SIZE }, () => new Array(SIZE).fill(0));
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      result[c][SIZE - 1 - r] = grid[r][c];
    }
  }
  return result;
}

export type Direction = "up" | "down" | "left" | "right";

export function move2048(id: string, userId: string, direction: Direction): { error?: string } {
  const game = games.get(id);
  if (!game) return { error: "This game no longer exists." };
  if (game.playerId !== userId) return { error: "This isn't your game." };
  if (game.over) return { error: "This game has already ended." };

  let grid = game.grid;
  let rotations = 0;
  if (direction === "up") rotations = 3;
  else if (direction === "right") rotations = 2;
  else if (direction === "down") rotations = 1;

  for (let i = 0; i < rotations; i++) grid = rotateGrid(grid);

  let anyMoved = false;
  let totalScore = 0;
  const newGrid = grid.map((row) => {
    const { line, scoreGained, moved } = collapseLine(row);
    if (moved) anyMoved = true;
    totalScore += scoreGained;
    return line;
  });

  let result = newGrid;
  for (let i = 0; i < (4 - rotations) % 4; i++) result = rotateGrid(result);

  if (!anyMoved) return { error: "That move doesn't change the board." };

  game.grid = result;
  game.score += totalScore;
  spawnTile(game.grid);

  if (!hasMovesAvailable(game.grid)) game.over = true;
  return {};
}

function hasMovesAvailable(grid: number[][]): boolean {
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      if (grid[r][c] === 0) return true;
      if (c < SIZE - 1 && grid[r][c] === grid[r][c + 1]) return true;
      if (r < SIZE - 1 && grid[r][c] === grid[r + 1][c]) return true;
    }
  }
  return false;
}

const TILE_EMOJI: Record<number, string> = {
  0: "⬛",
  2: "2️⃣",
  4: "4️⃣",
};

function renderTile(value: number): string {
  if (value === 0) return "⬜";
  return TILE_EMOJI[value] ?? `\`${value}\``;
}

export function buildGame2048Embed(game: Game2048) {
  const board = game.grid.map((row) => row.map(renderTile).join(" ")).join("\n");
  const embed = new EmbedBuilder().setTitle("2048").setDescription(board).setColor(0x5865f2).addFields({ name: "Score", value: String(game.score) });
  if (game.over) embed.setFooter({ text: "Game over!" });
  return embed;
}

export function buildGame2048Rows(game: Game2048) {
  const disabled = game.over;
  const row1 = new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(`g2048_${game.id}_up`).setLabel("⬆️").setStyle(ButtonStyle.Secondary).setDisabled(disabled)
  );
  const row2 = new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(`g2048_${game.id}_left`).setLabel("⬅️").setStyle(ButtonStyle.Secondary).setDisabled(disabled),
    new ButtonBuilder().setCustomId(`g2048_${game.id}_down`).setLabel("⬇️").setStyle(ButtonStyle.Secondary).setDisabled(disabled),
    new ButtonBuilder().setCustomId(`g2048_${game.id}_right`).setLabel("➡️").setStyle(ButtonStyle.Secondary).setDisabled(disabled)
  );
  return [row1, row2];
}
