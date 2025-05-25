package com.app.DAO;

import com.app.Model.Hashtag;

public interface HashtagDao {
    Hashtag findByTag(String tag);
    Hashtag save(Hashtag hashtag);
}
