package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.language.Lang;
import javafx.scene.control.TreeItem;

public class ConfiguratorTreeItem extends TreeItem<String> {

    private String name; // one of group(ConfigItem.group)
    private String groupId;

    public ConfiguratorTreeItem(String name) {
        super(null);
        this.name = name;
        this.groupId = "";
        setValue(this.name);
    }

    public ConfiguratorTreeItem(String originalGroupId, int depth) {
        super(null);
        setNameFrom(originalGroupId, depth);
        setValue(Lang.of("configurator." + this.groupId));
    }

    private void setNameFrom(String originalGroupId, int depth) {

        String[] group = originalGroupId.split("\\.");
        StringBuilder groupId = new StringBuilder();
        for(int i = 0; i <= depth; i++) {
            groupId.append('.');
            groupId.append(group[i]);
        }
        this.groupId = groupId.substring(1); // 初めの.は削除する
        String[] strs = this.groupId.split("\\.");
        this.name = strs[strs.length-1];
    }

    public String getName(){
        return name;
    }

    public String getGroupId(){
        return groupId;
    }
}
