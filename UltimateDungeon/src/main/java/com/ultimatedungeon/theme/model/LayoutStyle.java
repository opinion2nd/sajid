package com.ultimatedungeon.theme.model;

/**
 * Spatial layout archetype used by the layout planner. Each theme maps to a
 * different archetype so every dungeon map has its own recognisable shape.
 *
 * <p>The archetypes follow classic dungeon-design patterns: hub-and-spoke,
 * linear winding path, mirrored processional axis, concentric defence rings
 * and a branching grid maze.</p>
 */
public enum LayoutStyle {

    /** Central hub with four spokes radiating outward; boss at the longest spoke's tip. */
    HUB_AND_SPOKE,

    /** One long serpentine path with small side pockets; boss at the far end. */
    WINDING_PATH,

    /** Straight processional axis with mirrored side chambers; boss at the axis end. */
    SYMMETRIC_AXIS,

    /** Concentric defensive rings; players fight inward from the outer ring to the central keep. */
    CONCENTRIC_RINGS,

    /** Dense branching maze with dead ends; boss hidden in the deepest cell. */
    GRID_MAZE
}
