package dev.hysampler.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hysampler.sampler.DecentSamplerMapping;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SamplerCommand extends AbstractCommand {

    private final DecentSamplerMapping mapping;

    public SamplerCommand(String name, String description, DecentSamplerMapping mapping) {
        super(name, description);
        this.mapping = mapping;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command can only be used by a player."));
            return CompletableFuture.completedFuture(null);
        }

        String[] args = parseArgs(context.getInputString());
        if (args.length < 2 || !"play".equalsIgnoreCase(args[0])) {
            context.sendMessage(Message.raw("Usage: /sampler play <note 0-127> [velocity 0-127]"));
            return CompletableFuture.completedFuture(null);
        }

        int note = parseBoundedInt(args[1], 0, 127);
        if (note < 0) {
            context.sendMessage(Message.raw("Invalid note. Expected an integer between 0 and 127."));
            return CompletableFuture.completedFuture(null);
        }

        int velocity = 127;
        if (args.length >= 3) {
            velocity = parseBoundedInt(args[2], 0, 127);
            if (velocity < 0) {
                context.sendMessage(Message.raw("Invalid velocity. Expected an integer between 0 and 127."));
                return CompletableFuture.completedFuture(null);
            }
        }

        DecentSamplerMapping.Region region = mapping.resolve(note, velocity).orElse(null);
        if (region == null) {
            context.sendMessage(Message.raw("No mapped sample was found for note " + note + " and velocity " + velocity + "."));
            return CompletableFuture.completedFuture(null);
        }

        int soundEventIndex = SoundEvent.getAssetMap().getIndex(region.soundEventId());
        if (soundEventIndex == 0) {
            context.sendMessage(Message.raw("Mapped sound event not found: " + region.soundEventId()));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> playerEntityRef = context.senderAsPlayerRef();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            context.sendMessage(Message.raw("Player entity is not available yet. Try again in a moment."));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = playerEntityRef.getStore();
        TransformComponent transform = store.getComponent(playerEntityRef, EntityModule.get().getTransformComponentType());
        if (transform == null) {
            context.sendMessage(Message.raw("Player transform is unavailable."));
            return CompletableFuture.completedFuture(null);
        }

        Vector3d position = transform.getPosition();
        float pitchModifier = region.pitchModifierFor(note);
        SoundUtil.playSoundEvent3dToPlayer(
                playerEntityRef,
                soundEventIndex,
                SoundCategory.UI,
                position.x(),
                position.y(),
                position.z(),
                1.0f,
                pitchModifier,
                store.getStore()
        );

        context.sendMessage(Message.raw("Played note " + note + " using " + region.soundEventId() + " (pitch x" + String.format("%.3f", pitchModifier) + ")."));
        return CompletableFuture.completedFuture(null);
    }

    private static String[] parseArgs(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length <= 1) {
            return new String[0];
        }

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    private static int parseBoundedInt(String input, int min, int max) {
        try {
            int value = Integer.parseInt(input);
            return value >= min && value <= max ? value : -1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
