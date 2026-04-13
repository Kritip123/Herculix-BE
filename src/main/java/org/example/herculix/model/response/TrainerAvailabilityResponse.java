package org.example.herculix.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerAvailabilityResponse {
    private String date;
    private Map<String, List<String>> weekly;
    private List<String> slots;
}
