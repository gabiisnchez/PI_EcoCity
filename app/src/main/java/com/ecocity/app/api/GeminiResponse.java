package com.ecocity.app.api;

import java.util.List;

/**
 * Modelo Pojo para parsear automáticamente la respuesta JSON de Gemini API
 * usando GSON.
 */
public class GeminiResponse {

    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public static class Candidate {
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private String text;

        public String getText() {
            return text;
        }
    }

    /**
     * Helper method para extraer el string del primer resultado validando nulos.
     */
    public String extractText() {
        try {
            if (candidates != null && !candidates.isEmpty() &&
                    candidates.get(0).getContent() != null &&
                    candidates.get(0).getContent().getParts() != null &&
                    !candidates.get(0).getContent().getParts().isEmpty()) {

                return candidates.get(0).getContent().getParts().get(0).getText();
            }
        } catch (Exception e) {
        }
        return "Lo siento, no he podido procesar la respuesta.";
    }
}
