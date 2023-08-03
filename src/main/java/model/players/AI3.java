package model.players;

import aimodel.AI;
import aimodel.ProbabilityAI;
import model.tiles.AI3TableTiles;
import model.basic.Tile;
import model.tiles.Tiles;
import utils.TileUtils;

import java.util.List;

public class AI3 extends Player {
    private final AI3TableTiles aiTable;
    private final AI ai;

    public AI3(String name, List<Tile> hand) {
        super(name, hand);
        this.aiTable = new AI3TableTiles();
        this.ai = new ProbabilityAI();
    }

    @Override
    public void plays(Tile tile) {
        super.hand.remove(tile);
        TileUtils.resetEntity(tile);
        this.aiTable.add(tile);
    }

    @Override
    public void playAction() {
        ai.setHand(this.getHand());
        this.plays(ai.getTileToPlay());
    }

    @Override
    public PlayerActionEnum otherAction(Tile tile) {
        ai.setHand(this.getHand());
        if (this.containsPung() && ai.shouldPung(tile)) {
            return PlayerActionEnum.PUNG;
        } else if (this.containsChow() && ai.shouldChow(tile)) {
            return PlayerActionEnum.CHOW;
        } else if (this.containsKong() && ai.shouldKong(tile)) {
            return PlayerActionEnum.KONG;
        } else {
            return PlayerActionEnum.SKIP;
        }
    }


    @Override
    public Tiles getTable() {
        return this.aiTable;
    }
}
