package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.stages.Preferences;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;

public class Configurator extends SplitPane {

    private final Preferences preferences;
    private final TreeView<String> groups;
    private final VBox contents; // ここで、TreeItemがクリックされるごとに要素(ConfigItem.control達)が入れ替わる感じ

    private final HashMap<ConfiguratorTreeItem, ArrayList<ConfigItem>> treeItemAndConfigs = new HashMap<>();

    public Configurator(Preferences preferences) {

        this.preferences = preferences;

        setOrientation(Orientation.HORIZONTAL);
        groups = new TreeView<>(new ConfiguratorTreeItem("root")); // rootのTreeItemは、必要だけど、表示はしたくないので消す
        contents = new VBox();
        ScrollPane scrollPane = new ScrollPane(contents);
        groups.setShowRoot(false);
        contents.setAlignment(Pos.TOP_LEFT);
        contents.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        contents.setPadding(new Insets(5, 10, 5, 10));
        contents.setSpacing(5);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        AnchorPane.setTopAnchor(groups, 0d);
        AnchorPane.setLeftAnchor(groups, 0d);
        AnchorPane.setRightAnchor(groups, 0d);
        AnchorPane.setBottomAnchor(groups, 0d);
        AnchorPane.setTopAnchor(contents, 0d);
        AnchorPane.setLeftAnchor(contents, 0d);
        AnchorPane.setRightAnchor(contents, 0d);
        AnchorPane.setBottomAnchor(contents, 0d);

        groups.setCellFactory(param -> new TreeCell<String>() {
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);

                if (selected) {
                    ConfiguratorTreeItem treeItem = (ConfiguratorTreeItem) getTreeItem();
                    ArrayList<ConfigItem> configs = treeItemAndConfigs.get(treeItem);
                    if (configs != null) {
                        showConfigs(configs);
                    } else {
                        showConfigs(new ArrayList<>());
                    }
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                }
            }
        });

        getItems().addAll(groups, scrollPane);
        setDividerPositions(SaveData.get("configurator.divider pos", 0.15d));

        SaveData.addSavingTask(() -> SaveData.set("configurator.divider pos", getDividerPositions()[0]));
    }

    public void addConfig(ConfigItem item) {
        ConfiguratorTreeItem parent = (ConfiguratorTreeItem) groups.getRoot();
        int depth = 0;
        while (depth <= item.group.length - 1) {
            String childName = item.group[depth];
            ConfiguratorTreeItem child = null;
            for (Object object : parent.getChildren()) {
                ConfiguratorTreeItem treeItem = (ConfiguratorTreeItem) object;
                if (childName.equals(treeItem.getName())) {
                    child = treeItem;
                    break;
                }
            }
            if (child == null) {
                child = new ConfiguratorTreeItem(item.groupId, depth);
                parent.getChildren().add(child);
            }
            parent = child;
            depth++;
        }

        if (!treeItemAndConfigs.containsKey(parent))
            treeItemAndConfigs.put(parent, new ArrayList<>(Collections.singletonList(item)));
        else
            treeItemAndConfigs.get(parent).add(item);
    }

    private void showConfigs(ArrayList<ConfigItem> items) {
        contents.getChildren().clear();

        if (items.size() >= 1) {
            for (ConfigItem item : items) {
                item.gatherInfo();
                contents.getChildren().add(item.container);
            }
        }
    }

    public void showCorrespondedConfigs(final String keyword) {
        String[] words = keyword.replace('　', ' ').split(" "); // 全角スペースは半角に変換してから、分割
        HashMap<Integer, ArrayList<ConfigItem>> map = new HashMap<>(); // 該当度を数値化して、ソートに使う
        for (Map.Entry<ConfiguratorTreeItem, ArrayList<ConfigItem>> set : treeItemAndConfigs.entrySet()) {
            ConfiguratorTreeItem treeItem = set.getKey();
            int treeItemPoint = matchLevel(words, treeItem.getGroupId()) + matchLevel(words, treeItem.getValue());
            for (ConfigItem configItem : set.getValue()) {
                int point = matchLevel(words, configItem.nameId) + matchLevel(words, configItem.nameText.getText()) + treeItemPoint;
                if (point >= 1) {
                    if (!map.containsKey(point))
                        map.put(point, new ArrayList<>(Collections.singletonList(configItem)));
                    else
                        map.get(point).add(configItem);
                }
            }
        }

        contents.getChildren().clear();

        if (map.size() >= 1) {
            // ソートする
            Set<Integer> set = map.keySet();
            Object[] keys = set.toArray();
            Arrays.sort(keys);
            for (int i = set.size() - 1; i >= 0; i--) {
                for (ConfigItem configItem : map.get(keys[i])) {
                    configItem.gatherInfo();
                    ConfiguratorTreeItem treeItem = null;
                    for (Map.Entry<ConfiguratorTreeItem, ArrayList<ConfigItem>> entry : treeItemAndConfigs.entrySet()) {
                        if (entry.getValue().contains(configItem)) {
                            treeItem = entry.getKey();
                            break;
                        }
                    }
                    Text groupText = new Text(Lang.of("configurator." + treeItem.getGroupId()));
                    groupText.setFont(new Font(groupText.getFont().getName(), groupText.getFont().getSize() - 2));
                    groupText.setStyle("-fx-foreground-color: #575663");
                    StackPane stackPane = new StackPane(groupText);
                    stackPane.setAlignment(Pos.CENTER_LEFT);
                    stackPane.setPadding(new Insets(0, 0, 0, 40d));
                    stackPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
                    VBox result = new VBox(2, stackPane, configItem.container);
                    contents.getChildren().add(result);
                }
            }
        } else {
            contents.getChildren().add(new Text(Lang.of("preferences.notFound")));
        }
    }

    public void applyChanges() {
        for (Collection<ConfigItem> collection : treeItemAndConfigs.values()) {
            for (ConfigItem item : collection) {
                if (item.isChanged()) {
                    try {
                        item.applyInfo();
                    } catch (Exception e) {
                        Debug.log("Failed to apply a change, " + item.groupId + ":" + item.nameId + ":" + item.nameText);
                        e.printStackTrace();
                    }
                }
            }
        }

        preferences.changeApplyingButtonStates(false);
    }

    private int matchLevel(String[] strs, String s2) {
        int level = 0;
        for (String str : strs) {
            if (s2.contains(str))
                level += str.length() + 5; // 下の検索機能より評価が劣って仕舞わないように、計算式を工夫
            else { // IntelliJ IDEAの補完機能みたいな機能を実装してみた
                boolean failed = false;
                int lastInd = -1;
                String copy = s2 + "";
                char[] chars = str.toCharArray();
                for (char aChar : chars) {
                    int index = copy.indexOf(aChar);
                    if (index > lastInd) {
                        lastInd = index;
                        copy = copy.substring(index);
                    } else {
                        failed = true;
                        break;
                    }
                }
                if (!failed)
                    level += 1;
            }
        }

        return level;
    }
}