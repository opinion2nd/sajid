import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";

const CHOICES = ["rock", "paper", "scissors"] as const;
type Choice = (typeof CHOICES)[number];

const BEATS: Record<Choice, Choice> = { rock: "scissors", paper: "rock", scissors: "paper" };

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("rps")
    .setDescription("Play rock-paper-scissors against the bot")
    .addStringOption((o) =>
      o
        .setName("choice")
        .setDescription("Your choice")
        .setRequired(true)
        .addChoices({ name: "Rock", value: "rock" }, { name: "Paper", value: "paper" }, { name: "Scissors", value: "scissors" })
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const userChoice = interaction.options.getString("choice", true) as Choice;
    const botChoice = CHOICES[Math.floor(Math.random() * CHOICES.length)];

    let result: string;
    if (userChoice === botChoice) result = "It's a tie!";
    else if (BEATS[userChoice] === botChoice) result = "You win! 🎉";
    else result = "I win! 🤖";

    await interaction.reply({ embeds: [infoEmbed(`You chose **${userChoice}**, I chose **${botChoice}**.\n${result}`)] });
  },
};

export default command;
