package server.card.effect;

/**
 * Each card will have its own unique and crazy effects. Rather than make
 * separate individual classes for those effects, we instead write them as
 * anonymous classes in their respective cards. These effects still need to be
 * somehow referenced (mainly for deserializing EventAddEffect). This class
 * provides a means for referencing these anonymous classes, by indexing static
 * instances of them in a list.
 * 
 * @author Michael
 *
 */
public class AnonymousEffectList {
	AnonymousEffect list[];

	public AnonymousEffectList(int id, AnonymousEffect... list) {
		this.list = list;
		for (int i = 0; i < list.length; i++) {
			this.list[i].anonymousIndex = i;
			this.list[i].ownerID = id;
		}
	}

	public AnonymousEffect get(int index) {
		return this.list[index];
	}
}
