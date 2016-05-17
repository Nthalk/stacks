package com.nthalk.stacks.viewer;

import com.jme3.export.*;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.nthalk.stacks.Board;

import java.io.IOException;

public class PositionNode {
    private static Cylinder INDICATOR_SHAPE;
    private static Cylinder POSITION_SHAPE;
    private final Node node;
    private final Geometry redIndicator;
    private final Geometry positionShape;
    private final BitmapText text;
    private boolean hasRed = false;
    private int stackSize = 0;

    public PositionNode(Board.Position position, Material redMaterial, Material greyMaterial, BitmapFont font) {
        node = new Node();
        Board.Row row = position.getRow();
        float offset = row.getNumber();
        node.setLocalTranslation((position.getColumn() * 2.3f) + offset, 9.5f - (row.getNumber() * 2f), .1f);

        redIndicator = new Geometry("Red Inidcator", getIndicatorShape());
        redIndicator.setMaterial(redMaterial);
        redIndicator.setLocalTranslation(.2f, .2f, 0f);

        positionShape = new Geometry(position.toString(), getPositionShape());
        positionShape.setUserData("position", new PositionWrapped(position));
        positionShape.setMaterial(greyMaterial);
        node.attachChild(positionShape);

        text = new BitmapText(font);
        text.setSize(.5f);
        text.setColor(ColorRGBA.Black);
        text.setLocalTranslation(-.4f, -.6f, .01f);
        node.attachChild(text);
    }

    private static Mesh getPositionShape() {
        if (POSITION_SHAPE == null) {
            POSITION_SHAPE = new Cylinder(10, 16, .4f, .1f, true);
        }
        return POSITION_SHAPE.clone();
    }

    private static Mesh getIndicatorShape() {
        if (INDICATOR_SHAPE == null) {
            INDICATOR_SHAPE = new Cylinder(10, 16, .1f, .2f, true);
        }
        return INDICATOR_SHAPE.clone();
    }

    public Node getNode() {
        return node;
    }

    public void setPositionMaterial(Material material) {
        positionShape.setMaterial(material);
    }

    public void setHasRed(boolean hasRed) {
        if (this.hasRed != hasRed) {
            if (hasRed) {
                node.attachChild(redIndicator);
            } else {
                redIndicator.removeFromParent();
            }
        }
        this.hasRed = hasRed;
    }

    public void setStackSize(int stackSize) {
        if (this.stackSize != stackSize) {
            if (stackSize == 0) {
                text.removeFromParent();
            } else {
                text.setText(String.valueOf(stackSize));
            }
            this.stackSize = stackSize;
        }
    }

    public void setGlow(boolean b) {

    }

    public BitmapText getText() {
        return text;
    }

    public static class PositionWrapped implements Savable {
        private Board.Position position;

        public PositionWrapped() {

        }

        public PositionWrapped(Board.Position position) {
            this.position = position;
        }

        public Board.Position getPosition() {
            return position;
        }

        @Override
        public void write(JmeExporter jmeExporter) throws IOException {
            OutputCapsule capsule = jmeExporter.getCapsule(this);
            capsule.write(position.toString(), "position", "");
        }

        @Override
        public void read(JmeImporter jmeImporter) throws IOException {
            InputCapsule capsule = jmeImporter.getCapsule(this);
            String positionName = capsule.readString("position", "");
            for (Board.Position position : GameViewer.getGame().getBoard().getPositions()) {
                if (positionName.equals(position.toString())) {
                    this.position = position;
                    return;
                }
            }

        }
    }
}
