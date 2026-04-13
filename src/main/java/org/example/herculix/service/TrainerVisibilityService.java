package org.example.herculix.service;

import org.example.herculix.entity.Trainer;

public interface TrainerVisibilityService {

    boolean isVisibleToUsers(Trainer trainer);
}
