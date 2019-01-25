package clerk.pepusiasoft.utils;

import clerk.pepusiasoft.Debug;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.LogManager;

public class NativeAccessor {

    public static class KeyCode implements Serializable {
        public final int keyCode;
        public final int location;

        public KeyCode(int keyCode, int location) {
            this.keyCode = keyCode;
            this.location=location;
        }
    }

    public static class NativeKeyListener implements org.jnativehook.keyboard.NativeKeyListener {

        private final ArrayList<KeyPressedListener> keyPressedListeners = new ArrayList<>();
        /**
         * 同じキーコードの整数が複数ある(Ctrlのように)可能性があるため、注意が必要
         */
        private final ArrayList<KeyCode> pressedKeyCodes = new ArrayList<>();

        private boolean pressedCtrl = false;
        private boolean pressedShift = false;
        private boolean pressedAlt = false;

        public boolean isCtrlPressed() {
            return pressedCtrl;
        }

        public boolean isShiftPressed() {
            return pressedShift;
        }

        public boolean isAltPressed() {
            return pressedAlt;
        }

        void addKeyPressedListener(KeyPressedListener listener) {
            if (!keyPressedListeners.contains(listener)) {
                keyPressedListeners.add(listener);
            }
        }

        void removeKeyPressedListener(KeyPressedListener listener) {
            if (!keyPressedListeners.contains(listener)) {
                keyPressedListeners.remove(listener);
            }
        }

        @Override
        // キーが、タイピングにより入力されたときに呼ばれる. TODO で、何時呼ばれるんだろこの関数
        public void nativeKeyTyped(NativeKeyEvent event) {
            Debug.log("KeyTyped: " + event.paramString());
        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent event) {
            //Debug.log("KeyPressed: " + event.paramString());

            int keyCode = event.getKeyCode();
            int location = event.getKeyLocation();

            if (keyCode == NativeKeyEvent.VC_CONTROL && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedCtrl = true;
            if (keyCode == NativeKeyEvent.VC_SHIFT && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedShift = true;
            if (keyCode == NativeKeyEvent.VC_ALT && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedAlt = true;

            boolean keyContained = false;
            for(KeyCode model : pressedKeyCodes) {
                if(model.keyCode == keyCode && model.location == location){
                    keyContained=true;
                    break;
                }
            }
            if(!keyContained)
                pressedKeyCodes.add(new KeyCode(keyCode, location));

            for (KeyPressedListener listener : keyPressedListeners)
                listener.listen(this, event);
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent event) {
            //Debug.log("KeyReleased: " + event.paramString());

            int keyCode = event.getKeyCode();
            int location = event.getKeyLocation();

            if (keyCode == NativeKeyEvent.VC_CONTROL && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedCtrl = false;
            if (keyCode == NativeKeyEvent.VC_SHIFT && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedShift = false;
            if (keyCode == NativeKeyEvent.VC_ALT && location == NativeKeyEvent.KEY_LOCATION_LEFT)
                pressedAlt = false;

            for(int i = 0; i < pressedKeyCodes.size(); i++){
                KeyCode model = pressedKeyCodes.get(i);
                if(model.keyCode == keyCode && model.location == location){
                    pressedKeyCodes.remove(i);
                    break;
                }
            }
        }
    }

    public interface KeyPressedListener {
        void listen(NativeKeyListener state, NativeKeyEvent event);
    }

    private static boolean failedToRegisterNativeHook = false;
    private static final NativeKeyListener keyListener = new NativeKeyListener();

    public static void start() {
        LogManager.getLogManager().reset(); // 多量のログ出力の防止

        // フックされていなかったらフックする
        if (!GlobalScreen.isNativeHookRegistered()) {
            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException e) { // 失敗した場合、アプリの終了まではしないが、JNativeHookの機能が使えなくなる
                e.printStackTrace();
                failedToRegisterNativeHook = true;
                Debug.log("Failed to register native hook.");
            }
        }

        if (!failedToRegisterNativeHook) {
            GlobalScreen.addNativeKeyListener(keyListener);
        }
    }

    public static void addKeyPressedListener(KeyPressedListener listener) {
        keyListener.addKeyPressedListener(listener);
    }

    public static void removeKeyPressedListener(KeyPressedListener listener) {
        keyListener.removeKeyPressedListener(listener);
    }

    public static ArrayList<KeyCode> getPressedKeyCodes() {
        return keyListener.pressedKeyCodes;
    }

    public static boolean isKeyDown(int keyCode, int location) {
        boolean contained = false;
        for(KeyCode model : keyListener.pressedKeyCodes){
            if(model.keyCode == keyCode && model.location == location) {
                contained = true;
                break;
            }
        }

        return contained;
    }

    public static boolean areKeysDown(ArrayList<KeyCode> models) {
        int matchedCount = 0;
        for(KeyCode pressed : keyListener.pressedKeyCodes){
            for(KeyCode model : models) {
                if(pressed.keyCode == model.keyCode && pressed.location == model.location)
                    matchedCount++;
            }
        }

        return models.size() <= matchedCount;
    }
}
