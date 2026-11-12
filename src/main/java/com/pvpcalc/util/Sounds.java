
package com.pvpcalc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class Sounds {

    private Sounds() {
    }

    public static boolean play(String id, float pitch, float volume) {
        try {
            Identifier ident = Identifier.tryParse(id);

            if (ident == null) {
                return false;
            }

            MinecraftClient mc = MinecraftClient.getInstance();

            if (mc == null || mc.getSoundManager() == null) {
                return false;
            }

            PositionedSoundInstance sound = PositionedSoundInstance.ui(
                    SoundEvent.of(ident),
                    pitch,
                    volume
            );

            mc.getSoundManager().play(sound);

            return true;

        } catch (Throwable t) {
            return false;
        }
    }
}
