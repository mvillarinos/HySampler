# HySampler

HySampler is a Hytale server-side plugin POC that mirrors DecentSampler's note-to-sample playback model:

- parse `<sample>` note zones (`rootNote`, `loNote`, `hiNote`, `loVel`, `hiVel`) from a `.dspreset`
- map notes to pre-rendered audio samples
- trigger mapped samples in-game via `SoundUtil.playSoundEvent3dToPlayer`

This POC intentionally does **not** implement live synthesis, PCM generation, or runtime DSP modulation.

## What this POC includes

- Rebranded plugin-template scaffold (`dev.hysampler.HySamplerPlugin`)
- DecentSampler XML parser (`DecentSamplerPresetParser`)
- Note/velocity lookup model (`DecentSamplerMapping`)
- In-game trigger command:
  - `/sampler play <note 0-127> [velocity 0-127]`
- Minimal sample set (3 source samples covering the playable region from C3 to C5 by pitch-shifting)
- Registered custom `SoundEvent` assets for the bundled samples

## Source material and licensing

Source pack used:
- Repository: https://github.com/DecentSamples/DecentSampler-Sample-Library-Examples
- Preset: `example-001-boilerplate/Simple Preset Library.dspreset`
- License: MIT (copied into `src/main/resources/hysampler/source/DecentSampler-Sample-Library-Examples-LICENSE`)

Attribution:
- Copyright (c) 2024 Decent Samples

The original source `.dspreset` used by this POC is included at:
- `src/main/resources/hysampler/source/Simple-Preset-Library.dspreset`

## Asset pipeline used

1. Read DecentSampler `.dspreset` XML
2. Extract only `<sample>` mapping attributes required for note-to-sample routing
3. Select a minimal subset of referenced samples for the POC
4. Convert source WAV files to OGG
5. Place OGG files under `src/main/resources/Common/Sounds/HySampler`
6. Register one `SoundEvent` JSON per sample under `src/main/resources/Server/SoundEvents`
7. Resolve note+velocity at runtime, find matching `SoundEvent`, and play with optional pitch offset relative to `rootNote`

## Build

```bash
./gradlew build
```

## In-game usage

```text
/sampler play 60
/sampler play 64 100
```

- `note` is MIDI-style integer range `0..127`
- `velocity` is optional (`0..127`, defaults to `127`)

## Scope and non-goals

Out of scope for this POC:

- real-time oscillators / PCM synthesis
- runtime modulation engine (LFO, vibrato, tremolo, filter sweeps)
- DecentSampler effects, UI, envelopes, and other non-mapping sections
- full multi-layer instrument emulation beyond basic mapping proof

## Useful references

- Hytale playing sounds guide:
  - https://hytalemodding.dev/en/docs/guides/plugin/playing-sounds
- Hytale plugin template:
  - https://github.com/HytaleModding/plugin-template
- DecentSampler docs:
  - https://www.decentsamples.com/docs/format-documentation.html
