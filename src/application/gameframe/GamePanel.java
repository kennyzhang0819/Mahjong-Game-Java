package application.gameframe;

import application.core.Game;
import model.GameState;
import config.Config;
import model.players.Player;
import model.basic.Tile;
import model.players.PlayerStatusEnum;

import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import static utils.TileUtils.*;

public class GamePanel extends JPanel implements Runnable {

    Thread gameThread;
    KeyHandler keyHandler = new KeyHandler();
    private final Game game;
    private GameState gameState;
    private final Player player;
    private Tile hoveredTile = null;
    private final TileImageLoader imageLoader = new TileImageLoader();


    public GamePanel() {
        this.game = new Game();
        this.gameState = this.game.getGameState();
        this.player = this.game.getPlayers().get(0);

        this.setPreferredSize(new Dimension(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
        this.setBounds(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (hoveredTile != null && player.getStatus().contains(PlayerStatusEnum.PLAYING) &&
                        !player.getChouPungKong()) {
                    player.plays(hoveredTile);
                    hoveredTile = null;
                    game.processPlayerPlayed();
                    gameState = game.getGameState();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                List<Tile> hand = new ArrayList<Tile>() {{
                    this.addAll(gameState.getPlayerHand());
                }};
                Tile newHoveredTile = getTileAt(e.getX(), e.getY(), hand);
                if (newHoveredTile != hoveredTile) {
                    if (hoveredTile != null) {
                        moveTileDown(hoveredTile);
                    }
                    if (newHoveredTile != null) {
                        moveTileUp(newHoveredTile);
                    }
                    hoveredTile = newHoveredTile;
                }
            }
        });
    }

    public void start() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void update() {
        if (this.game.isOver()) {
            this.gameThread = null;
        }
        if (keyHandler.hPressed && player.getStatus().contains(PlayerStatusEnum.HU)) {
            System.out.println(player.getName() + " wins!");
        }
        if (keyHandler.pPressed && !keyHandler.pProcessed
                && player.getStatus().contains(PlayerStatusEnum.PUNG)) {
            System.out.println("P pressed, going to process Pung now");
            this.gameState = this.game.processPung(player);
            keyHandler.pProcessed = true;
        }
        if (keyHandler.sPressed
                && player.getChouPungKong()) {
            this.game.processPlayerSkipped();
            this.gameState = this.game.getGameState();
        }

        List<Player> allPlayers = this.gameState.getAllPlayers();
//                System.out.println(allPlayers.get(0).getName() + " : " + allPlayers.get(0).getHand().toList().size()  +
//                " | " + allPlayers.get(1).getName() + " : " + allPlayers.get(1).getHand().toList().size()  +
//                " | " + allPlayers.get(2).getName() + " : " + allPlayers.get(2).getHand().toList().size()  +
//                " | " + allPlayers.get(3).getName() + " : " + allPlayers.get(3).getHand().toList().size() );
//        System.out.println(allPlayers.get(0).getName() + " : " + allPlayers.get(0).getStatus() +
//                " | " + allPlayers.get(1).getName() + " : " + allPlayers.get(1).getStatus() +
//                " | " + allPlayers.get(2).getName() + " : " + allPlayers.get(2).getStatus() +
//                " | " + allPlayers.get(3).getName() + " : " + allPlayers.get(3).getStatus());
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Drawer drawer = new Drawer(g2, getWidth(), getHeight(), imageLoader);
        drawer.drawBackground();
        drawer.drawLogs(this.game.getLog().getLastXMessages(39));
        drawer.drawInstructions();
        drawer.drawHelperBoxes();
        for (Tile tile : gameState.getTilesToDraw()) {
            drawer.drawTile(tile);
        }
        g2.dispose();
    }

    @Override
    public void run() {
        double interval = 1000000000.0 / Config.FPS;
        double nextTime = System.nanoTime() + interval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double wait = (nextTime - System.nanoTime()) / 1000000;
                if (wait < 0) {
                    wait = 0;
                }
                Thread.sleep((long) wait);
                nextTime += interval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}