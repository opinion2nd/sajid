import { ActionRowBuilder, ButtonBuilder, ButtonStyle, EmbedBuilder } from "discord.js";

export interface TicTacToeGame {
  id: string;
  board: (0 | 1 | null)[];
  players: [string, string];
  turn: 0 | 1;
  winner: 0 | 1 | "draw" | null;
}

const games = new Map<string, TicTacToeGame>();

const LINES = [
  [0, 1, 2],
  [3, 4, 5],
  [6, 7, 8],
  [0, 3, 6],
  [1, 4, 7],
  [2, 5, 8],
  [0, 4, 8],
  [2, 4, 6],
];

export function createTicTacToeGame(player1: string, player2: string): TicTacToeGame {
  const id = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const game: TicTacToeGame = { id, board: new Array(9).fill(null), players: [player1, player2], turn: 0, winner: null };
  games.set(id, game);
  return game;
}

export function getTicTacToeGame(id: string): TicTacToeGame | undefined {
  return games.get(id);
}

export function playTicTacToeMove(id: string, userId: string, cell: number): { error?: string } {
  const game = games.get(id);
  if (!game) return { error: "This game no longer exists." };
  if (game.winner) return { error: "This game has already ended." };
  if (game.players[game.turn] !== userId) return { error: "It's not your turn." };
  if (game.board[cell] !== null) return { error: "That cell is already taken." };

  game.board[cell] = game.turn;

  for (const [a, b, c] of LINES) {
    if (game.board[a] !== null && game.board[a] === game.board[b] && game.board[b] === game.board[c]) {
      game.winner = game.board[a] as 0 | 1;
      return {};
    }
  }
  if (game.board.every((cell) => cell !== null)) {
    game.winner = "draw";
    return {};
  }

  game.turn = game.turn === 0 ? 1 : 0;
  return {};
}

export function buildTicTacToeEmbed(game: TicTacToeGame) {
  const embed = new EmbedBuilder().setTitle("Tic-Tac-Toe").setColor(0x5865f2);
  if (game.winner === "draw") {
    embed.setDescription("It's a draw!");
  } else if (game.winner !== null) {
    embed.setDescription(`<@${game.players[game.winner]}> wins! 🎉`);
  } else {
    embed.setDescription(`Turn: <@${game.players[game.turn]}> (${game.turn === 0 ? "❌" : "⭕"})`);
  }
  return embed;
}

export function buildTicTacToeRows(game: TicTacToeGame) {
  const rows: ActionRowBuilder<ButtonBuilder>[] = [];
  for (let r = 0; r < 3; r++) {
    const row = new ActionRowBuilder<ButtonBuilder>();
    for (let c = 0; c < 3; c++) {
      const idx = r * 3 + c;
      const value = game.board[idx];
      const button = new ButtonBuilder()
        .setCustomId(`ttt_${game.id}_${idx}`)
        .setLabel(value === null ? "​" : value === 0 ? "X" : "O")
        .setStyle(value === 0 ? ButtonStyle.Danger : value === 1 ? ButtonStyle.Success : ButtonStyle.Secondary)
        .setDisabled(value !== null || game.winner !== null);
      row.addComponents(button);
    }
    rows.push(row);
  }
  return rows;
}
