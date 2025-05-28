package com.app.DAO;

import com.app.Model.Hashtag;

import java.util.Map;
import java.util.Set;

public interface HashtagDao {
    Map<String, Hashtag> findByTag(Set<String> tags);
    Hashtag save(Hashtag hashtag);
}
