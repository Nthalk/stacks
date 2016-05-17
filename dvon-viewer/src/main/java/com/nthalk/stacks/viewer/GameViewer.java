package com.nthalk.stacks.viewer;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.nthalk.fn.Option;
import com.nthalk.stacks.Board;
import com.nthalk.stacks.Game;
import com.nthalk.stacks.Player;
import com.nthalk.stacks.players.RandomPlayer;
import org.apache.log4j.Logger;

import java.util.IdentityHashMap;
import java.util.Map;

public class GameViewer extends SimpleApplication {

    private static Logger LOG = Logger.getLogger(GameViewer.class);

    private static Game game = new Game();
    private final Map<Board.Position, PositionNode> positions = new IdentityHashMap<>();
    private Material redMaterial;
    private Material greyMaterial;
    private Material blackMaterial;
    private Material whiteMaterial;


    public static Game getGame() {
        return game;
    }


    public static void main(String[] args) {
        GameViewer gameViewer = new GameViewer();
        gameViewer.setShowSettings(false);
        gameViewer.setDisplayStatView(false);
        AppSettings appSettings = new AppSettings(true);
        appSettings.setHeight(800);
        appSettings.setWidth(1380);
        gameViewer.setSettings(appSettings);
        gameViewer.start();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (game.findValidPlay()) {
            Player currentPlayer = game.getCurrentPlayer();
            if (game.getPhase() == Game.Phase.PLACEMENT) {
                Game.ValidPosition place = currentPlayer.place(game.getCurrentColor(), game);
                game.submit(place);
            } else {
                Game.ValidMove move = currentPlayer.move(game.getCurrentColor(), game);
                game.submit(move);
            }
        }
        for (Board.Position position : game.getBoard().getPositions()) {
            updatePosition(position);
        }
    }

    private void updatePosition(Board.Position position) {
        PositionNode to = positions.get(position);
        Option<Board.Stack> stackOption = game.getStack(position);
        if (stackOption.isEmpty()) {
            to.setPositionMaterial(greyMaterial);
            to.setHasRed(false);
            to.setStackSize(0);
        } else {
            for (Board.Stack stack : stackOption) {
                Game.Color owner = stack.getOwner();
                if (owner == Game.Color.WHITE) {
                    to.setPositionMaterial(whiteMaterial);
                } else if (owner == Game.Color.BLACK) {
                    to.setPositionMaterial(blackMaterial);
                } else if (owner == Game.Color.RED) {
                    to.setPositionMaterial(redMaterial);
                }
                to.setHasRed(stack.getHasRed());
                to.setStackSize(stack.getSize());
            }
        }

    }

    @Override
    public void simpleInitApp() {
        configureViewPort();
        configureEvents();
        setupMaterials();
        buildTheBoard();
    }

    private void configureEvents() {
        inputManager.addMapping("MouseButton", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String s, boolean b, float v) {
                if (b) {
                    Vector2f eventCursorPosition = getInputManager().getCursorPosition();
                    Vector3f click3d = cam.getWorldCoordinates(new Vector2f(eventCursorPosition.x, eventCursorPosition.y), 0f).clone();
                    Vector3f dir = cam.getWorldCoordinates(new Vector2f(eventCursorPosition.x, eventCursorPosition.y), 1f).subtractLocal(click3d).normalizeLocal();
                    Ray ray = new Ray(click3d, dir);
                    CollisionResults results = new CollisionResults();
                    rootNode.collideWith(ray, results);
                    for (CollisionResult result : results) {
                        Geometry geometry = result.getGeometry();
                        PositionNode.PositionWrapped positionWrapped = geometry.getUserData("position");
                        if (positionWrapped != null) {
                            LOG.info(geometry);
                            PositionNode positionNode = positions.get(positionWrapped.getPosition());
                            positionNode.setGlow(true);
                            break;
                        }

                    }
                }
            }
        }, "MouseButton");
    }

    private void buildTheBoard() {
        Board board = game.getBoard();

        game.setPlayer(Game.Color.WHITE, new RandomPlayer());
        game.setPlayer(Game.Color.BLACK, new RandomPlayer());

        Node gameBoardNode = new Node();
        gameBoardNode.setLocalTranslation(-12.75f, -5.5f, -10);
        rootNode.attachChild(gameBoardNode);

        Geometry gameBoard = new Geometry("Board", new Quad(25.5f, 11));
        Material gameBoardMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gameBoardMaterial.setColor("Color", ColorRGBA.Brown);
        gameBoard.setMaterial(gameBoardMaterial);
        gameBoardNode.attachChild(gameBoard);

        BitmapFont font = loadGuiFont();
        for (Board.Row row : board.getRows()) {
            for (Board.Position position : row.getPositions()) {
                PositionNode positionNode = new PositionNode(position, redMaterial, greyMaterial, font);
                positions.put(position, positionNode);
                gameBoardNode.attachChild(positionNode.getNode());
            }
        }
    }

    private void setupMaterials() {
        redMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redMaterial.setColor("Color", ColorRGBA.Red);

        greyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        greyMaterial.setColor("Color", ColorRGBA.Gray);

        blackMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blackMaterial.setColor("Color", ColorRGBA.Black);

        whiteMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        whiteMaterial.setColor("Color", ColorRGBA.White.mult(.9f));
    }

    private void configureViewPort() {
        rootNode.addLight(new AmbientLight(ColorRGBA.White));
        getFlyByCamera().setEnabled(false);
//        FilterPostProcessor filterPostProcessor = new FilterPostProcessor(assetManager);
//        SSAOFilter ssaoFilter = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
//        filterPostProcessor.addFilter(ssaoFilter);
//        viewPort.addProcessor(filterPostProcessor);

    }
}
