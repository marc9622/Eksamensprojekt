import java.util.HashMap;
import java.util.stream.IntStream;

import processing.core.*;

public class ButtonManager {
  
    public HashMap<Integer, Boolean> buttonMap;
    
    public static enum Binding {
        JUMP, FALL, MOVE_LEFT, MOVE_RIGHT,
        CAM_UP, CAM_DOWN, SHOOT, INTERACT, 
    }

    public ButtonManager() {
        buttonMap = new HashMap<Integer, Boolean>();
    }

    private int[] getKeyCodesFromBinding(Binding binding) {
        return switch (binding) {
            case JUMP       -> new int[] {'W', ' '};
            case FALL       -> new int[] {'S'};
            case CAM_UP     -> new int[] {PApplet.UP};
            case CAM_DOWN   -> new int[] {PApplet.DOWN};
            case MOVE_LEFT  -> new int[] {'A'};
            case MOVE_RIGHT -> new int[] {'D'};
            case SHOOT      -> new int[] {PApplet.SHIFT, mouse(PApplet.LEFT)};
            case INTERACT   -> new int[] {'E'};
        };
    }
    
    public boolean isBindingPressed(Binding binding) {
        return isButtonPressed(getKeyCodesFromBinding(binding));
    }
    
    public boolean isMousePressed(int... buttons) {
        return IntStream.of(buttons).anyMatch(b -> isSingleButtonPressed(mouse(b)));
    }
    
    //Check a list of buttons and if atleast one is pressed, return true;
    public boolean isButtonPressed(int... buttons) {
        return IntStream.of(buttons).anyMatch(this::isSingleButtonPressed);
    }
    
    //Checks if a specific button is pressed.
    private boolean isSingleButtonPressed(int button) {
        buttonMap.putIfAbsent(button, false);
        return buttonMap.get(button);
    }
    
    public void mousePress(int... codes) {
        IntStream.of(codes).map(this::mouse).forEach(this::buttonPress);
    }
    
    public void mouseRelease(int... codes) {
        IntStream.of(codes).map(this::mouse).forEach(this::buttonRelease);
    }
    
    public void buttonPress(int... codes) {
        IntStream.of(codes).forEach(this::buttonPress);
    }
    
    public void buttonPress(int code) {
        updateButton(code, true);
    }
    
    public void buttonRelease(int... codes) {
        IntStream.of(codes).forEach(this::buttonRelease);
    }
    
    public void buttonRelease(int code) {
        updateButton(code, false);
    }
    
    public void bindPress(Binding binding) {
        buttonPress(getKeyCodesFromBinding(binding));
    }

    public void bindRelease(Binding binding) {
        buttonRelease(getKeyCodesFromBinding(binding));
    }

    //If the code is already contained in the HashMap then update its value. (This is to prevent people from adding unused keys to the map by pressing keys not used by the game.)
    private void updateButton(int code, boolean value) {
        if(buttonMap.containsKey(code))
            buttonMap.put(code, value);
    }
    
    private int mouse(int code) {
        return code - 100;
    }
}