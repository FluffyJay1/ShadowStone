package server.card;

public enum CardTrait {
    OFFICER("Officer"), COMMANDER("Commander"),
    ARTIFACT("Artifact");

    private final String descriptiveName;

    private CardTrait(String descriptiveName) {
        this.descriptiveName = descriptiveName;
    }

    public String toString() {
        return this.descriptiveName;
    }
}
