package com.mailassistant;

import com.mailassistant.application.AppServices;
import com.mailassistant.config.PathConfig;
import com.mailassistant.domain.model.Submission;
import com.mailassistant.ui.controller.MainController;
import com.mailassistant.desktop.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.*;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in, opens only this application's test windows, uses synthetic data, never SMTP. */
@EnabledIfSystemProperty(named="uiSmoke",matches="true")
@EnabledOnOs(OS.WINDOWS)
class UiSmokeTest {
    @TempDir Path temp;
    private Stage stage; private MainController controller;
    static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task=new FutureTask<>(action);Platform.runLater(task);return task.get(20,TimeUnit.SECONDS);
    }
    @Test void renderAllMainScreens() throws Exception {
        CountDownLatch boot=new CountDownLatch(1);Platform.startup(()->{Platform.setImplicitExit(false);boot.countDown();});assertTrue(boot.await(15,TimeUnit.SECONDS));
        var services=new AppServices(new PathConfig(temp));
        try {
            fx(()->{stage=new Stage();controller=new MainController(stage,services);Scene scene=new Scene(controller.root(),1280,860);scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());stage.setScene(scene);stage.show();controller.start();return null;});
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);
            while(fx(()->controller.root().isDisabled()) && System.nanoTime()<deadline)Thread.sleep(100);
            assertFalse(fx(()->controller.root().isDisabled()));snapshot("01-exercises");
            verifyRevertedSelection();
            fx(()->{var field=MainController.class.getDeclaredField("submission");field.setAccessible(true);field.set(controller,CoreWorkflowTest.complete());return null;});
            click("02   编辑作业");verifySourceControls();snapshot("02-editor");
            click("03   预览与提交");Thread.sleep(700);verifyPreviewNavigation();snapshot("03-preview");
            click("个人中心");verifyProfileControls();snapshot("04-settings");
            fx(()->{stage.setWidth(1040);stage.setHeight(720);return null;});Thread.sleep(400);snapshot("05-settings-small");
            assertTrue(fx(()->controller.root().getWidth()) < 1100);
            assertTrue(Files.size(Path.of("target/ui-smoke/03-preview.png"))>5000);
            verifyDraftControls(services);
            verifyPreferencesAndDraftCleanup(services);
            verifyTrayLifecycle();
        } finally {fx(()->{if(controller!=null)controller.close();if(stage!=null)stage.close();return null;});Platform.exit();}
    }
    private void verifyPreviewNavigation() throws Exception {
        fx(()->{
            var blocks=(javafx.scene.control.ListView<?>)controller.root().lookup("#body-blocks");
            var web=(javafx.scene.web.WebView)controller.root().lookup("#body-preview");
            assertEquals(javafx.concurrent.Worker.State.SUCCEEDED,web.getEngine().getLoadWorker().getState());
            blocks.getSelectionModel().selectLast();
            assertTrue(((Number)web.getEngine().executeScript("window.scrollY")).doubleValue()>0);
            blocks.getSelectionModel().selectFirst();
            assertTrue(Math.abs(((Number)web.getEngine().executeScript("document.getElementById('preview-block-0').getBoundingClientRect().top")).doubleValue())<2);
            blocks.getSelectionModel().selectLast();
            return null;
        });
        click("刷新预览");
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
        while(fx(()->((javafx.scene.web.WebView)controller.root().lookup("#body-preview")).getEngine().getLoadWorker().getState()!=javafx.concurrent.Worker.State.SUCCEEDED) && System.nanoTime()<deadline)Thread.sleep(50);
        fx(()->{
            var web=(javafx.scene.web.WebView)controller.root().lookup("#body-preview");
            assertTrue(((Number)web.getEngine().executeScript("window.scrollY")).doubleValue()>0,"Refresh should keep the selected block in view");
            web.getEngine().executeScript("window.scrollTo(0,0)");
            controller.root().lookup("#body-blocks").getOnMouseClicked().handle(null);
            assertTrue(((Number)web.getEngine().executeScript("window.scrollY")).doubleValue()>0,"Clicking the selected block again should jump back");
            return null;
        });
    }
    private void verifySourceControls() throws Exception {
        fx(()->{
            ((Button)controller.root().lookup("#add-source")).fire();
            var name=(javafx.scene.control.TextField)controller.root().lookup("#source-name-1");
            var code=(javafx.scene.control.TextArea)controller.root().lookup("#source-code-1");
            assertEquals("",name.getText());name.setText("Helper.java");code.setText("class Helper {}");
            ((Button)controller.root().lookup("#add-source")).fire();
            ((Button)controller.root().lookup("#remove-source-2")).fire();
            assertNull(controller.root().lookup("#source-name-2"));
            assertEquals("Helper.java",((javafx.scene.control.TextField)controller.root().lookup("#source-name-1")).getText());
            var field=MainController.class.getDeclaredField("submission");field.setAccessible(true);var s=(Submission)field.get(controller);
            assertNull(s.preview);assertEquals(2,s.items.getFirst().sources.size());
            assertEquals("class Helper {}",s.items.getFirst().sources.get(1).code);
            s.preview=new com.mailassistant.preview.HtmlSubmissionRenderer().compose(s);
            return null;
        });
    }
    private void awaitIdle() throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);
        while(fx(()->controller.root().isDisabled()) && System.nanoTime()<deadline)Thread.sleep(50);
        assertFalse(fx(()->controller.root().isDisabled()));
    }
    private void verifyRevertedSelection() throws Exception {
        fx(()->{
            var selected=(javafx.scene.control.CheckBox)controller.root().lookup(".check-box");
            assertTrue(controller.mayExit());
            selected.fire();
            answerDialog(null,javafx.scene.control.ButtonType.CANCEL);
            assertFalse(controller.mayExit(),"An actual unsaved selection must prompt");
            selected.fire();
            assertFalse(selected.isSelected());
            // Close any unexpected prompt so a regression fails without hanging.
            Platform.runLater(()->javafx.stage.Window.getWindows().stream()
                    .filter(w->w!=stage && w.isShowing() && w.getScene().getRoot() instanceof javafx.scene.control.DialogPane)
                    .findFirst().ifPresent(w->{var pane=(javafx.scene.control.DialogPane)w.getScene().getRoot();
                        pane.getButtonTypes().stream().filter(t->t.getButtonData()==javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE)
                                .findFirst().ifPresent(t->((Button)pane.lookupButton(t)).fire());}));
            assertTrue(controller.mayExit(),"Selecting and deselecting must not prompt to save an empty draft");
            return null;
        });
        click("个人中心");
        fx(()->{
            ((javafx.scene.control.TextField)controller.root().lookup("#class-number")).setText("2301");
            answerDialog(null,javafx.scene.control.ButtonType.CANCEL);
            assertFalse(controller.mayExit(),"Unsaved profile edits must still prompt even with zero exercises");
            return null;
        });
    }
    private void verifyProfileControls() throws Exception {
        fx(()->{
            var student=(javafx.scene.control.TextField)controller.root().lookup("#student-id");
            student.setText("00123");student.appendText("abc");assertEquals("00123",student.getText());
            student.replaceText(0,student.getLength(),"12号");assertEquals("00123",student.getText());
            student.setText("2026001");
            var provider=(javafx.scene.control.ComboBox<String>)controller.root().lookup("#mail-provider");
            var mailbox=(javafx.scene.control.TextField)controller.root().lookup("#mail-local-part");
            assertEquals(java.util.List.of("QQ","163"),provider.getItems());
            mailbox.setText("00123");provider.setValue("163");
            var submissionField=MainController.class.getDeclaredField("submission");submissionField.setAccessible(true);
            var current=(Submission)submissionField.get(controller);
            assertEquals("00123@163.com",current.account.address);assertEquals("smtp.163.com",current.account.host);
            provider.setValue("QQ");assertEquals("00123@qq.com",current.account.address);
            mailbox.appendText("@qq.com");assertEquals("00123",mailbox.getText());
            assertFalse(((javafx.scene.control.CheckBox)controller.root().lookup("#auto-delete-draft")).isSelected());
            assertEquals("最小化至托盘",((javafx.scene.control.ComboBox<?>)controller.root().lookup("#exit-mode")).getValue());
            var number=(javafx.scene.control.TextField)controller.root().lookup("#class-number");
            number.setText("00123");number.appendText("abc");assertEquals("00123",number.getText());
            number.replaceText(0,number.getLength(),"12班");assertEquals("00123",number.getText());
            number.setText("");assertEquals("",number.getText());number.setText("2301");
            return null;
        });
        click("+");click("+");
        fx(()->{
            assertNotNull(controller.root().lookup("#assistant-email-2"));
            var add=controller.root().lookupAll(".circle-button").stream().map(n->(Button)n).filter(b->b.getText().equals("+")).findFirst().orElseThrow();
            assertTrue(add.isDisabled());
            ((javafx.scene.control.TextField)controller.root().lookup("#assistant-email-2")).setText("third@example.com");
            return null;
        });
        click("−");
        fx(()->{
            assertNull(controller.root().lookup("#assistant-email-2"));
            assertEquals("third@example.com",((javafx.scene.control.TextField)controller.root().lookup("#assistant-email-1")).getText());
            assertFalse(controller.root().lookupAll(".button").stream().anyMatch(n->((Button)n).getText().equals("新建作业")));
            return null;
        });
    }
    private void answerDialog(String name, javafx.scene.control.ButtonType response) {
        Platform.runLater(()->{
            var window=javafx.stage.Window.getWindows().stream().filter(w->w!=stage && w.isShowing() && w.getScene().getRoot() instanceof javafx.scene.control.DialogPane).findFirst().orElseThrow();
            var pane=(javafx.scene.control.DialogPane)window.getScene().getRoot();
            if(name!=null) ((javafx.scene.control.TextField)pane.lookup(".text-field")).setText(name);
            var target=pane.getButtonTypes().stream().filter(t->t==response || t.getButtonData()==response.getButtonData()).findFirst().orElseThrow();
            ((Button)pane.lookupButton(target)).fire();
        });
    }
    private void verifyDraftControls(AppServices services) throws Exception {
        click("02   编辑作业");
        fx(()->{answerDialog("界面测试草稿",javafx.scene.control.ButtonType.OK);
            controller.root().lookupAll(".button").stream().map(n->(Button)n).filter(b->b.getText().equals("保存草稿")).findFirst().orElseThrow().fire();return null;});
        awaitIdle();assertEquals("界面测试草稿",services.submissions.drafts().getFirst().title());
        click("草稿箱");awaitIdle();snapshot("08-drafts-saved");
        fx(()->{answerDialog(null,javafx.scene.control.ButtonType.CANCEL);
            controller.root().lookupAll(".button").stream().map(n->(Button)n).filter(b->b.getText().equals("删除")).findFirst().orElseThrow().fire();return null;});
        assertEquals(1,services.submissions.drafts().size());
        click("打开");awaitIdle();
        click("草稿箱");awaitIdle();
        fx(()->{answerDialog(null,javafx.scene.control.ButtonType.OK);
            controller.root().lookupAll(".button").stream().map(n->(Button)n).filter(b->b.getText().equals("删除")).findFirst().orElseThrow().fire();return null;});
        awaitIdle();assertTrue(services.submissions.drafts().isEmpty());snapshot("09-drafts-empty");
    }
    private void verifyPreferencesAndDraftCleanup(AppServices services) throws Exception {
        click("个人中心");
        fx(()->{
            ((javafx.scene.control.CheckBox)controller.root().lookup("#auto-delete-draft")).setSelected(true);
            ((javafx.scene.control.TextField)controller.root().lookup("#homework-folder")).setText(temp.toString());
            ((javafx.scene.control.ComboBox<String>)controller.root().lookup("#exit-mode")).setValue("直接退出");
            return null;
        });
        click("保存设置");awaitIdle();
        var saved=services.profiles.load().preferences();
        assertTrue(saved.autoDeleteDraft);assertFalse(saved.minimizeToTray);assertEquals(temp.toString(),saved.homeworkFolder);
        assertFalse(fx(controller::minimizeToTray));
        var s=fx(()->{var field=MainController.class.getDeclaredField("submission");field.setAccessible(true);return (Submission)field.get(controller);});
        services.submissions.save(s);
        for(boolean enabled:new boolean[]{false,true}) {
            fx(()->{
                var field=MainController.class.getDeclaredField("preferences");field.setAccessible(true);
                ((com.mailassistant.domain.model.AppPreferences)field.get(controller)).autoDeleteDraft=enabled;
                var method=MainController.class.getDeclaredMethod("outputCompleted",String.class,String.class);method.setAccessible(true);
                Platform.runLater(()->{try{method.invoke(controller,"测试导出完成","测试正文");}catch(Exception e){throw new RuntimeException(e);}});
                return null;
            });
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            boolean dismissed=false;
            while(!dismissed && System.nanoTime()<deadline) {
                dismissed=fx(()->{
                    var window=javafx.stage.Window.getWindows().stream().filter(w->w!=stage && w.isShowing() && w.getScene().getRoot() instanceof javafx.scene.control.DialogPane).findFirst();
                    if(window.isEmpty())return false;
                    var pane=(javafx.scene.control.DialogPane)window.get().getScene().getRoot();
                    assertEquals("测试导出完成",pane.getHeaderText());
                    ((Button)pane.lookupButton(javafx.scene.control.ButtonType.OK)).fire();return true;
                });
                if(!dismissed)Thread.sleep(50);
            }
            assertTrue(dismissed);awaitIdle();
            assertEquals(enabled?0:1,services.submissions.drafts().size());
        }
        fx(()->{assertTrue(controller.mayExit(),"Completed and deleted drafts must not prompt to be saved again");return null;});
    }
    private void verifyTrayLifecycle() throws Exception {
        class TestTray implements TrayService {
            boolean installed;
            Runnable show, profile, drafts, quit;
            public boolean available() { return installed; }
            public void install(Runnable show, Runnable profile, Runnable drafts, Runnable quit) {
                installed=true; this.show=show; this.profile=profile; this.drafts=drafts; this.quit=quit;
            }
            public void close() { installed=false; }
        }
        TestTray tray=new TestTray();
        int[] exitChecks={0};
        boolean[] minimize={true};
        DesktopLifecycle lifecycle=fx(()->{
            new AppIconProvider().apply(stage);
            assertEquals(5,stage.getIcons().size());
            int[] sizes={16,32,48,128,256};
            for(int i=0;i<sizes.length;i++) {
                assertEquals(sizes[i],stage.getIcons().get(i).getWidth());
                assertEquals(sizes[i],stage.getIcons().get(i).getHeight());
            }
            return new DesktopLifecycle(stage,tray,()->{exitChecks[0]++;return false;},controller::showProfile,controller::showDrafts,()->minimize[0]);
        });
        try {
            fx(()->{stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));return null;});
            assertFalse(fx(stage::isShowing));
            assertEquals(0,exitChecks[0],"Hiding must not prompt to discard or save work");
            tray.profile.run();
            assertTrue(fx(stage::isShowing));
            assertTrue(fx(()->controller.root().lookupAll(".page-title").stream().anyMatch(n->n instanceof javafx.scene.control.Label l && l.getText().equals("个人中心"))));
            tray.drafts.run();
            awaitIdle();
            assertTrue(fx(()->controller.root().lookupAll(".page-title").stream().anyMatch(n->n instanceof javafx.scene.control.Label l && l.getText().equals("草稿箱"))));
            snapshot("06-drafts");
            fx(()->{stage.hide();return null;});
            tray.show.run();
            assertTrue(fx(stage::isShowing));
            fx(()->{stage.hide();return null;});
            tray.quit.run();
            assertFalse(fx(stage::isShowing),"Tray exit must not restore the main window");
            assertEquals(1,exitChecks[0]);
            assertTrue(tray.available(),"Cancelled exit must retain the tray");
            fx(()->{stage.show();minimize[0]=false;stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));return null;});
            assertTrue(fx(stage::isShowing),"Direct exit must run the unsaved-work check instead of hiding");
            assertEquals(2,exitChecks[0]);
        } finally { fx(()->{lifecycle.close();return null;}); }
        fx(()->{
            stage.show();
            DesktopLifecycle fallback=new DesktopLifecycle(stage,TrayService.disabled(),()->false,()->{},()->{});
            stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));
            assertTrue(stage.isShowing(),"Without a tray a cancelled exit must keep the window visible");
            fallback.close();Platform.setImplicitExit(false);return null;
        });
        AwtTrayService nativeTray=new AwtTrayService(stage);
        try {
            nativeTray.install(()->{},()->{},()->{},()->{});
            if(java.awt.SystemTray.isSupported())assertTrue(nativeTray.available());
        } finally { nativeTray.close(); }
        assertFalse(nativeTray.available());
        fx(()->{
            stage.hide();
            int[] selected={0};
            TrayMenu menu=new TrayMenu(stage,()->selected[0]=1,()->selected[0]=2,()->selected[0]=3,()->selected[0]=4);
            try {
                var bounds=javafx.stage.Screen.getPrimary().getVisualBounds();
                menu.showAt(new javafx.geometry.Point2D(bounds.getMaxX(),bounds.getMaxY()));
                var window=javafx.stage.Window.getWindows().stream().filter(w->w.getScene()!=null && w.getScene().getRoot().getStyleClass().contains("tray-root")).findFirst().orElseThrow();
                assertTrue(window.isShowing(),"Tray menu must work while the main window is hidden");
                assertTrue(window.getX()+window.getWidth()<=bounds.getMaxX()+1);
                assertTrue(window.getY()+window.getHeight()<=bounds.getMaxY()+1);
                assertTrue(Double.isFinite(window.getX()) && Double.isFinite(window.getY()));
                assertEquals(bounds.getMaxX(),window.getX()+window.getWidth(),2,"First opening must anchor at the tray, not the top-left corner");
                assertEquals(bounds.getMaxY(),window.getY()+window.getHeight(),2);
                window.hide();
                menu.showAt(new javafx.geometry.Point2D(bounds.getMaxX()-30,bounds.getMaxY()-30));
                assertEquals(bounds.getMaxX()-30,window.getX()+window.getWidth(),2);
                assertEquals(bounds.getMaxY()-30,window.getY()+window.getHeight(),2);
                var buttons=window.getScene().getRoot().lookupAll(".tray-item").stream().map(n->(Button)n).toList();
                assertEquals(java.util.Set.of("主界面","个人信息","草稿箱","退出"),buttons.stream().map(Button::getText).collect(java.util.stream.Collectors.toSet()));
                saveSnapshot(window.getScene().getRoot(),"07-tray-menu");
                buttons.stream().filter(b->b.getText().equals("个人信息")).findFirst().orElseThrow().fire();
                assertEquals(2,selected[0]);assertFalse(window.isShowing());
            } finally {menu.close();stage.show();}
            return null;
        });
    }
    private void click(String text)throws Exception {
        fx(()->{var b=controller.root().lookupAll(".button").stream().filter(n->n instanceof Button button && button.getText().equals(text)).map(n->(Button)n).findFirst().orElseThrow();b.fire();controller.root().applyCss();controller.root().layout();return null;});
    }
    private void snapshot(String name)throws Exception {
        fx(()->{saveSnapshot(controller.root(),name);return null;});
    }
    private void saveSnapshot(javafx.scene.Parent root,String name)throws Exception {
        root.applyCss();root.layout();var img=root.snapshot(null,null);BufferedImage buffered=new BufferedImage((int)img.getWidth(),(int)img.getHeight(),BufferedImage.TYPE_INT_ARGB);var pixels=img.getPixelReader();for(int y=0;y<buffered.getHeight();y++)for(int x=0;x<buffered.getWidth();x++)buffered.setRGB(x,y,pixels.getArgb(x,y));Path path=Path.of("target/ui-smoke",name+".png");Files.createDirectories(path.getParent());ImageIO.write(buffered,"png",path.toFile());
    }
}
