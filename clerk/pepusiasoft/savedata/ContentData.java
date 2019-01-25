package clerk.pepusiasoft.savedata;

import java.io.Serializable;
import java.util.HashMap;

public class ContentData implements Serializable {

    private static final long serialVersionUID = -2398598719834L;

    private HashMap<String, Object> map = new HashMap<>();

    public void set(String key, Object value) {
        map.put(key, value);
    }

    public String getString(String key) {
        return map.get(key).toString();
    }
}
