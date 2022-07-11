package network;

import java.util.EnumMap;

public class EmoteSet {
    private EnumMap<Emote, String> lines;

    private EmoteSet() {
        this.lines = new EnumMap<Emote, String>(Emote.class);
    }

    public String getLine(Emote emote) {
        return this.lines.get(emote);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        EmoteSet current;

        private Builder() {
            this.current = new EmoteSet();
        }

        public Builder setLine(Emote emote, String line) {
            current.lines.put(emote, line);
            return this;
        }

        public EmoteSet build() {
            return current;
        }
    }
}
