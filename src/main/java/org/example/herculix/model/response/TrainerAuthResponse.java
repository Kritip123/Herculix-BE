package org.example.herculix.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerAuthResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String token;
    private String refreshToken;
    private Long expiresIn;
}
