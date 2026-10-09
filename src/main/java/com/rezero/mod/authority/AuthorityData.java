package com.rezero.mod.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Set;

/** Persistent authority/protection ownership. Effects remain allowlisted Java behavior. */
public record AuthorityData(Set<Identifier> authorities, Set<Identifier> divineProtections) {
    public static final AuthorityData DEFAULT = new AuthorityData(Set.of(), Set.of());
    private static final Codec<Set<Identifier>> ID_SET_CODEC = Identifier.CODEC.listOf().xmap(Set::copyOf, java.util.ArrayList::new);
    public static final Codec<AuthorityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ID_SET_CODEC.optionalFieldOf("authorities", Set.of()).forGetter(AuthorityData::authorities),
            ID_SET_CODEC.optionalFieldOf("divine_protections", Set.of()).forGetter(AuthorityData::divineProtections)
    ).apply(instance, AuthorityData::new));

    public AuthorityData {
        authorities = Set.copyOf(authorities);
        divineProtections = Set.copyOf(divineProtections);
    }

    public AuthorityData grantAuthority(Identifier id) {
        var updated = new java.util.HashSet<>(authorities);
        updated.add(id);
        return new AuthorityData(updated, divineProtections);
    }

    public AuthorityData grantProtection(Identifier id) {
        var updated = new java.util.HashSet<>(divineProtections);
        updated.add(id);
        return new AuthorityData(authorities, updated);
    }
}
