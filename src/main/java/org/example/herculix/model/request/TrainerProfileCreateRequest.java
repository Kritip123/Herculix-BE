package org.example.herculix.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileCreateRequest {

    @Valid
    @NotNull(message = "Profile is required")
    private Profile profile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "City is required")
        private String city;

        private String headline;

        private String bio;

        private String profileImage;

        private String coverImage;

        private java.util.List<String> specializations;

        private Integer yearsActive;

        private java.util.List<String> languages;

        private TrainerProfileUpdateRequest.Pricing pricing;
    }
}
