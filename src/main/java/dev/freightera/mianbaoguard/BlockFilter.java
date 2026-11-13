package dev.freightera.mianbaoguard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Immutable whitelist. Built once on config load; never reads the config file
 * or parses strings on the hot path.
 */
public final class BlockFilter {
    public static final BlockFilter EMPTY = new BlockFilter(Set.of(), List.of());

    private final Set<ResourceLocation> ids;
    private final List<TagKey<Block>> tags;

    private BlockFilter(Set<ResourceLocation> ids, List<TagKey<Block>> tags) {
        this.ids = ids;
        this.tags = tags;
    }

    public static BlockFilter parse(List<? extends String> entries) {
        if (entries == null || entries.isEmpty()) {
            return EMPTY;
        }

        Set<ResourceLocation> ids = new HashSet<>();
        List<TagKey<Block>> tags = new ArrayList<>();

        for (String raw : entries) {
            if (raw == null) {
                continue;
            }
            String entry = raw.trim();
            if (entry.isEmpty()) {
                continue;
            }

            if (entry.charAt(0) == '#') {
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id == null) {
                    MianbaoBlockGuard.LOGGER.warn("Mianbao BlockGuard: ignoring malformed block tag '{}'", entry);
                    continue;
                }
                tags.add(BlockTags.create(id));
                continue;
            }

            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id == null) {
                MianbaoBlockGuard.LOGGER.warn("Mianbao BlockGuard: ignoring malformed block id '{}'", entry);
                continue;
            }
            // Do not resolve against BuiltInRegistries here: common config can load
            // before every mod has registered its blocks. The id is compared at
            // runtime, when the registry is complete. An id that never exists
            // simply never matches.
            ids.add(id);
        }

        if (ids.isEmpty() && tags.isEmpty()) {
            return EMPTY;
        }
        return new BlockFilter(Set.copyOf(ids), List.copyOf(tags));
    }

    public boolean isEmpty() {
        return this.ids.isEmpty() && this.tags.isEmpty();
    }

    public int size() {
        return this.ids.size() + this.tags.size();
    }

    /** True only when the block is explicitly whitelisted. An empty filter allows nothing. */
    public boolean allows(BlockState state) {
        if (this.isEmpty()) {
            return false;
        }
        if (!this.ids.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (this.ids.contains(id)) {
                return true;
            }
        }
        for (TagKey<Block> tag : this.tags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
