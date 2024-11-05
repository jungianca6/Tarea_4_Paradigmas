package Game;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ball{
    @JsonProperty("id")
    int id;
    @JsonProperty("posx")
    int posx;
    @JsonProperty("posy")
    int posy;
    @JsonProperty("active")
    boolean active;

    public Ball(int id, int posx, int posy, boolean active) {
        this.posx = posx;
        this.posy = posy;
        this.active = active;
    }
    // Constructor predeterminado
    public Ball() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
