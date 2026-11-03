package com.datathon.api.assessment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The model's answer with the Croatian keys required by the brief (docs/ANOMALY_DETECTION.md §4.3). Enum values are
 * kept as the raw strings the model returned; {@link AiAssessmentValidator} decides what is usable.
 */
public record AiAnswer(
        @JsonProperty("oznaka") String label,
        @JsonProperty("razlozi") List<Reason> reasons,
        @JsonProperty("nedostajuci_podaci") List<String> missingData,
        @JsonProperty("moguca_legitimna_objasnjenja") List<String> legitExplanations,
        @JsonProperty("pouzdanost") String confidence) {

    public record Reason(
            @JsonProperty("tvrdnja") String claim,
            @JsonProperty("polja") List<String> fields,
            @JsonProperty("izvori") List<String> sources) {

        public List<String> fieldsOrEmpty() {
            return fields == null ? List.of() : fields;
        }

        public List<String> sourcesOrEmpty() {
            return sources == null ? List.of() : sources;
        }
    }

    public List<Reason> reasonsOrEmpty() {
        return reasons == null ? List.of() : reasons;
    }

    public List<String> missingDataOrEmpty() {
        return missingData == null ? List.of() : missingData;
    }

    public List<String> legitExplanationsOrEmpty() {
        return legitExplanations == null ? List.of() : legitExplanations;
    }
}
