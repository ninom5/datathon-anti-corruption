package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import java.util.List;
import java.util.Map;

/**
 * What a group rule found: the indicator draft, what it is about ({@code group} or {@code buyer_cpv} with a JSON
 * reference, docs/DATA_MODEL.md §2) and every contract of the group, all of them listed in the evidence.
 */
public record GroupFinding(IndicatorDraft draft, String subjectType, Map<String, Object> subjectRef, List<Contract> contracts) {
}
