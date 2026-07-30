package dev.hysampler.sampler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DecentSamplerPresetParser {

    private DecentSamplerPresetParser() {
    }

    public static DecentSamplerMapping loadFromResource(
            String presetResourcePath,
            Map<String, String> sourcePathToSoundEventId,
            Logger logger
    ) {
        try (InputStream stream = DecentSamplerPresetParser.class.getClassLoader().getResourceAsStream(presetResourcePath)) {
            if (stream == null) {
                logger.severe("HySampler preset not found in resources: " + presetResourcePath);
                return new DecentSamplerMapping(List.of());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(stream);
            NodeList sampleNodes = document.getElementsByTagName("sample");
            List<DecentSamplerMapping.Region> regions = new ArrayList<>();

            for (int i = 0; i < sampleNodes.getLength(); i++) {
                Element sample = (Element) sampleNodes.item(i);
                String sourcePath = sample.getAttribute("path");
                String soundEventId = sourcePathToSoundEventId.get(sourcePath);
                if (soundEventId == null) {
                    continue;
                }

                int rootNote = parseIntAttr(sample, "rootNote", -1);
                int loNote = parseIntAttr(sample, "loNote", rootNote);
                int hiNote = parseIntAttr(sample, "hiNote", rootNote);
                int loVel = parseIntAttr(sample, "loVel", 0);
                int hiVel = parseIntAttr(sample, "hiVel", 127);

                if (rootNote < 0 || loNote < 0 || hiNote < 0) {
                    logger.warning("Skipping sample with invalid note mapping: " + sourcePath);
                    continue;
                }

                regions.add(new DecentSamplerMapping.Region(
                        sourcePath,
                        soundEventId,
                        rootNote,
                        loNote,
                        hiNote,
                        loVel,
                        hiVel
                ));
            }

            logger.info("HySampler loaded " + regions.size() + " DecentSampler regions from " + presetResourcePath + ".");
            return new DecentSamplerMapping(regions);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Failed to parse HySampler preset XML: " + presetResourcePath, exception);
            return new DecentSamplerMapping(List.of());
        }
    }

    private static int parseIntAttr(Element element, String attr, int fallback) {
        String raw = element.getAttribute(attr);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
