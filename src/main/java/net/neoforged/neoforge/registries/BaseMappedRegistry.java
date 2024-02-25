/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public abstract class BaseMappedRegistry<T> implements Registry<T> {
    protected final List<AddCallback<T>> addCallbacks = new ArrayList<>();
    protected final List<BakeCallback<T>> bakeCallbacks = new ArrayList<>();
    protected final List<ClearCallback<T>> clearCallbacks = new ArrayList<>();
    final Map<ResourceLocation, ResourceLocation> aliases = new HashMap<>();
    final Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> dataMaps = new IdentityHashMap<>();
    final Map<T, String> intrusiveHolderStackTraces = Boolean.getBoolean("neoforge.traceIntrusiveHolders") ? new IdentityHashMap<>() : null;

    private int maxId = Integer.MAX_VALUE - 1;
    private boolean sync;

    void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public boolean doesSync() {
        return this.sync;
    }

    void setMaxId(int maxId) {
        this.maxId = maxId;
    }

    @Override
    public int getMaxId() {
        return this.maxId;
    }

    @Override
    public void addCallback(RegistryCallback<T> callback) {
        if (callback instanceof AddCallback<T> addCallback)
            this.addCallbacks.add(addCallback);
        if (callback instanceof BakeCallback<T> bakeCallback)
            this.bakeCallbacks.add(bakeCallback);
        if (callback instanceof ClearCallback<T> clearCallback)
            this.clearCallbacks.add(clearCallback);
    }

    @Override
    public void addAlias(ResourceLocation from, ResourceLocation to) {
        if (from.equals(to))
            return;
        if (this.aliases.containsKey(from)) {
            ResourceLocation old = this.aliases.get(from);
            if (!old.equals(to))
                throw new IllegalStateException("Duplicate alias with key \"" + from + "\" attempting to map to \"" + to + "\", found existing mapping \"" + old + "\"");
        }
        if (resolve(from).equals(to))
            throw new IllegalStateException("Infinite alias loop detected: from " + from + " to " + to);
        this.aliases.put(from, to);
    }

    @Override
    public ResourceLocation resolve(ResourceLocation name) {
        if (this.containsKey(name))
            return name;

        ResourceLocation alias = this.aliases.get(name);
        if (alias == null)
            return name;

        return resolve(alias);
    }

    @Override
    public ResourceKey<T> resolve(ResourceKey<T> key) {
        ResourceLocation resolvedName = resolve(key.location());
        // Try to reuse the key if possible
        return resolvedName == key.location() ? key : ResourceKey.create(this.key(), resolvedName);
    }

    @Override
    public int getId(ResourceKey<T> key) {
        T value = this.get(key);
        return value == null ? -1 : this.getId(value);
    }

    @Override
    public int getId(ResourceLocation name) {
        T value = this.get(name);
        return value == null ? -1 : this.getId(value);
    }

    protected void clear(boolean full) {
        this.aliases.clear();
        if (full) {
            this.dataMaps.clear();
        }
    }

    /**
     * Register a key <-> ID mapping.
     * <b>The IDs must be registered in increasing order.</b>
     */
    protected abstract void registerIdMapping(ResourceKey<T> key, int id);

    protected abstract void unfreeze();

    protected void removeTraceIfEnabled(T element) {
        if (intrusiveHolderStackTraces != null) {
            intrusiveHolderStackTraces.remove(element);
        }
    }

    protected void storeTraceIfEnabled(T element) {
        if (intrusiveHolderStackTraces != null) {
            intrusiveHolderStackTraces.computeIfAbsent(element, t -> StackWalker.getInstance().walk(stackFrameStream -> stackFrameStream.skip(5).findFirst().map(Object::toString).orElse("")));
        }
    }

    protected List<String> wrapIntrusiveHolderError(Collection<Holder.Reference<T>> elements) {
        if (intrusiveHolderStackTraces != null) {
            List<String> traces = elements.stream().map(tReference -> intrusiveHolderStackTraces.get(tReference.value())).toList();
            intrusiveHolderStackTraces.clear();
            return traces;
        }
        return elements.stream().map(t -> t.value().getClass().getSimpleName()).toList();
    }

    @Override
    public <A> @Nullable A getData(DataMapType<T, A> type, ResourceKey<T> key) {
        final var innerMap = dataMaps.get(type);
        return innerMap == null ? null : (A) innerMap.get(key);
    }

    @Override
    public <A> Map<ResourceKey<T>, A> getDataMap(DataMapType<T, A> type) {
        return (Map<ResourceKey<T>, A>) dataMaps.getOrDefault(type, Map.of());
    }
}
