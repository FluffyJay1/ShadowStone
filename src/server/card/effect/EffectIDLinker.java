package server.card.effect;

public class EffectIDLinker {
	public static Class<? extends Effect> getClass(int id) {
		// next is 2
		switch (id) {
		case EffectStatChange.ID:
			return EffectStatChange.class;
		case EffectBrambles.ID:
			return EffectBrambles.class;
		default:
			return null;
		}
	}

}
