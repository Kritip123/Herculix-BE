package org.example.herculix.service;

import org.example.herculix.entity.Trainer;

public interface TrainerAdminService {

    Trainer approveTrainer(String trainerId);

    Trainer rejectTrainer(String trainerId);
}
