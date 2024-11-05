package Game;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ball{
    @JsonProperty("posx")
    int posx;
    @JsonProperty("posy")
    int posy;
    @JsonProperty("active")
    boolean active;

    public Ball(int posx, int posy, boolean active) {
        this.posx = posx;
        this.posy = posy;
        this.active = active;
    }
}
