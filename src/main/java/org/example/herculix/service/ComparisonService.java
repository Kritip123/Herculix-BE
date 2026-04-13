package org.example.herculix.service;

import org.example.herculix.model.response.ComparisonResponse;

public interface ComparisonService {

    ComparisonResponse getComparison(String userId, Double latitude, Double longitude);
}
