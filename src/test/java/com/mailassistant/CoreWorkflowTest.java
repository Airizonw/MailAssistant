package com.mailassistant;

import com.mailassistant.domain.model.*;
import com.mailassistant.security.*;
import com.mailassistant.storage.encrypted.*;
import com.mailassistant.storage.sqlite.*;
import com.mailassistant.preview.*;
import com.mailassistant.validation.*;
import com.mailassistant.application.*;
import com.mailassistant.export.*;
import com.mailassistant.mail.*;
import com.mailassistant.config.PathConfig;
import com.mailassistant.util.JsonUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.*;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import jakarta.mail.*;
import org.apache.poi.xwpf.usermodel.*;

class CoreWorkflowTest {
    @TempDir Path temp;
    static ImageResource image(String id) throws Exception {
        var bytes=new ByteArrayOutputStream();var image=new BufferedImage(120,70,BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics();g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,120,70);g.setColor(java.awt.Color.BLACK);g.drawString("Result: 42",10,35);g.dispose();ImageIO.write(image,"png",bytes);
        return new ImageValidator().decode(id,"result.png",bytes.toByteArray());
    }
    static Submission complete() throws Exception {
        Submission s=new Submission();s.chapter=new Chapter(1,1,"第1章");s.profile.college="计算机学院";s.profile.className="2301";s.profile.studentId="2026001";s.profile.name="测试用户";s.profile.teacherEmail="teacher@example.com";s.profile.assistantEmails.add("ta@example.com");s.account.address="student@example.com";
        var exercise=new Exercise(1001,1,"1","示例题","PROGRAMMING",true,true,false,List.of(ContentBlock.text("paragraph","题干 <script>alert('x')</script>")),Map.of());
        var item=new SubmissionItem(exercise);item.sources.add(new JavaSource("Main.java","public class Main {\n    // <tag> & 中文\n}"));item.answer="自己的解答";
        var first=new RunResultCase();first.description="正常输入";first.images.add(image("first"));var second=new RunResultCase();second.description="边界输入";second.images.add(image("second"));item.resultCases.add(first);item.resultCases.add(second);s.items.add(item);s.preview=new HtmlSubmissionRenderer().compose(s);return s;
    }
    private CryptoService crypto(){byte[] key=new byte[32];new java.security.SecureRandom().nextBytes(key);return new AesGcmCryptoService(key);}
    @Test void generatedBodyFormattingAndSectionOrderMatchHtmlAndDocx() throws Exception {
        var s=complete();s.items.getFirst().umlImages.add(image("uml"));
        var renderer=new HtmlSubmissionRenderer();s.preview=renderer.compose(s);
        assertTrue(new PreviewValidator().validate(s).isEmpty());
        assertEquals("计算机学院-2301班-2026001-测试用户",s.preview.blocks.getFirst().text);
        String html=renderer.html(s.preview,false);
        assertTrue(html.contains("font-weight:bold'>计算机学院-2301班"));
        assertTrue(html.contains("font-size:17px;font-weight:bold'>解答</h3>"));
        assertTrue(html.indexOf("cid:uml")<html.indexOf("运行结果"));
        assertTrue(s.preview.blocks.stream().filter(b->b.role.equals("uml")).allMatch(b->b.caption.isBlank()));
        assertEquals(1,html.split("运行结果",-1).length-1);
        assertTrue(html.contains("运行情况 1：正常输入"));assertTrue(html.contains("运行情况 2：边界输入"));
        assertTrue(s.preview.blocks.stream().filter(b->b.role.startsWith("result:")).allMatch(b->b.caption.isBlank()));
        String preview=renderer.html(s.preview,true);
        for(int i=0;i<s.preview.blocks.size();i++)assertTrue(preview.contains("id='preview-block-"+i+"'"));
        assertFalse(html.contains("preview-block-"));
        Path output=temp.resolve("formatting.docx");new ApachePoiDocxExporter().export(s.preview,output);
        try(var doc=new XWPFDocument(Files.newInputStream(output))) {
            assertTrue(doc.getParagraphs().getFirst().getRuns().getFirst().isBold());
            for(String heading:List.of("解答","运行结果")) {
                var run=doc.getParagraphs().stream().filter(p->p.getText().equals(heading)).findFirst().orElseThrow().getRuns().getFirst();
                assertTrue(run.isBold());assertEquals(12,run.getFontSizeAsDouble());
            }
            var text=doc.getParagraphs().stream().map(XWPFParagraph::getText).reduce("",(a,b)->a+"\n"+b);
            assertTrue(text.indexOf("UML 图")<text.indexOf("运行结果"));
            assertFalse(doc.getParagraphs().stream().anyMatch(p->p.getText().equals("UML")));
        }
        var uml=s.preview.blocks.stream().filter(b->b.role.equals("uml")).findFirst().orElseThrow();
        s.preview.blocks.remove(uml);s.preview.blocks.add(uml);
        assertTrue(new PreviewValidator().validate(s).stream().anyMatch(e->e.contains("顺序")));
    }
    @Test void offlinePreviewAndExportDoNotRequireMailSettingsButSendingDoes() throws Exception {
        var s=complete();s.profile.teacherEmail="";s.profile.assistantEmails.clear();s.account.address="";s.account.host="";
        var submissions=new SubmissionService(null);
        s.preview=submissions.generate(s);
        assertDoesNotThrow(()->submissions.validateFinal(s));
        Path output=temp.resolve("offline.docx");new ApachePoiDocxExporter().export(s.preview,output);
        assertTrue(Files.size(output)>0);
        var mail=new MailService((a,secret,doc)->fail("Must not send"),new CredentialService(new SecretStore(){
            public void save(String n,byte[] v){} public byte[] load(String n){return null;} public void delete(String n){}
        }),submissions);
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("教师邮箱"));
        s.preview.to="teacher@example.com";
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("助教邮箱"));
        s.preview.cc.add("ta@example.com");
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("发件邮箱"));
        s.account.address="student@example.com";
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("SMTP"));
        s.account.host="smtp.example.com";
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("授权码"));
        s.preview.cc.add("TEACHER@example.com");
        assertTrue(assertThrows(IllegalArgumentException.class,()->mail.prepare(s)).getMessage().contains("重复"));
    }
    @Test void manuallyNamedSourcesGetJavaExtensionInGeneratedBody() throws Exception {
        var s=complete();var source=s.items.getFirst().sources.getFirst();
        for(String name:List.of("Main","Main.java"," Main ")) {
            source.name=name;s.preview=new SubmissionService(null).generate(s);
            assertEquals("Main.java",s.preview.blocks.stream().filter(b->b.role.equals("source-heading")).findFirst().orElseThrow().text);
        }
        assertEquals("Main.JAVA",new JavaSource("Main.JAVA","").fileName());
        assertEquals("",new JavaSource(" ","").fileName());
    }
    @Test void singleRunHasNoNumberAndKeepsOptionalDescription() throws Exception {
        var s=complete();var item=s.items.getFirst();item.resultCases.removeLast();
        var renderer=new HtmlSubmissionRenderer();
        for(String description:List.of("正常输入","")) {
            item.resultCases.getFirst().description=description;s.preview=renderer.compose(s);
            assertTrue(new PreviewValidator().validate(s).isEmpty());
            var html=renderer.html(s.preview,false);
            assertFalse(html.contains("运行情况"));assertEquals(1,html.split("运行结果",-1).length-1);
            if(!description.isBlank())assertTrue(html.contains(description));
        }
        item.resultCases.clear();s.preview=renderer.compose(s);
        assertFalse(renderer.html(s.preview,false).contains("运行结果"));
    }
    @Test void multipleNamedSourcesRetainOrderInDraftHtmlMailAndDocx() throws Exception {
        var s=complete();var item=s.items.getFirst();
        // Names deliberately sort differently from insertion order.
        item.sources.clear();item.sources.add(new JavaSource("Zebra.java","class Zebra {}"));item.sources.add(new JavaSource("Alpha.java","class Alpha {}"));
        var repository=new EncryptedDraftRepository(temp.resolve("drafts"),crypto());repository.save(s);
        s=repository.load(s.id);
        assertEquals(List.of("Zebra.java","Alpha.java"),s.items.getFirst().sources.stream().map(source->source.name).toList());
        s.preview=new SubmissionService(repository).generate(s);
        assertTrue(new PreviewValidator().validate(s).isEmpty());
        String html=new HtmlSubmissionRenderer().html(s.preview,false);
        assertSourceOrder(html);
        var client=new JakartaMailClient();var message=client.compose(Session.getInstance(client.properties(s.account)),s.account,s.preview);
        assertSourceOrder(((Multipart)message.getContent()).getBodyPart(0).getContent().toString());
        Path output=temp.resolve("sources.docx");new ApachePoiDocxExporter().export(s.preview,output);
        try(var doc=new XWPFDocument(Files.newInputStream(output))) {
            assertSourceOrder(doc.getParagraphs().stream().map(XWPFParagraph::getText).reduce("",(a,b)->a+"\n"+b));
        }
        s.preview.blocks.removeIf(b->b.role.equals("source") && b.text.contains("Alpha"));
        assertTrue(new PreviewValidator().validate(s).stream().anyMatch(e->e.contains("每份 Java 代码")));
    }
    private void assertSourceOrder(String text) {
        assertTrue(text.contains("Zebra.java"));
        assertTrue(text.indexOf("Zebra.java")<text.indexOf("class Zebra {}"));
        assertTrue(text.indexOf("class Zebra {}")<text.indexOf("Alpha.java"));
        assertTrue(text.indexOf("Alpha.java")<text.indexOf("class Alpha {}"));
    }
    @Test void everySourceNeedsNameAndContentAndImportUsesFilename() throws Exception {
        var s=complete();var source=new JavaSource("","class Helper {}");s.items.getFirst().sources.add(source);
        assertTrue(new SubmissionValidator().validate(s).stream().anyMatch(e->e.contains("第 2 份") && e.contains("名称")));
        source.name="Helper.java";source.code=" ";
        assertTrue(new SubmissionValidator().validate(s).stream().anyMatch(e->e.contains("第 2 份") && e.contains("缺少源代码")));
        Path file=temp.resolve("工具.java");Files.writeString(file,"\uFEFFclass Helper {}",StandardCharsets.UTF_8);
        var imported=new SubmissionService(null).readJavaSource(file);
        assertEquals("工具.java",imported.name);assertEquals("class Helper {}",imported.code);
        s.items.getFirst().sources.set(1,imported);assertTrue(new SubmissionValidator().validate(s).isEmpty());
    }
    @Test void oldDraftSourceIsRetainedAndNeedsUserProvidedName() throws Exception {
        var s=complete();var json=JsonUtil.MAPPER.valueToTree(s);
        var item=(com.fasterxml.jackson.databind.node.ObjectNode)json.get("items").get(0);
        item.remove("sources");item.put("sourceCode","class Legacy {}");
        var loaded=JsonUtil.MAPPER.treeToValue(json,Submission.class);
        assertEquals(1,loaded.items.getFirst().sources.size());
        assertEquals("class Legacy {}",loaded.items.getFirst().sources.getFirst().code);
        assertTrue(new SubmissionValidator().validate(loaded).stream().anyMatch(e->e.contains("名称")));
        assertFalse(JsonUtil.MAPPER.writeValueAsString(loaded).contains("sourceCode"));
    }
    @Test void aesRoundtripRandomNonceAndTamperRejection() throws Exception {
        var c=crypto();byte[] plain="姓名与代码".getBytes(StandardCharsets.UTF_8),a=c.encrypt(plain),b=c.encrypt(plain);
        assertFalse(Arrays.equals(a,b));assertArrayEquals(plain,c.decrypt(a));a[a.length-1]^=1;
        assertThrows(java.security.GeneralSecurityException.class,()->c.decrypt(a));
        assertThrows(java.security.GeneralSecurityException.class,()->c.decrypt(new byte[4]));
    }
    @Test void encryptedDraftRetainsSnapshotsAndEditedPreview() throws Exception {
        var repository=new EncryptedDraftRepository(temp,crypto());var s=complete();s.preview.blocks.add(ContentBlock.text("paragraph","预览新增说明"));repository.save(s);
        var loaded=repository.load(s.id);assertEquals("预览新增说明",loaded.preview.blocks.getLast().text);
        assertArrayEquals(s.items.getFirst().resultCases.getFirst().images.getFirst().data(),loaded.items.getFirst().resultCases.getFirst().images.getFirst().data());
        assertEquals("边界输入",loaded.items.getFirst().resultCases.get(1).description);assertEquals(1,repository.list().size());
        String ciphertext=new String(Files.readAllBytes(temp.resolve(s.id).resolve("draft.enc")),StandardCharsets.ISO_8859_1);assertFalse(ciphertext.contains("public class Main"));
        try(var paths=Files.walk(temp)){assertEquals(1,paths.filter(Files::isRegularFile).count());}
    }
    @Test void validationRejectsMissingIdentityCodeAndEachEmptyCase() throws Exception {
        var s=complete();assertTrue(new SubmissionValidator().validate(s).isEmpty());
        s.profile.studentId="";s.items.getFirst().sources.clear();s.items.getFirst().resultCases.get(1).images.clear();s.profile.assistantEmails.add("TEACHER@example.com");
        String errors=String.join("\n",new SubmissionValidator().validate(s));assertTrue(errors.contains("学号"));assertTrue(errors.contains("源代码"));assertTrue(errors.contains("情况 2"));
        assertTrue(EmailValidator.recipients(s.profile.teacherEmail,s.profile.assistantEmails).stream().anyMatch(e->e.contains("重复")));
        s.items.clear();assertTrue(new SubmissionValidator().validate(s).stream().anyMatch(e->e.contains("至少选择")));
    }
    @Test void namedDraftsDefaultNamesRetentionAndDeletion() throws Exception {
        var repository=new EncryptedDraftRepository(temp,crypto());
        var first=new Submission(); first.draftName="自定义作业"; repository.save(first);
        assertEquals("自定义作业",repository.load(first.id).draftName);
        // Make retention deterministic regardless of the system clock resolution.
        first.modifiedAt="2000-01-01T00:00:00Z";
        var encryption=crypto();
        var store=new EncryptedDraftRepository(temp.resolve("retention"),encryption);
        store.save(first);
        Files.write(temp.resolve("retention").resolve(first.id).resolve("draft.enc"),encryption.encrypt(com.mailassistant.util.JsonUtil.MAPPER.writeValueAsBytes(withOldTime(first))));
        for(int i=0;i<30;i++) store.save(new Submission());
        assertEquals(30,store.list().size());
        assertFalse(Files.exists(temp.resolve("retention").resolve(first.id)));
        var latest=store.list().getFirst();
        assertTrue(latest.title().matches("草稿 \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        var loaded=store.load(latest.id()); loaded.draftName="重新命名"; store.save(loaded);
        assertEquals(30,store.list().size()); assertEquals("重新命名",store.load(loaded.id).draftName);
        store.delete(loaded.id); assertEquals(29,store.list().size());
        assertThrows(IllegalArgumentException.class,()->store.delete("../outside"));
    }
    private static Submission withOldTime(Submission s) { s.modifiedAt="2000-01-01T00:00:00Z"; return s; }
    @Test void profileRejectsNonNumericClassAndMultipleAddressesPerField() throws Exception {
        var s=complete();s.profile.className="一班";
        assertTrue(new SubmissionValidator().validate(s).stream().anyMatch(e->e.contains("只能填写数字")));
        assertFalse(EmailValidator.valid("a@example.com;b@example.com"));
        assertFalse(EmailValidator.valid("a@example.com,b@example.com"));
        assertTrue(EmailValidator.recipients("teacher@example.com",List.of("a@example.com","b@example.com","c@example.com","d@example.com")).stream().anyMatch(e->e.contains("最多")));
    }
    @Test void previewCannotRemoveRequiredScreenshotOrIdentity() throws Exception {
        var s=complete();assertTrue(new PreviewValidator().validate(s).isEmpty());
        s.preview.blocks.removeIf(b->b.role.equals("result:1"));assertTrue(new PreviewValidator().validate(s).stream().anyMatch(e->e.contains("情况 2")));
        s.preview.blocks.removeFirst();assertTrue(new PreviewValidator().validate(s).stream().anyMatch(e->e.contains("首行")));
    }
    @Test void realBankContains173ExercisesAndRequiredUmlMetadata() throws Exception {
        var database=new DatabaseManager(temp.resolve("exercise.db"));var repository=new SQLiteExerciseRepository(database);var chapters=repository.chapters();assertEquals(19,chapters.size());
        int[] expectedCounts={4,9,20,12,15,9,7,9,14,6,6,10,12,6,8,9,4,7,6};
        for(int i=0;i<expectedCounts.length;i++)assertEquals(expectedCounts[i],repository.findByChapter(i+1).size(),"Chapter "+(i+1));
        int total=0,uml=0,assets=0;
        for(var chapter:chapters)for(var ex:repository.findByChapter(chapter.id())){total++;if(ex.requireUml())uml++;assets+=ex.assets().size();var doc=new PreviewDocument();doc.blocks.addAll(ex.content());doc.assets.putAll(ex.assets());assertDoesNotThrow(()->new HtmlSubmissionRenderer().html(doc,true));}
        assertEquals(173,total);assertEquals(6,uml);assertEquals(38,assets);
        try(var c=database.connect();var st=c.createStatement()){assertThrows(java.sql.SQLException.class,()->st.executeUpdate("DELETE FROM exercise"));}
    }
    @Test void htmlEscapesTextUsesCidAndPreservesOrder() throws Exception {
        var s=complete();String html=new HtmlSubmissionRenderer().html(s.preview,false);
        assertTrue(html.contains("&lt;script&gt;"));assertFalse(html.contains("<script>"));assertTrue(html.contains("src='cid:first'"));assertFalse(html.contains("file:"));
        assertTrue(html.indexOf("正常输入")<html.indexOf("cid:first"));assertTrue(html.indexOf("cid:first")<html.indexOf("边界输入"));assertTrue(html.indexOf("边界输入")<html.indexOf("cid:second"));
        assertTrue(new HtmlSubmissionRenderer().html(s.preview,true).contains("data:image/png;base64,"));
    }
    @Test void mimeContainsOnlyInlineImagesAndEditedBody() throws Exception {
        var s=complete();s.preview.blocks.add(ContentBlock.text("paragraph","edited body"));var client=new JakartaMailClient();
        var message=client.compose(Session.getInstance(client.properties(s.account)),s.account,s.preview);assertEquals(s.preview.subject,message.getSubject());assertEquals(2,message.getAllRecipients().length);
        Multipart multipart=(Multipart)message.getContent();assertEquals(3,multipart.getCount());assertTrue(multipart.getBodyPart(0).getContent().toString().contains("edited body"));
        for(int i=1;i<multipart.getCount();i++){assertEquals(Part.INLINE,multipart.getBodyPart(i).getDisposition());assertNotNull(multipart.getBodyPart(i).getHeader("Content-ID"));assertNull(multipart.getBodyPart(i).getFileName());}
        s.account.tls="STARTTLS";assertEquals("true",client.properties(s.account).getProperty("mail.smtp.starttls.required"));assertEquals("true",client.properties(s.account).getProperty("mail.smtp.ssl.checkserveridentity"));
    }
    @Test void docxUsesEditedPreviewOrderAndNativeTable() throws Exception {
        var s=complete();s.preview.blocks.add(ContentBlock.text("paragraph","edited body"));var table=ContentBlock.text("table","");table.rows.add(List.of(List.of(ContentBlock.Inline.text("表头"))));table.rows.add(List.of(List.of(ContentBlock.Inline.text("值"))));s.preview.blocks.add(table);
        Path output=temp.resolve("work.docx");new ApachePoiDocxExporter().export(s.preview,output);
        try(var doc=new XWPFDocument(Files.newInputStream(output))){String text=doc.getParagraphs().stream().map(XWPFParagraph::getText).reduce("",(a,b)->a+"\n"+b);assertTrue(text.contains("edited body"));assertTrue(text.indexOf("正常输入")<text.indexOf("边界输入"));assertEquals(2,doc.getParagraphs().stream().flatMap(p->p.getRuns().stream()).mapToInt(r->r.getEmbeddedPictures().size()).sum());assertEquals(1,doc.getTables().size());assertEquals("表头",doc.getTables().getFirst().getRow(0).getCell(0).getText());}
    }
    @Test void mailPreparationDoesNotSendAndConfirmationUsesSnapshot() throws Exception {
        Map<String,byte[]> secrets=new HashMap<>();SecretStore store=new SecretStore(){public void save(String n,byte[] v){secrets.put(n,v.clone());}public byte[] load(String n){return secrets.containsKey(n)?secrets.get(n).clone():null;}public void delete(String n){secrets.remove(n);}};
        CredentialService credentials=new CredentialService(store);credentials.save("student@example.com","test-token");
        List<PreviewDocument> sent=new ArrayList<>();MailClient fake=(account,secret,document)->{assertEquals("test-token",secret);sent.add(document);};
        var service=new MailService(fake,credentials,new SubmissionService(new EncryptedDraftRepository(temp,crypto())));var s=complete();var prepared=service.prepare(s);assertTrue(sent.isEmpty());
        s.preview.to="changed@example.com";service.sendConfirmed(prepared);assertEquals("teacher@example.com",sent.getFirst().to);
    }
    @Test void invalidImagesAndUnsupportedStructuredContentFail() throws Exception {
        assertThrows(IOException.class,()->new ImageValidator().decode("x","x.png","not an image".getBytes()));
        assertThrows(IllegalArgumentException.class,()->new StructuredContentReader().read("{\"schemaVersion\":2,\"blocks\":[]}",Map.of()));
        assertThrows(IllegalArgumentException.class,()->new StructuredContentReader().read("{\"schemaVersion\":1,\"blocks\":[{\"type\":\"image\",\"assetId\":\"missing\"}]}",Map.of()));
    }
    @Test void applicationRootDoesNotDependOnWorkingDirectory() throws Exception {
        String old=System.getProperty("mailassistant.home");
        try{System.setProperty("mailassistant.home",temp.toAbsolutePath().toString());var paths=PathConfig.discover();paths.initialize();assertEquals(temp.resolve("data"),paths.data());assertTrue(Files.isDirectory(paths.exports()));}
        finally{if(old==null)System.clearProperty("mailassistant.home");else System.setProperty("mailassistant.home",old);}
    }
    @Test @EnabledOnOs(OS.WINDOWS) void dpapiProtectsMasterKeyAndAccountSecret() throws Exception {
        var store=new WindowsSecretStore(temp);byte[] secret="test-only-secret".getBytes(StandardCharsets.UTF_8);store.save("test",secret);assertArrayEquals(secret,store.load("test"));assertFalse(Arrays.equals(secret,Files.readAllBytes(temp.resolve("test.dpapi"))));store.delete("test");assertNull(store.load("test"));
    }
    @Test void movingScreenshotIntoAnotherCaseIsRejected() throws Exception {
        var s=complete();var first=s.preview.blocks.stream().filter(b->b.role.equals("result:0")).findFirst().orElseThrow();
        s.preview.blocks.remove(first);s.preview.blocks.add(first);
        assertTrue(new PreviewValidator().validate(s).stream().anyMatch(e->e.contains("对应运行情况")));
    }
    @Test void inlineFormulaAndTableRetainLatexAndOrder() throws Exception {
        String json="""
            {"schemaVersion":1,"blocks":[
              {"type":"paragraph","inlines":[{"type":"text","text":"计算"},{"type":"formula","latex":"x^2","assetId":"f","alt":"x 平方"}]},
              {"type":"table","headers":[{"inlines":[{"type":"text","text":"变量"}]}],"rows":[[{"inlines":[{"type":"formula","latex":"x^2","assetId":"f","alt":"x 平方"}]}]]}
            ]}
            """;
        var asset=image("f");var blocks=new StructuredContentReader().read(json,Map.of("f",asset));
        assertEquals("x^2",blocks.getFirst().inlines.get(1).latex());assertTrue(blocks.get(1).searchableText().contains("x^2"));
        var doc=new PreviewDocument();doc.blocks=blocks;doc.assets.put("f",asset);String html=new HtmlSubmissionRenderer().html(doc,false);assertTrue(html.contains("<table"));assertEquals(Set.of("f"),HtmlSubmissionRenderer.referencedAssets(doc));
        new ApachePoiDocxExporter().export(doc,temp.resolve("formula.docx"));
    }

}
