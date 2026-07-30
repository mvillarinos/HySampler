package dev.hysampler.sampler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DecentSamplerMapping {

    private final List<Region> regions;

    public DecentSamplerMapping(List<Region> regions) {
        this.regions = regions.stream()
                .sorted(Comparator.comparingInt(Region::loNote).thenComparingInt(Region::hiNote))
                .toList();
    }

    public Optional<Region> resolve(int note, int velocity) {
        return regions.stream()
                .filter(region -> region.matches(note, velocity))
                .findFirst();
    }

    public List<Region> regions() {
        return regions;
    }

    public record Region(
            String sourcePath,
            String soundEventId,
            int rootNote,
            int loNote,
            int hiNote,
            int loVel,
            int hiVel
    ) {
        public boolean matches(int note, int velocity) {
            return note >= loNote && note <= hiNote && velocity >= loVel && velocity <= hiVel;
        }

        public float pitchModifierFor(int note) {
            return (float) Math.pow(2.0d, (note - rootNote) / 12.0d);
        }
    }
}
