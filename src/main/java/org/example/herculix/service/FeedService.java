package org.example.herculix.service;

import org.example.herculix.model.request.FeedRequest;
import org.example.herculix.model.response.FeedResponse;

public interface FeedService {

    FeedResponse getFeed(String userId, FeedRequest request);
}
