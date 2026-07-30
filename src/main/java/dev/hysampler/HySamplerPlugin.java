package dev.hysampler;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hysampler.command.SamplerCommand;
import dev.hysampler.sampler.DecentSamplerMapping;
import dev.hysampler.sampler.DecentSamplerPresetParser;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.logging.Logger;

public class HySamplerPlugin extends JavaPlugin {

    private static final String SOURCE_PRESET_PATH = "hysampler/source/Simple-Preset-Library.dspreset";
    private static final Map<String, String> SOURCE_PATH_TO_SOUND_EVENT_ID = Map.of(
            "Samples/PolyBrute Triangle Wave-C3-V127-Z5O6.wav", "hysampler_polybrute_triangle_c3",
            "Samples/PolyBrute Triangle Wave-F#3-V127-WBNT.wav", "hysampler_polybrute_triangle_fs3",
            "Samples/PolyBrute Triangle Wave-C4-V127-SNGL.wav", "hysampler_polybrute_triangle_c4"
    );

    public HySamplerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Logger logger = this.getLogger();
        DecentSamplerMapping mapping = DecentSamplerPresetParser.loadFromResource(
                SOURCE_PRESET_PATH,
                SOURCE_PATH_TO_SOUND_EVENT_ID,
                logger
        );

        this.getCommandRegistry().registerCommand(new SamplerCommand("sampler", "Play mapped DecentSampler notes", mapping));
    }
}
