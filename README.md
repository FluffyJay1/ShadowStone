# ShadowStone

A Java card game inspired by the likes of Shadowverse and Hearthstone. Made
with the help of the Slick2D game library, however many things were implemented
from scratch.

[Link to video demo](https://youtu.be/yZAuBUbvx1k)

![Screenshot of game](https://user-images.githubusercontent.com/5732925/178848614-daff478d-9b69-40cc-898d-821cc318dfed.png)

## Requirements

- Java 15+

- Tolerance for fullscreen 1080p

## Running the game

Go to the [releases](https://github.com/FluffyJay1/ShadowStone/releases) page
and grab a zip. Extract contents to a folder, then run the jar in that folder.
Stuff like your decks will be saved to that folder too.

If you want to play an unreleased version, clone the repo and execute the gradle
"run" task, e.g. `./gradlew run`. Should integrate well with intellij and
vscode too.

## A note on multiplayer

Port 9091 is used for multiplayer. For now, this can only be changed by
changing the source code in `src/main/java/client/Game.java`.

Hosts will have to set up port forwarding.

Cheating isn't hard, so stick to playing with friends.

## Super secret ingame keyboard shortcuts

- `z`: Save the current board state (persists on disk)
- `x`: Load the saved board state
- Space: Skip all animations

## Mechanics

### Similarities to Hearthstone and Shadowverse

- Minions have attack and health
- Minions can't attack on the turn they're played ("summoning sickness") unless
  they have __Storm__ (Charge) or __Rush__
- When a minion attacks, it deals damage equal to its attack value (number on
  the left) to its target, and the target retaliates by dealing damage equal to
  its attack value as well
- When a minion's health goes to 0 or less, it is destroyed
- Playing cards cost mana, the amount you can use per turn starts at 1 and
  increases each turn
- The player going second starts with an extra card

### Similarities to Hearthstone

- Max number of cards in hand is 10
- Each class has a unique power it can use once per turn (unleash power)
- When playing a card to the board, it can be played in any position to the
  left/right of existing cards (positioning matters!)
- The player going second starts with a coin (kinda)

### Similarities to Shadowverse

- When you try to draw a card when your deck is empty, you lose
- Minions have unique (__Unleash__) abilities, you can trigger one of these once per
  turn, but only on turn 5 and after if going first, or turn 4 and after if going
  second
- You can play amulets, which take up a spot on the board but don't have health
  or attack
- Minions with 0 attack can still attack, but leaders cannot attack
- Turn start/end effects resolve in counter-clockwise order

### Differences

- Max number of cards on board is 6
- The __Unleash__ mechanic
  - Minions will usually have special __Unleash__ abilities that activate when
    you __Unleash__ them
  - Unleashing is done via your Unleash Power, which usually costs 2 mana to
    use and can only be used once per turn, but has no limit on number of uses
    per game
  - Each class has a unique Unleash Power, which does additional things on top
    of Unleashing the minion
  - Unlike the Evolve mechanic of Shadowverse, an Unleashed minion doesn't
    automatically gain __Rush__ nor does it enter an "Unleashed" form; a minion
    can be Unleashed as many times as you want
- Minions also have a 3rd stat: magic (the middle number) that usually
  influences the strength of their __Unleash__ ability, and may be used by
  other card effects
- Leaders start the game with 25 health
