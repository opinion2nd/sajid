package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Players must deliver the required item to the altar to advance. */
public final class ItemDeliveryPuzzle extends AbstractPuzzle {
    private final Material required;
    public ItemDeliveryPuzzle() { this(PuzzleDefinition.of("ItemDeliveryPuzzle", 1), Material.EMERALD); }
    public ItemDeliveryPuzzle(@NotNull final PuzzleDefinition def, @NotNull final Material required) {
        super(def); this.required = required;
    }
    @Override
    public void onPlayerInteract(@NotNull final Player player) {
        if (player.getInventory().getItemInMainHand().getType() == required) {
            advance(player);
        } else {
            fail(player);
        }
    }
}
