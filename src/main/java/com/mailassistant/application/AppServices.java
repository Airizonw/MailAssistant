package com.mailassistant.application;

import com.mailassistant.config.PathConfig;
import com.mailassistant.repository.*;
import com.mailassistant.security.*;
import com.mailassistant.storage.encrypted.*;
import com.mailassistant.storage.sqlite.*;
import com.mailassistant.export.*;
import com.mailassistant.mail.JakartaMailClient;

public final class AppServices {
    public final PathConfig paths;
    public final ExerciseRepository exercises;
    public final UserProfileRepository profiles;
    public final CredentialService credentials;
    public final SubmissionService submissions;
    public final DocxExporter exporter = new ApachePoiDocxExporter();
    public final MailService mail;
    public AppServices(PathConfig paths) throws Exception {
        this.paths = paths; paths.initialize();
        credentials = new CredentialService(new WindowsSecretStore(paths.config().resolve("secrets")));
        CryptoService crypto = credentials.crypto();
        profiles = new EncryptedProfileRepository(paths.config().resolve("user-profile.enc"), crypto);
        submissions = new SubmissionService(new EncryptedDraftRepository(paths.drafts(), crypto));
        exercises = new SQLiteExerciseRepository(new DatabaseManager(paths.database()));
        mail = new MailService(new JakartaMailClient(), credentials, submissions);
    }
}
