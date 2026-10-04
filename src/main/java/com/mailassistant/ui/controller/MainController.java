package com.mailassistant.ui.controller;

import com.mailassistant.application.*;
import com.mailassistant.domain.model.*;
import com.mailassistant.preview.*;
import com.mailassistant.mail.provider.MailProviderRegistry;
import com.mailassistant.repository.UserProfileRepository;
import com.mailassistant.util.JsonUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import static com.mailassistant.ui.component.Ui.*;

public final class MainController implements AutoCloseable {
    private final Stage stage;
    private final AppServices services;
    private final BorderPane root = new BorderPane();
    private final VBox page = new VBox(20);
    private final Label status = label("离线就绪", "status"), selection = label("尚未选择题目", "sidebar-note");
    private final List<Button> navigation = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "mailassistant-worker"); t.setDaemon(true); return t; });
    private Submission submission = new Submission();
    private AppPreferences preferences = new AppPreferences();
    private List<Chapter> chapters = List.of();
    private List<Exercise> exercises = List.of();
    private boolean dirty, busy, loading;
    private com.fasterxml.jackson.databind.JsonNode cleanState;

    private void markClean() {
        cleanState = JsonUtil.MAPPER.valueToTree(submission);
        dirty = false;
    }

    public MainController(Stage stage, AppServices services) {
        this.stage=stage; this.services=services;
        root.getStyleClass().add("app-root");
        VBox sidebar = new VBox(12); sidebar.getStyleClass().add("sidebar"); sidebar.setPrefWidth(218);
        sidebar.getChildren().addAll(label("M / A", "brand-mark"), label("MailAssistant", "brand"), label("让作业提交井然有序", "sidebar-note"), new Separator());
        String[] titles = {"01   选择习题", "02   编辑作业", "03   预览与提交"};
        for (int i=0;i<titles.length;i++) {
            final int target=i; Button b = button(titles[i], "nav-button", () -> navigate(target)); b.setMaxWidth(Double.MAX_VALUE); navigation.add(b); sidebar.getChildren().add(b);
        }
        Region spring = new Region(); VBox.setVgrow(spring, Priority.ALWAYS);
        Button profile = button("个人中心", "sidebar-button", this::showProfile);
        navigation.add(profile);
        sidebar.getChildren().addAll(spring, selection, button("草稿箱", "sidebar-button", this::showDrafts), profile, label("LOCAL FIRST\n资料与草稿仅存于本机", "sidebar-note"));
        ScrollPane sidebarScroll = scroll(sidebar); sidebarScroll.setFitToHeight(true); sidebarScroll.setPrefWidth(218);
        sidebarScroll.getStyleClass().add("sidebar-scroll"); root.setLeft(sidebarScroll); page.getStyleClass().add("page");root.setCenter(page);
        HBox footer = row(status, spacer(), label("v1.0.0", "muted")); footer.getStyleClass().add("footer"); root.setBottom(footer);
    }
    public BorderPane root() { return root; }
    public boolean minimizeToTray() { return preferences.minimizeToTray; }
    public void showProfile() { if (!busy) navigate(3); }
    public void showDrafts() {
        if (busy) return;
        page.getChildren().clear();
        navigation.forEach(button -> button.getStyleClass().remove("active"));
        heading("DRAFTS", "草稿箱", "最多保留 30 条草稿；超出时自动清理保存时间最早的草稿。");
        VBox drafts = new VBox(12); content(scroll(drafts));
        run("正在读取草稿列表…", services.submissions::drafts, items -> {
            drafts.getChildren().add(label("已保存 " + items.size() + " / 30 条", "badge"));
            if (items.isEmpty()) drafts.getChildren().add(card(label("暂无草稿", "card-title"), label("在「02 编辑作业」点击「保存草稿」即可保存。", "muted")));
            for (var info : items) {
                VBox details = new VBox(7, label(info.title(), "card-title"), label("保存时间：" + draftTime(info.modifiedAt()), "muted"));
                HBox.setHgrow(details, Priority.ALWAYS);
                drafts.getChildren().add(card(row(details,
                        button("打开", "primary", () -> openDraft(info.id())),
                        button("删除", "quiet", () -> {
                            if (!confirm("删除草稿？", "确认删除「" + info.title() + "」？删除后无法恢复。")) return;
                            run("正在删除草稿…", () -> { services.submissions.deleteDraft(info.id()); return true; }, v -> {
                                if (submission.id.equals(info.id())) { dirty = true; cleanState = null; }
                                showDrafts();
                            });
                        }))));
            }
        });
    }
    private String draftTime(String timestamp) {
        try { return java.time.Instant.parse(timestamp).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
        catch (Exception e) { return "未知"; }
    }
    public void start() {
        run("正在读取个人设置与章节…", () -> new Object[]{services.profiles.load(), services.exercises.chapters()}, result -> {
            var settings = (UserProfileRepository.Settings) result[0]; submission.profile=settings.profile(); submission.account=settings.account();
            preferences=settings.preferences();
            @SuppressWarnings("unchecked") List<Chapter> loaded=(List<Chapter>)result[1]; chapters=loaded;
            if (!chapters.isEmpty()) { submission.chapter=chapters.getFirst(); markClean(); loadExercises(() -> navigate(0)); }
            else { markClean(); navigate(0); }
        });
    }
    private void changed() {
        if (loading) return;
        submission.preview=null;
        // An empty selection may be a return to the initial state, but deleting
        // saved items or changing personal settings must still count as edits.
        dirty = !submission.items.isEmpty() || !Objects.equals(cleanState, JsonUtil.MAPPER.valueToTree(submission));
        updateSelection(); status.setText(dirty ? "有未保存的更改 · 可随时保存加密草稿" : "离线就绪");
    }
    private void previewChanged() { dirty=true; status.setText("预览已修改 · 导出和发送将使用此版本"); }
    private void updateSelection() { selection.setText((submission.chapter==null ? "未选章节" : submission.chapter.title()) + "\n已选择 " + submission.items.size() + " 道题"); }
    private void navigate(int index) {
        page.getChildren().clear();
        for (int i=0;i<navigation.size();i++) { navigation.get(i).getStyleClass().remove("active"); if (i==index) navigation.get(i).getStyleClass().add("active"); }
        switch(index) { case 0 -> exercisesPage(); case 1 -> editorPage(null); case 2 -> previewPage(); default -> profilePage(); }
        updateSelection();
    }
    private void heading(String eyebrow, String title, String detail) {
        page.getChildren().add(new VBox(7, label(eyebrow, "eyebrow"), label(title, "page-title"), label(detail, "muted")));
    }
    private void content(Node node) { page.getChildren().add(node); VBox.setVgrow(node, Priority.ALWAYS); }
    private void loadExercises(Runnable next) {
        if (submission.chapter==null) { exercises=List.of(); next.run(); return; }
        run("正在读取离线习题…", () -> services.exercises.findByChapter(submission.chapter.id()), result -> { exercises=result; next.run(); });
    }
    private void exercisesPage() {
        heading("YOUR COURSEWORK", "从一道习题开始", "选择章节与本次要提交的题目，题目插图和公式会一同保留。");
        ComboBox<Chapter> chapterBox = new ComboBox<>(); chapterBox.getItems().setAll(chapters); chapterBox.setValue(submission.chapter); chapterBox.setPrefWidth(175);
        TextField search = new TextField(); search.setPromptText("搜索题号、标题或正文"); HBox.setHgrow(search, Priority.ALWAYS);
        page.getChildren().add(row(chapterBox, search, button("编辑已选题目 →", "primary", () -> navigate(1))));
        VBox cards = new VBox(12); content(scroll(cards));
        Runnable refresh = () -> {
            cards.getChildren().clear(); String term=search.getText().strip().toLowerCase(Locale.ROOT);
            for (Exercise ex : exercises) {
                String searchable=(ex.number()+" "+ex.title()+" "+ex.content().stream().map(ContentBlock::searchableText).reduce("",String::concat)).toLowerCase(Locale.ROOT);
                if (!searchable.contains(term)) continue;
                CheckBox selected = new CheckBox(); selected.setSelected(submission.items.stream().anyMatch(i -> i.exercise.id()==ex.id()));
                selected.setOnAction(event -> {
                    if (selected.isSelected()) submission.items.add(new SubmissionItem(ex));
                    else {
                        SubmissionItem item = submission.items.stream().filter(i -> i.exercise.id()==ex.id()).findFirst().orElse(null);
                        if (item!=null && (!item.answer.isBlank() || !item.sources.isEmpty() || !item.resultCases.isEmpty() || !item.umlImages.isEmpty()) && !confirm("移除此题？", "当前草稿中这道题的答案和图片将被移除。")) { selected.setSelected(true); return; }
                        submission.items.removeIf(i -> i.exercise.id()==ex.id());
                    }
                    changed();
                });
                VBox text = new VBox(7, label("第 " + ex.number() + " 题   " + ex.title(), "card-title"), label(ex.programming()?"编程题 · 需代码与运行截图"+(ex.requireUml()?" · 需 UML":"") : "解答题", "muted")); HBox.setHgrow(text,Priority.ALWAYS);
                cards.getChildren().add(card(row(selected,text,button("查看题目", "quiet", () -> showExercise(ex)))));
            }
            if (cards.getChildren().isEmpty()) cards.getChildren().add(card(label("没有找到匹配的习题", "card-title"),label("尝试其他关键词或选择另一个章节。", "muted")));
        };
        search.textProperty().addListener((o,a,b)->refresh.run()); refresh.run();
        chapterBox.setOnAction(event -> {
            Chapter next=chapterBox.getValue(); if (Objects.equals(next,submission.chapter)) return;
            if (!submission.items.isEmpty() && !confirm("切换章节？", "切换会清空本次已选题目，请先保存需要保留的草稿。")) { chapterBox.setValue(submission.chapter); return; }
            submission.chapter=next; submission.items.clear(); changed(); loadExercises(() -> navigate(0));
        });
    }
    private void showExercise(Exercise exercise) {
        PreviewDocument doc=new PreviewDocument(); doc.blocks.addAll(exercise.content()); doc.assets.putAll(exercise.assets());
        WebView web=web(); web.getEngine().loadContent(new HtmlSubmissionRenderer().html(doc,true)); web.setPrefSize(780,600);
        Dialog<Void> dialog=new Dialog<>(); dialog.initOwner(stage); dialog.setTitle(exercise.toString()); dialog.getDialogPane().setContent(web); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); dialog.setResizable(true); dialog.showAndWait();
    }
    private void editorPage(SubmissionItem preferred) {
        heading("MAKE IT COMPLETE", "整理本次作业", "答案、代码与运行结果分别整理；每种运行情况支持多张截图。");
        page.getChildren().add(row(label(submission.items.size()+" 道题已加入", "badge"),spacer(),button("保存草稿", "",this::saveDraft),button("生成正文 →", "primary",this::generate)));
        if (submission.items.isEmpty()) { content(card(label("还没有添加习题", "card-title"),label("先从题库中选择本次需要提交的题目。", "muted"),button("去选择习题", "primary",()->navigate(0)))); return; }
        ListView<SubmissionItem> list=new ListView<>(); list.getItems().setAll(submission.items); list.setPrefWidth(245); list.setMinWidth(180);
        VBox edit=new VBox(16); ScrollPane right=scroll(edit);
        VBox left=new VBox(10,list,row(button("↑", "",()->moveItem(list,-1)),button("↓", "",()->moveItem(list,1)),button("移除", "quiet",()->{
            SubmissionItem i=list.getSelectionModel().getSelectedItem(); if(i!=null && confirm("移除此题？","此题的输入内容将从当前作业移除。")){submission.items.remove(i);changed();navigate(1);}
        }))); VBox.setVgrow(list,Priority.ALWAYS);
        SplitPane split=new SplitPane(left,right); split.setDividerPositions(.26); content(split);
        list.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b!=null) itemEditor(edit,b);});
        list.getSelectionModel().select(preferred==null?submission.items.getFirst():preferred);
    }
    private void moveItem(ListView<SubmissionItem> list,int delta) {
        var item=list.getSelectionModel().getSelectedItem(); int i=submission.items.indexOf(item), j=i+delta;
        if(i<0 || j<0 || j>=submission.items.size()) return;
        Collections.swap(submission.items,i,j); changed(); page.getChildren().clear();editorPage(item);
    }
    private TextArea area(String value, String prompt, Consumer<String> write, int rows) {
        TextArea a=new TextArea(value);a.setPromptText(prompt);a.setPrefRowCount(rows);a.setWrapText(true);
        a.textProperty().addListener((o,old,v)->{write.accept(v);changed();});return a;
    }
    private void itemEditor(VBox box, SubmissionItem item) {
        box.getChildren().clear();
        TextArea answer=area(item.answer,"填写解题思路或文字解答…",v->item.answer=v,4);
        box.getChildren().add(card(row(label(item.exercise.toString(),"card-title"),spacer(),button("完整题目","quiet",()->showExercise(item.exercise))),field("解答 / 思路",answer)));
        VBox sources=new VBox(12);
        Button addSource=button("+","circle-button",()->{item.sources.add(new JavaSource());changed();renderSources(sources,item);});
        addSource.setId("add-source");addSource.setAccessibleText("添加 Java 代码");addSource.setTooltip(new Tooltip("添加 Java 代码"));
        box.getChildren().add(card(row(label("Java 源代码","card-title"),spacer(),addSource),
                label("可添加多份代码，正文按添加顺序排列。名称无需填写 .java，程序自动补全；导入文件自动使用文件名（UTF-8）。","muted"),sources));
        renderSources(sources,item);
        VBox cases=new VBox(12); Runnable render=()->renderCases(cases,item);
        box.getChildren().add(card(row(label("程序运行结果","card-title"),spacer(),button("＋ 添加情况","",()->{item.resultCases.add(new RunResultCase());changed();render.run();})),label("每个保留的情况至少一张截图。说明可留空；支持 PNG / JPEG，单张 ≤ 10 MB。","muted"),cases));render.run();
        VBox uml=new VBox(8); renderImages(uml,item.umlImages,()->itemEditor(box,item));
        box.getChildren().add(card(row(label(item.exercise.requireUml()?"UML 图 · 本题必填":"UML 图 · 可选","card-title"),spacer(),button("导入 UML","",()->pickImages(item.umlImages,()->itemEditor(box,item)))),uml));
    }
    private void renderSources(VBox box,SubmissionItem item) {
        box.getChildren().clear();
        if(item.sources.isEmpty())box.getChildren().add(label("暂无代码，点击圆圈加号添加。","muted"));
        for(int i=0;i<item.sources.size();i++) {
            JavaSource source=item.sources.get(i);
            TextField name=input(source.name,v->source.name=v);name.setPromptText("请输入代码名称，例如 Main（自动补全 .java）");name.setId("source-name-"+i);
            TextArea code=area(source.code,"粘贴 Java 源代码，或导入 .java 文件",v->source.code=v,12);
            code.setWrapText(false);code.getStyleClass().add("code-editor");code.setId("source-code-"+i);
            Button remove=button("−","circle-button",()->{item.sources.remove(source);changed();renderSources(box,item);});
            remove.setId("remove-source-"+i);remove.setAccessibleText("删除第 "+(i+1)+" 份 Java 代码");remove.setTooltip(new Tooltip("删除此份代码"));
            Button importSource=button("导入 .java","",()->{
                FileChooser chooser=new FileChooser();initialHomeworkFolder(chooser);chooser.setTitle("导入 UTF-8 Java 源文件");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java 源文件","*.java"));File file=chooser.showOpenDialog(stage);
                if(file!=null && (source.code.isBlank() || confirm("替换代码？","导入内容和文件名将替换此份代码及名称。")))
                    run("正在读取源码…",()->services.submissions.readJavaSource(file.toPath()),loaded->{source.name=loaded.name;source.code=loaded.code;changed();renderSources(box,item);});
            });
            VBox entry=card(row(label("代码 "+(i+1),"card-title"),spacer(),importSource,remove),field("代码名称 · 必填",name),code);
            entry.getStyleClass().add("sub-card");box.getChildren().add(entry);
        }
    }
    private void renderCases(VBox box,SubmissionItem item) {
        box.getChildren().clear();
        if(item.resultCases.isEmpty())box.getChildren().add(label("暂无运行情况。添加一种情况并导入截图即可。","muted"));
        for(int i=0;i<item.resultCases.size();i++) {
            final int index=i;RunResultCase result=item.resultCases.get(i);VBox images=new VBox(8);
            TextArea description=area(result.description,"可选：输入数据、边界条件或异常情况",v->result.description=v,2);
            Runnable redraw=()->renderCases(box,item);renderImages(images,result.images,redraw);
            Button up=button("↑","",()->{Collections.swap(item.resultCases,index,index-1);changed();redraw.run();});up.setDisable(i==0);
            Button down=button("↓","",()->{Collections.swap(item.resultCases,index,index+1);changed();redraw.run();});down.setDisable(i==item.resultCases.size()-1);
            VBox c=card(row(label("情况 "+(i+1),"card-title"),spacer(),up,down,button("删除","quiet",()->{item.resultCases.remove(result);changed();redraw.run();})),description,images,button("＋ 导入运行截图","",()->pickImages(result.images,redraw)));c.getStyleClass().add("sub-card");box.getChildren().add(c);
        }
    }
    private void renderImages(VBox box,List<ImageResource> images,Runnable redraw) {
        box.getChildren().clear();
        for(int i=0;i<images.size();i++) {
            final int index=i;var image=images.get(i);ImageView thumbnail=new ImageView(new Image(new ByteArrayInputStream(image.data()),90,65,true,true));thumbnail.setFitWidth(90);thumbnail.setFitHeight(65);thumbnail.setPreserveRatio(true);
            Button up=button("↑","",()->{Collections.swap(images,index,index-1);changed();redraw.run();});up.setDisable(i==0);
            Button down=button("↓","",()->{Collections.swap(images,index,index+1);changed();redraw.run();});down.setDisable(i==images.size()-1);
            box.getChildren().add(row(thumbnail,label(image.name()+"\n"+image.width()+" × "+image.height(),"muted"),spacer(),up,down,button("移除","quiet",()->{images.remove(image);changed();redraw.run();})));
        }
    }
    private void pickImages(List<ImageResource> destination,Runnable redraw) {
        FileChooser chooser=new FileChooser();initialHomeworkFolder(chooser);chooser.setTitle("导入正文图片");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG / JPEG","*.png","*.jpg","*.jpeg"));List<File> files=chooser.showOpenMultipleDialog(stage);
        if(files==null)return;
        run("正在检查图片…",()->{List<ImageResource> result=new ArrayList<>();for(File f:files)result.add(services.submissions.readImage(f.toPath()));return result;},images->{destination.addAll(images);changed();redraw.run();});
    }

    private TextField input(String value,Consumer<String> write) {
        TextField field=new TextField(value);field.textProperty().addListener((o,a,b)->{write.accept(b.strip());changed();});return field;
    }
    private void initialHomeworkFolder(FileChooser chooser) {
        Path folder=preferences.existingHomeworkFolder();
        if(folder!=null)chooser.setInitialDirectory(folder.toFile());
    }
    private void profilePage() {
        heading("PERSONAL CENTER", "个人中心", "个人资料使用 AES-GCM 加密；邮箱凭据独立交由 Windows DPAPI 保护。");
        UserProfile p=submission.profile;MailAccount a=submission.account;
        TextField classNumber=input(p.className,v->p.className=v); classNumber.setId("class-number");
        classNumber.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().matches("[0-9]*") ? change : null));
        TextField studentId=input(p.studentId,v->p.studentId=v); studentId.setId("student-id");
        studentId.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().matches("[0-9]*") ? change : null));
        VBox assistants=new VBox(8); renderAssistantEmails(assistants,p,new ArrayList<>(p.assistantEmails));
        VBox identity=card(label("课程身份","card-title"),row(field("学院 *",input(p.college,v->p.college=v)),field("班级 / 班号 *",classNumber)),row(field("学号 *",studentId),field("姓名 *",input(p.name,v->p.name=v))),field("教师邮箱（提交邮件时必填）",input(p.teacherEmail,v->p.teacherEmail=v)),field("助教邮箱（提交邮件时必填，最多三个）",assistants));
        ComboBox<String> provider=new ComboBox<>();provider.setId("mail-provider");provider.getItems().setAll(MailProviderRegistry.PROVIDERS);provider.setValue(MailProviderRegistry.PROVIDERS.contains(a.provider)?a.provider:null);provider.setPromptText("请选择 QQ 或 163");provider.setMaxWidth(Double.MAX_VALUE);
        TextField mailbox=new TextField(MailProviderRegistry.localPart(a.address));mailbox.setId("mail-local-part");mailbox.setPromptText("只需填写 @ 前的部分");
        mailbox.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().matches("[^@\\s]*") ? change : null));
        Label suffix=label(provider.getValue()==null?"请选择服务商":MailProviderRegistry.suffix(provider.getValue()),"muted");suffix.setId("mail-suffix");
        mailbox.textProperty().addListener((o,old,value)->{if(provider.getValue()!=null)a.address=MailProviderRegistry.address(provider.getValue(),value);changed();});
        TextField host=input(a.host,v->a.host=v);Spinner<Integer> port=new Spinner<>(1,65535,a.port);port.setEditable(false);port.valueProperty().addListener((o,x,v)->{a.port=v;changed();});
        ComboBox<String> tls=new ComboBox<>();tls.getItems().setAll("SSL","STARTTLS");tls.setValue(a.tls);tls.setOnAction(e->{a.tls=tls.getValue();changed();});
        ComboBox<String> auth=new ComboBox<>();auth.getItems().setAll("授权码","OAuth2 Token");auth.setValue(a.auth);auth.setOnAction(e->{a.auth=auth.getValue();changed();});
        provider.setOnAction(e->{if(provider.getValue()==null)return;MailProviderRegistry.apply(provider.getValue(),a);a.address=MailProviderRegistry.address(provider.getValue(),mailbox.getText());suffix.setText(MailProviderRegistry.suffix(provider.getValue()));host.setText(a.host);port.getValueFactory().setValue(a.port);tls.setValue(a.tls);changed();});
        PasswordField secret=new PasswordField();secret.setPromptText("填写新的授权码 / Token；留空保留已保存凭据");
        VBox account=card(label("发件邮箱","card-title"),row(field("服务商",provider),field("发件邮箱（提交邮件时必填）",row(mailbox,suffix))),row(field("SMTP 主机",host),field("端口",port),field("加密连接",tls)),field("认证方式",auth),field("授权码 / Token",secret),label("选择 QQ 或 163 后自动补全邮箱后缀。OAuth2 支持已有访问 Token；浏览器授权与自动刷新留待后续接入。","muted"));
        CheckBox autoDelete=new CheckBox("导出 DOCX 或提交邮件成功后自动删除该草稿");autoDelete.setId("auto-delete-draft");autoDelete.setSelected(preferences.autoDeleteDraft);
        TextField folder=new TextField(preferences.homeworkFolder);folder.setEditable(false);folder.setId("homework-folder");folder.setPromptText("未设置（可选）");HBox.setHgrow(folder,Priority.ALWAYS);
        Button chooseFolder=button("选择文件夹","",()->{DirectoryChooser chooser=new DirectoryChooser();chooser.setTitle("选择默认作业文件夹");Path current=preferences.existingHomeworkFolder();if(current!=null)chooser.setInitialDirectory(current.toFile());File chosen=chooser.showDialog(stage);if(chosen!=null)folder.setText(chosen.getAbsolutePath());});
        ComboBox<String> exitMode=new ComboBox<>();exitMode.setId("exit-mode");exitMode.getItems().setAll("最小化至托盘","直接退出");exitMode.setValue(preferences.minimizeToTray?"最小化至托盘":"直接退出");
        Button shortcut=button("创建桌面快捷方式","",()->run("正在创建桌面快捷方式…",()->{new com.mailassistant.desktop.DesktopShortcut().create(services.paths.root());return true;},v->information("快捷方式已创建","桌面上的 MailAssistant 快捷方式已创建或更新，不会重复创建。")));
        VBox options=card(label("应用设置","card-title"),autoDelete,field("默认作业文件夹（选填）",row(folder,chooseFolder,button("清除","quiet",folder::clear))),field("默认退出方式（关闭主窗口时）",exitMode),shortcut,label("点击保存设置后生效。未保存的作业在退出前仍会提示保存。","muted"));
        Button save=button("保存设置","primary",()->{
            if (!p.className.matches("[0-9]*")) { error(new IllegalArgumentException("班级 / 班号只能填写数字")); return; }
            if (!p.studentId.matches("[0-9]*")) { error(new IllegalArgumentException("学号只能填写数字")); return; }
            if(provider.getValue()==null){error(new IllegalArgumentException("请选择 QQ 或 163 邮箱服务商"));return;}
            a.address=MailProviderRegistry.address(provider.getValue(),mailbox.getText());
            if (p.assistantEmails.size()>3 || p.assistantEmails.stream().anyMatch(email -> !com.mailassistant.validation.EmailValidator.valid(email))) { error(new IllegalArgumentException("请填写最多三个有效的助教邮箱，每个输入框只能填写一个邮箱")); return; }
            String token=secret.getText();
            if(!token.isBlank() && !com.mailassistant.validation.EmailValidator.valid(a.address)){error(new IllegalArgumentException("保存凭据前请填写有效发件邮箱"));return;}
            AppPreferences updated=new AppPreferences();updated.autoDeleteDraft=autoDelete.isSelected();updated.homeworkFolder=folder.getText();updated.minimizeToTray="最小化至托盘".equals(exitMode.getValue());
            var settings=new UserProfileRepository.Settings(JsonUtil.copy(p,UserProfile.class),JsonUtil.copy(a,MailAccount.class),updated);
            run("正在加密保存设置…",()->{services.profiles.save(settings);if(!token.isBlank())services.credentials.save(settings.account().address,token);return true;},v->{preferences=updated;secret.clear();status.setText("个人中心设置已加密保存");});
        });
        Button clear=button("清除此账号凭据与个人设置","quiet",()->{
            if(!confirm("清除本地设置？","将删除已保存的个人设置及当前邮箱凭据。现有加密草稿仍保留其中的资料快照。"))return;
            run("正在清除设置…",()->{services.credentials.delete(a.address);services.profiles.delete();return true;},v->{submission.profile=new UserProfile();submission.account=new MailAccount();preferences=new AppPreferences();changed();navigate(3);});
        });
        VBox body=new VBox(18,identity,account,options,row(save,clear),label("数据目录："+services.paths.data(),"muted"));content(scroll(body));
    }
    private void generate() {
        if(submission.preview!=null && !confirm("重新生成正文？","重新生成会替换已有预览中的手动调整。"))return;
        run("正在检查作业完整性…",()->services.submissions.generate(submission),doc->{submission.preview=doc;dirty=true;navigate(2);status.setText("正文已生成 · 请检查后导出或提交");});
    }
    private void previewPage() {
        heading("REVIEW BEFORE SENDING", "最后检查，然后提交", "点击内容块定位右侧预览，可修改文字、删除或调整顺序；点击「刷新预览」核对正文。");
        if(submission.preview==null){content(card(label("准备好后生成正文","card-title"),label("系统会检查身份信息、代码、每种运行情况的截图，以及题目要求的 UML。","muted"),button("检查并生成正文","primary",this::generate)));return;}
        PreviewDocument doc=submission.preview;
        TextField subject=new TextField(doc.subject);subject.setEditable(false);
        page.getChildren().add(card(field("邮件主题",subject),label("To  "+doc.to+"     CC  "+(doc.cc.isEmpty()?"无":String.join("；",doc.cc)),"muted")));
        WebView web=web(); web.setId("body-preview"); web.getEngine().setJavaScriptEnabled(true);
        ListView<ContentBlock> blocks=new ListView<>();blocks.setId("body-blocks");blocks.getItems().setAll(doc.blocks);
        Runnable jump=()->{
            int index=blocks.getSelectionModel().getSelectedIndex();
            if(index>=0 && web.getEngine().getLoadWorker().getState()==javafx.concurrent.Worker.State.SUCCEEDED)
                web.getEngine().executeScript("var target=document.getElementById('preview-block-"+index+"'); if(target) target.scrollIntoView(true);");
        };
        web.getEngine().getLoadWorker().stateProperty().addListener((o,a,b)->{if(b==javafx.concurrent.Worker.State.SUCCEEDED)jump.run();});
        Runnable refresh=()->web.getEngine().loadContent(new HtmlSubmissionRenderer().html(doc,true)); refresh.run();
        blocks.setOnMouseClicked(event->jump.run());
        TextArea edit=new TextArea();edit.setPromptText("选择一个内容块");edit.setPrefRowCount(5);edit.setWrapText(true);
        Label hint=label("", "muted");
        blocks.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
            loading=true;
            if(b!=null){boolean locked=!b.inlines.isEmpty() || b.type.equals("table") || b.role.equals("identity") || b.role.equals("exercise");edit.setEditable(!locked);edit.setText(b.type.equals("image")?b.caption:b.text);hint.setText(locked?"题库结构块和身份标题保留原始内容；可调整位置。":b.type.equals("image")?"编辑图片说明；使用 ↑ ↓ 调整图片位置。":"文字修改自动保留；刷新预览查看排版。");}
            else {edit.clear();edit.setEditable(false);}
            loading=false;
            jump.run();
        });
        Runnable updateList=()->{ContentBlock selected=blocks.getSelectionModel().getSelectedItem();blocks.getItems().setAll(doc.blocks);blocks.getSelectionModel().select(selected);refresh.run();previewChanged();};
        edit.textProperty().addListener((o,a,b)->{if(!loading){var block=blocks.getSelectionModel().getSelectedItem();if(block!=null && edit.isEditable()){if(block.type.equals("image"))block.caption=b;else block.text=b;previewChanged();blocks.refresh();}}});
        Button up=button("↑","",()->moveBlock(blocks,-1,updateList));Button down=button("↓","",()->moveBlock(blocks,1,updateList));
        Button remove=button("删除块","quiet",()->{var b=blocks.getSelectionModel().getSelectedItem();if(b!=null && !b.role.equals("identity") && !b.role.equals("exercise")){doc.blocks.remove(b);updateList.run();}});
        VBox left=new VBox(10,label("正文内容块","card-title"),blocks,row(up,down,remove),edit,hint,button("刷新预览","",refresh));left.setMinWidth(270);VBox.setVgrow(blocks,Priority.ALWAYS);
        SplitPane split=new SplitPane(left,web);split.setDividerPositions(.34);content(split);
        page.getChildren().add(row(button("返回编辑","",()->navigate(1)),button("重新生成","quiet",this::generate),spacer(),button("保存草稿","",this::saveDraft),button("导出 DOCX","",this::exportDocx),button("提交邮件 →","primary",this::send)));
    }
    private void moveBlock(ListView<ContentBlock> blocks,int delta,Runnable refresh) {
        var b=blocks.getSelectionModel().getSelectedItem();int i=submission.preview.blocks.indexOf(b),j=i+delta;
        if(i<=0 || j<=0 || j>=submission.preview.blocks.size())return;
        var neighbor=submission.preview.blocks.get(j);
        if(b.role.equals("exercise") || neighbor.role.equals("exercise") || b.exerciseId!=neighbor.exerciseId)return;
        Collections.swap(submission.preview.blocks,i,j);refresh.run();
    }
    private WebView web(){WebView web=new WebView();web.getEngine().setJavaScriptEnabled(false);web.setContextMenuEnabled(false);return web;}
    private void renderAssistantEmails(VBox box, UserProfile profile, List<String> values) {
        if (values.isEmpty()) values.add("");
        box.getChildren().clear();
        Runnable sync = () -> { profile.assistantEmails = new ArrayList<>(values.stream().map(String::strip).filter(v -> !v.isBlank()).toList()); changed(); };
        for (int i=0; i<values.size(); i++) {
            final int index=i;
            TextField email=input(values.get(i),v -> { values.set(index,v); sync.run(); });
            email.setPromptText("输入一个助教邮箱"); email.setId("assistant-email-"+i); HBox.setHgrow(email,Priority.ALWAYS);
            Button control=button(i==0 ? "+" : "−", "circle-button", () -> {
                if (index==0) { if(values.size()>=3)return; values.add(""); }
                else values.remove(index);
                sync.run(); renderAssistantEmails(box,profile,values);
            });
            control.setAccessibleText(i==0 ? "添加助教邮箱" : "删除助教邮箱");
            control.setTooltip(new Tooltip(i==0 ? "添加助教邮箱（最多三个）" : "删除此助教邮箱"));
            control.setDisable(i==0 && values.size()>=3);
            box.getChildren().add(row(email,control));
        }
    }
    private void saveDraft(){saveDraft(() -> status.setText("草稿已加密保存 · "+submission.draftName));}
    private void saveDraft(Runnable afterSave){
        TextInputDialog dialog=new TextInputDialog(submission.draftName);
        dialog.initOwner(stage); dialog.setTitle("保存草稿"); dialog.setHeaderText("为草稿命名，留空则按当前系统时间命名"); dialog.setContentText("草稿名称：");
        ((Button)dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("保存");
        ((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("取消");
        dialog.showAndWait().ifPresent(name -> {
            Submission snapshot=JsonUtil.copy(submission,Submission.class); snapshot.draftName=name.strip();
            run("正在加密保存草稿…",()->{services.submissions.save(snapshot);return snapshot;},saved->{submission.draftName=saved.draftName;submission.modifiedAt=saved.modifiedAt;markClean();afterSave.run();});
        });
    }
    private void openDraft(String id){
        if(dirty && !confirm("打开其他草稿？","当前未保存的更改将被替换。需要保留时请先保存草稿。"))return;
        run("正在解密草稿…",()->services.submissions.load(id),s->{submission=s;markClean();loadExercises(()->navigate(1));});
    }
    private void exportDocx(){
        try{services.submissions.validateFinal(submission);}catch(Exception e){error(e);return;}
        FileChooser chooser=new FileChooser();chooser.setTitle("导出最终正文");chooser.setInitialDirectory(services.paths.exports().toFile());chooser.setInitialFileName(submission.preview.subject.replaceAll("[\\\\/:*?\"<>|]","_")+".docx");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word 文档","*.docx"));File file=chooser.showSaveDialog(stage);
        if(file==null)return;
        Path output=file.toPath();if(!output.toString().toLowerCase(Locale.ROOT).endsWith(".docx"))output=Path.of(output+".docx"); final Path destination=output;
        run("正在导出 DOCX…",()->{services.exporter.export(submission.preview,destination);return destination;},path->outputCompleted("导出完成",path.toString()));
    }
    private void send(){
        run("正在执行发送前检查…",()->services.mail.prepare(submission),prepared->{
            if(!confirm("确认发送这封作业邮件？",prepared.summary()+"\n\n确认后才会连接 SMTP 服务器并发送。"))return;
            run("正在发送，请勿关闭程序…",()->{services.mail.sendConfirmed(prepared);return true;},v->outputCompleted("邮件已发送","服务器已接受此邮件。请到邮箱查看已发送记录。"));
        });
    }
    private void outputCompleted(String title,String message) {
        if(!preferences.autoDeleteDraft){information(title,message);return;}
        String id=submission.id;
        run("正在清理已完成的草稿…",()->{
            try { services.submissions.deleteDraft(id); return true; }
            catch(Exception e) { return false; }
        },deleted->{
            if(deleted){markClean();information(title,message+"\n对应草稿已自动删除，当前正文仍可查看。继续编辑后可重新保存草稿。");}
            else information(title,message+"\n但自动删除草稿失败，请在草稿箱手动删除。无需重复导出或发送。");
        });
    }
    private <T> void run(String message,Callable<T> action,Consumer<T> success){
        if(busy)return;busy=true;root.setDisable(true);status.setText(message);
        Task<T> task=new Task<>(){protected T call()throws Exception{return action.call();}};
        task.setOnSucceeded(e->{busy=false;root.setDisable(false);status.setText("操作完成 · 离线就绪");try{success.accept(task.getValue());}catch(Exception ex){error(ex);}});
        task.setOnFailed(e->{busy=false;root.setDisable(false);status.setText("操作未完成");error(task.getException());});executor.submit(task);
    }
    private void error(Throwable e){
        String message=e instanceof IllegalArgumentException || e instanceof IllegalStateException ? e.getMessage() : "无法完成操作。请检查文件格式、目录权限或加密数据是否属于当前 Windows 账户。";
        Alert alert=new Alert(Alert.AlertType.ERROR);alert.initOwner(stage);alert.setTitle("请检查以下内容");alert.setHeaderText("操作未完成");TextArea details=new TextArea(message);details.setEditable(false);details.setWrapText(true);details.setPrefSize(570,230);alert.getDialogPane().setContent(details);alert.showAndWait();
    }
    private boolean confirm(String title,String message){Alert alert=new Alert(Alert.AlertType.CONFIRMATION,message,ButtonType.OK,ButtonType.CANCEL);alert.initOwner(stage);alert.setTitle(title);alert.setHeaderText(title);alert.getDialogPane().setMinWidth(550);((Button)alert.getDialogPane().lookupButton(ButtonType.OK)).setText("确认");((Button)alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("取消");return alert.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;}
    private void information(String title,String message){Alert a=new Alert(Alert.AlertType.INFORMATION,message,ButtonType.OK);a.initOwner(stage);a.setTitle(title);a.setHeaderText(title);a.showAndWait();status.setText(title);}
    public boolean mayExit(){
        if(busy){information("操作进行中","请等待当前保存、导出或发送操作结束后再关闭。");return false;}
        if(!dirty)return true;
        ButtonType save=new ButtonType("保存并退出"),discard=new ButtonType("不保存退出"),cancel=new ButtonType("取消",ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,"本次作业有未保存的更改。",save,discard,cancel);a.initOwner(stage);a.setHeaderText("退出 MailAssistant？");ButtonType result=a.showAndWait().orElse(cancel);
        if(result==save){saveDraft(Platform::exit);return false;}return result==discard;
    }
    public void close(){executor.shutdown();}
}
