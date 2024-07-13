/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a {@link LivingEntity} is rendered.
 * See the two subclasses to listen for before and after rendering.
 *
 * @param <T> the living entity that is being rendered
 * @param <M> the model for the living entity
 * @see RenderLivingEvent.Pre
 * @see RenderLivingEvent.Post
 * @see RenderPlayerEvent
 * @see LivingEntityRenderer
 */
public abstract class RenderLivingEvent<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends Event {
    private final S renderState;
    private final LivingEntityRenderer<T, S, M> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    @ApiStatus.Internal
    protected RenderLivingEvent(S renderState, LivingEntityRenderer<T, S, M> renderer, float partialTick, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int packedLight) {
        this.renderState = renderState;
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    /**
     * @return the render state of the living entity being rendered
     */
    public S getRenderState() {
        return renderState;
    }

    /**
     * @return the renderer for the living entity
     */
    public LivingEntityRenderer<T, S, M> getRenderer() {
        return renderer;
    }

    /**
     * {@return the partial tick}
     */
    public float getPartialTick() {
        return partialTick;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the source of rendering buffers}
     */
    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    /**
     * {@return the amount of packed (sky and block) light for rendering}
     *
     * @see LightTexture
     */
    public int getPackedLight() {
        return packedLight;
    }

    /**
     * Fired <b>before</b> an entity is rendered.
     * This can be used to render additional effects or suppress rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancelable}.
     * If this event is cancelled, then the entity will not be rendered and the corresponding
     * {@link RenderLivingEvent.Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main game event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> the living entity that is being rendered
     * @param <M> the model for the living entity
     */
    public static class Pre<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLivingEvent<T, S, M> implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(S renderState, LivingEntityRenderer<T, S, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(renderState, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    /**
     * Fired <b>after</b> an entity is rendered, if the corresponding {@link RenderLivingEvent.Post} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancelable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main game event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> the living entity that was rendered
     * @param <M> the model for the living entity
     */
    public static class Post<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLivingEvent<T, S, M> {
        @ApiStatus.Internal
        public Post(S renderState, LivingEntityRenderer<T, S, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(renderState, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
