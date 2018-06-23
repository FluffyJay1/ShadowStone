package server.card.effect;

public class EffectIDLinker {
	public static Class<? extends Effect> getClass(int id) {
		switch (id) {
		case EffectStatChange.ID:
			return EffectStatChange.class;
		default:
			return null;
		}
	}

}
