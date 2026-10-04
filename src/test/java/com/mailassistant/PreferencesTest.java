package com.mailassistant;

import com.mailassistant.desktop.DesktopShortcut;
import com.mailassistant.domain.model.*;
import com.mailassistant.mail.provider.MailProviderRegistry;
import com.mailassistant.repository.UserProfileRepository.Settings;
import com.mailassistant.security.AesGcmCryptoService;
import com.mailassistant.storage.encrypted.EncryptedProfileRepository;
import com.mailassistant.util.JsonUtil;
import com.mailassistant.validation.SubmissionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PreferencesTest {
    @TempDir Path temp;
    @Test void settingsRoundTripAndLegacyDefaults() throws Exception {
        var repository=new EncryptedProfileRepository(temp.resolve("profile.enc"),new AesGcmCryptoService(new byte[32]));
        var defaults=repository.load().preferences();
        assertFalse(defaults.autoDeleteDraft);assertTrue(defaults.minimizeToTray);assertEquals("",defaults.homeworkFolder);
        var prefs=new AppPreferences();prefs.autoDeleteDraft=true;prefs.minimizeToTray=false;prefs.homeworkFolder=temp.toString();
        repository.save(new Settings(new UserProfile(),new MailAccount(),prefs));
        var loaded=repository.load().preferences();
        assertTrue(loaded.autoDeleteDraft);assertFalse(loaded.minimizeToTray);assertEquals(temp,loaded.existingHomeworkFolder());
        var legacy=JsonUtil.MAPPER.readValue("{\"profile\":{},\"account\":{}}",Settings.class).preferences();
        assertFalse(legacy.autoDeleteDraft);assertTrue(legacy.minimizeToTray);
        assertFalse(JsonUtil.MAPPER.valueToTree(new Submission()).has("preferences"));
    }
    @Test void missingOrInvalidHomeworkFoldersFallBack() throws Exception {
        var prefs=new AppPreferences();assertNull(prefs.existingHomeworkFolder());
        prefs.homeworkFolder=temp.resolve("missing").toString();assertNull(prefs.existingHomeworkFolder());
        prefs.homeworkFolder="bad\u0000path";assertNull(prefs.existingHomeworkFolder());
        prefs.homeworkFolder=temp.toString();assertEquals(temp,prefs.existingHomeworkFolder());
    }
    @Test void providerCompletionAndNumericStudentId() throws Exception {
        assertEquals(List.of("QQ","163"),MailProviderRegistry.PROVIDERS);
        assertEquals("00123@qq.com",MailProviderRegistry.address("QQ","00123"));
        assertEquals("alice@163.com",MailProviderRegistry.address("163","alice"));
        assertEquals("",MailProviderRegistry.address("QQ",""));
        assertEquals("alice",MailProviderRegistry.localPart("alice@163.com"));
        var s=CoreWorkflowTest.complete();s.profile.studentId="00123";
        assertTrue(new SubmissionValidator().validate(s).isEmpty());
        s.profile.studentId="123abc";
        assertTrue(new SubmissionValidator().validate(s).stream().anyMatch(e->e.contains("学号只能")));
    }
    @Test @EnabledOnOs(OS.WINDOWS) void shortcutCreationIsIdempotentInTestDirectory() throws Exception {
        Path directory=Files.createDirectory(temp.resolve("桌面 ' test"));
        Path target=Path.of(System.getProperty("java.home"),"bin","javaw.exe");
        var shortcuts=new DesktopShortcut();
        shortcuts.create(directory,target,"-version",temp,target.toString());
        shortcuts.create(directory,target,"-version",temp,target.toString());
        try(var files=Files.list(directory)) { assertEquals(List.of(directory.resolve("MailAssistant.lnk")),files.toList()); }
        assertTrue(Files.size(directory.resolve("MailAssistant.lnk"))>0);
    }
}
