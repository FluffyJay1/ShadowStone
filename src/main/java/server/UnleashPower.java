package server;

import java.util.*;
import java.util.stream.Collectors;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class UnleashPower extends Card {
    public int unleashesThisTurn = 0;

    public UnleashPower(Board b, UnleashPowerText unleashPowerText) {
        super(b, unleashPowerText);
    }

    @Override
    public double getValue(int refs) {
        return 0;
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.unleashesThisTurn).append(" ");
    }
}
