package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.App;
import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.language.Lang;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class ConfigItem<T extends Control> {

    final String groupId;
    final String[] group;
    final String nameId;
    final Text nameText;
    final T control;
    final HBox container; // nameTextとcontrolを纏めた物
    private final Consumer<T> infoGatherer;
    private final Consumer<T> infoApplier;
    // TODO Preferences終了時は、この値がそのままtrueになっている、ということもあるだろうが、またPreferencesを起動したときにgatherInfo()でfalseになるのだから、問題ないだろう
    private boolean changed; // 設定の変更が行われた場合(まあ、controlにフォーカスが一度でも移った、という判定でもいいと思う)

    // groupId: example.a.b, strings split will be translated by Lang
    // nameId: normal(will be contained in Text), this will be translated by Lang
    // control: selector
    // infoGather: set info control
    // apply info of control
    public ConfigItem(String groupId, String nameId, T control, Consumer<T> infoGather, Consumer<T> infoApplier) {
        this.groupId = groupId;
        this.group = groupId.split("\\.");
        this.nameId = nameId;
        this.nameText = new Text(Lang.of("configurator.item." + nameId));
        this.control = control;
        this.container = new HBox(5, nameText, control);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        this.infoGatherer = infoGather;
        this.infoApplier = infoApplier;

        infoGather.accept(control); // 他の場所から、設定内容を参照する場合、これで初期化しておくとよい

        control.setOnMousePressed(event -> { // TODO プロパティ変更時のイベントでは動いてくれなかった。仕方なくこっちにした
            changedInfo();
        });
    }

    void gatherInfo() {
        infoGatherer.accept(control);
        changed = false;
    }

    void applyInfo() {
        infoApplier.accept(control);
        changed = false;
    }

    void changedInfo() {
        changed = true;

        App.preferences.changeApplyingButtonStates(true);
    }

    boolean isChanged() {
        return changed;
    }
}