/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class UpdateRenderStateEvent extends Event {
    private final EntityRenderer<?, ?> renderer;
    private final Entity entity;
    private final EntityRenderState renderState;
    private final ContextMap.Builder extensionBuilder;
    final ContextKeySet.Builder validatorBuilder;

    @ApiStatus.Internal
    public UpdateRenderStateEvent(EntityRenderer<?, ?> renderer, Entity entity, EntityRenderState renderState) {
        this.renderer = renderer;
        this.entity = entity;
        this.renderState = renderState;
        this.extensionBuilder = new ContextMap.Builder();
        this.validatorBuilder = new ContextKeySet.Builder();
    }

    public EntityRenderer<?, ?> getRenderer() {
        return renderer;
    }

    public Entity getEntity() {
        return entity;
    }

    public EntityRenderState getCurrentState() {
        return renderState;
    }

    public <T> void withRequiredState(ContextKey<T> key, T object) {
        validatorBuilder.required(key);
        extensionBuilder.withParameter(key, object);
    }

    public <T> void withOptionalState(ContextKey<T> key, @Nullable T object) {
        validatorBuilder.optional(key);
        extensionBuilder.withOptionalParameter(key, object);
    }

    @ApiStatus.Internal
    public ContextMap buildMap() {
        return extensionBuilder.create(validatorBuilder.build());
    }
}
