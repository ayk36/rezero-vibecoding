package com.rezero.mod.ability;

import net.minecraft.resources.Identifier;

/** Server-executed ability contract. Implementations must never trust client-supplied outcomes. */
public interface Ability {
    Identifier id();

    int manaCost();

    int staminaCost();

    int cooldownTicks();

    boolean canUse(AbilityContext context);

    void execute(AbilityContext context);
}
