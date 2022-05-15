import processing.core.PApplet;
import processing.core.PVector;

public interface Displayable {
    
    public void display(PVector cameraPosition, PVector screen, PApplet p);

}
