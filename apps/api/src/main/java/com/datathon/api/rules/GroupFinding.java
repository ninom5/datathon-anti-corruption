package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import java.util.List;
import java.util.Map;

public record GroupFinding(IndicatorDraft draft, String subjectType, Map<String, Object> subjectRef, List<Contract> contracts) {
}
