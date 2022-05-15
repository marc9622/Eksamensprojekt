import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class Shooter implements Displayable {
    
    protected float damage;
    protected float speed;

    public Shooter(float damage, float speed) {
        this.damage = damage;
        this.speed = speed;
    }

    private List<Bullet> bullets = new ArrayList<>();

    public void shoot(PVector position, PVector target) {
        PVector direction = PVector.sub(target, position);
        bullets.add(new Bullet(position.copy(), direction.copy().normalize().mult(speed)));
    }

    public void update(float deltaTime, PVector screen) {
        bullets.forEach(b -> b.update(deltaTime));
        bullets.removeIf(b -> b.isDead(screen));
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        bullets.forEach(b -> b.display(cameraPosition, screen, p));
        p.text(bullets.size(), 10, 10);
    }

    protected class Bullet {

        private PVector position;
        private PVector direction;
        private PVector size = new PVector(5, 2);

        public Bullet(PVector position, PVector direction) {
            this.position = position;
            this.direction = direction;
        }

        public void update(float deltaTime) {
            position.add(PVector.mult(direction, deltaTime));
        }

        public boolean isDead(PVector screen) {
            return !Camera.isInside(position, screen);
        }

        public void display(PVector cameraPosition, PVector screen, PApplet p) {
            p.fill(255, 0, 0);
            p.ellipse(position.x - cameraPosition.x, position.y - cameraPosition.y, size.x, size.y);
        }

    }
}