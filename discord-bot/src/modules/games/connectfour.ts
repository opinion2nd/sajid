import { ActionRowBuilder, ButtonBuilder, ButtonStyle, EmbedBuilder } from "discord.js";

export interface ConnectFourGame {
  id: string;
  grid: (0 | 1 | null)[][]; // 6 rows x 7 cols, grid[row][col]
  players: [string, string];
  turn: 0 | 1;
  winner: 0 | 1 | "draw" | null;
}

const ROWS = 6;
const COLS = 7;
const games = new Map<string, ConnectFourGame>();

export function createConnectFourGame(player1: string, player2: string): ConnectFourGame {
  const id = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const grid: (0 | 1 | null)[][] = Array.from({ length: ROWS }, () => new Array(COLS).fill(null));
  const game: ConnectFourGame = { id, grid, players: [player1, player2], turn: 0, winner: null };
  games.set(id, game);
  return game;
}

export function getConnectFourGame(id: string): ConnectFourGame | undefined {
  return games.get(id);
}

function checkWinner(grid: (0 | 1 | null)[][], row: number, col: number, player: 0 | 1): boolean {
  const directions = [
    [0, 1],
    [1, 0],
    [1, 1],
    [1, -1],
  ];
  for (const [dr, dc] of directions) {
    let count = 1;
    for (const sign of [1, -1]) {
      let r = row + dr * sign;
      let c = col + dc * sign;
      while (r >= 0 && r < ROWS && c >= 0 && c < COLS && grid[r][c] === player) {
        count++;
        r += dr * sign;
        c += dc * sign;
      }
    }
    if (count >= 4) return true;
  }
  return false;
}

export function playConnectFourMove(id: string, userId: string, col: number): { error?: string } {
  const game = games.get(id);
  if (!game) return { error: "This game no longer exists." };
  if (game.winner) return { error: "This game has already ended." };
  if (game.players[game.turn] !== userId) return { error: "It's not your turn." };

  let row = -1;
  for (let r = ROWS - 1; r >= 0; r--) {
    if (game.grid[r][col] === null) {
      row = r;
      break;
    }
  }
  if (row === -1) return { error: "That column is full." };

  game.grid[row][col] = game.turn;

  if (checkWinner(game.grid, row, col, game.turn)) {
    game.winner = game.turn;
    return {};
  }
  if (game.grid.every((r) => r.every((cell) => cell !== null))) {
    game.winner = "draw";
    return {};
  }

  game.turn = game.turn === 0 ? 1 : 0;
  return {};
}

export function buildConnectFourEmbed(game: ConnectFourGame) {
  const symbols = { [0]: "🔴", [1]: "🟡" } as const;
  const board = game.grid.map((row) => row.map((cell) => (cell === null ? "⚪" : symbols[cell])).join("")).join("\n");

  let status: string;
  if (game.winner === "draw") {
    status = "It's a draw!";
  } else if (game.winner !== null) {
    status = `🎉 <@${game.players[game.winner]}> (${symbols[game.winner]}) wins!`;
  } else {
    status = `Turn: <@${game.players[game.turn]}> (${symbols[game.turn]})`;
  }

  return new EmbedBuilder().setTitle("Connect Four").setDescription(`${board}\n\n${status}`).setColor(0x5865f2);
}

export function buildConnectFourRow(game: ConnectFourGame) {
  const row = new ActionRowBuilder<ButtonBuilder>();
  for (let c = 0; c < COLS; c++) {
    const colFull = game.grid.every((r) => r[c] !== null);
    row.addComponents(
      new ButtonBuilder()
        .setCustomId(`c4_${game.id}_${c}`)
        .setLabel(String(c + 1))
        .setStyle(ButtonStyle.Primary)
        .setDisabled(colFull || game.winner !== null)
    );
  }
  return row;
}
