package com.mailassistant.repository;

import com.mailassistant.domain.model.*;

public interface UserProfileRepository {
    record Settings(UserProfile profile, MailAccount account, AppPreferences preferences) {
        public Settings {
            if (preferences == null) preferences = new AppPreferences();
        }
        public Settings(UserProfile profile, MailAccount account) { this(profile, account, new AppPreferences()); }
    }
    Settings load() throws Exception;
    void save(Settings settings) throws Exception;
    void delete() throws Exception;
}
