package server.card;

public enum ClassCraft {
    NEUTRAL, FORESTROGUE, SWORDPALADIN, RUNEMAGE, DRAGONDRUID, SHADOWDEATHKNIGHT, BLOODWARLOCK, HAVENPRIEST, PORTALSHAMAN;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
