package com.etherblood.ethercards.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.destrostudios.cardgui.samples.visualization.cards.modelled.SimpleModelledCard;
import com.etherblood.ethercards.rules.templates.Tribe;
import com.etherblood.ethercards.templates.api.CardColor;
import com.etherblood.ethercards.templates.api.DisplayCardTemplate;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyCardVisualizer extends CardVisualizer<CardModel> {

    private static final Map<String, Texture> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> IMAGE_CACHE = new ConcurrentHashMap<>();
    private static final Font FONTTITLE = new Font("Tahoma", Font.BOLD, 28);
    private static final Font FONTSTATS = new Font("Tahoma", Font.BOLD, 50);
    private static final Font FONTFLAVORTEXT = new Font("Tahoma", Font.ITALIC, 20);
    private static final Font FONTTRIBES = new Font("Tahoma", Font.BOLD, 20);
    private static final Font FONTDESCRIPTION = new Font("Tahoma", Font.PLAIN, 20);
    private static final Font FONTKEYWORDS = new Font("Tahoma", Font.BOLD, 20);
    private static final int LINEWIDTH = 306;
    private static final int MAX_TEXTURE_WIDTH = 16384;
    private final CardImages cardImages;
    private final int textureWidth = 400;
    private final int textureHeight = 560;
    private static final float MILLIS_TO_SECONDS_FACTOR = 0.001f;

    public MyCardVisualizer(CardImages cardImages) {
        this.cardImages = cardImages;
    }

    @Override
    protected void updateVisualizationObject(CardVisualization visualization, Card<CardModel> card, AssetManager assetManager) {
        CardModel cardModel = card.getModel();
        boolean minified = cardModel.isInBattleZone() && !cardModel.isInspected();
        DisplayCardTemplate template = cardModel.getTemplate();
        List<CardColor> colors = template.getColors();
        boolean faceDown = !cardModel.isFaceUp();

        String alias = template.getAlias();
        int artworkTiles = template.getFrames();
        // TODO: split into rows/cols more optimally
        int rows = ceilDiv(artworkTiles * textureWidth, MAX_TEXTURE_WIDTH);
        int cols = ceilDiv(artworkTiles, rows);
        int artworkX = (minified ? 0 : 36);
        int artworkY = (minified ? 0 : 68);
        int artworkWidth = (minified ? textureWidth : 328);
        int artworkHeight = (minified ? textureHeight : 242);

        Texture textureCardBack = TEXTURE_CACHE.computeIfAbsent("cardback", key -> {
            PaintableImage imageBackground = new PaintableImage(textureWidth, textureHeight);
            imageBackground.setBackground_Alpha(0);
            BufferedImage art = cardImages.readAndScaleImage(CardVisualization.CARDBACK_IMAGE_PATH, textureWidth, textureHeight);
            imageBackground.paintImage(art, 0, 0);
            imageBackground.flipY();
            return new Texture2D(imageBackground.getImage());
        });

        Texture textureEmpty = TEXTURE_CACHE.computeIfAbsent("empty", key -> {
            PaintableImage imageBackground = new PaintableImage(textureWidth, textureHeight);
            imageBackground.setBackground_Alpha(0);
            imageBackground.flipY();
            return new Texture2D(imageBackground.getImage());
        });

        String backgroundKey = "background_" + minified + "_" + enumCollectionToLong(colors);
        Texture textureBackground = minified ? textureEmpty : faceDown ? textureCardBack : TEXTURE_CACHE.computeIfAbsent(backgroundKey, key -> createBackgroundTexture(colors, minified));

        String artworkKey = "artwork_" + minified + "_" + template.getImagePath();
        Texture textureArtwork = faceDown ? textureEmpty : TEXTURE_CACHE.computeIfAbsent(artworkKey, key -> createArtworkTexture(artworkTiles, cardModel, artworkWidth, artworkHeight, artworkX, artworkY, cols, rows));

        Integer attack = cardModel.getAttack();
        Integer health = cardModel.getHealth();
        boolean damaged = cardModel.isDamaged();
        Texture textureBattleStats = faceDown ? textureEmpty : TEXTURE_CACHE.computeIfAbsent("battle_" + attack + "_" + health + "_" + damaged, key -> createBattleStatsTexture(attack, health, damaged));

        String foilMapKey = "foilmap_" + minified + "_" + (attack != null) + "_" + (health != null);
        Texture textureFoilMap = faceDown ? textureEmpty : cardModel.isFoil() ? TEXTURE_CACHE.computeIfAbsent(foilMapKey, key -> createFoilTexture(artworkWidth, artworkHeight, artworkX, artworkY, attack, health)) : textureEmpty;

        Texture textureDetails = faceDown ? textureEmpty : minified ? textureEmpty : TEXTURE_CACHE.computeIfAbsent("details_" + alias, key -> createDetailsTexture(cardModel));

        Texture textureMulligan = cardModel.isMulligan() && !faceDown ? TEXTURE_CACHE.computeIfAbsent("mulligan", key -> createMulliganTexture()) : textureEmpty;

        Material material = visualization.getMaterial_Front();
        material.setTexture("DiffuseMap1", textureBackground);
        material.setTexture("DiffuseMap2", textureArtwork);
        float loopSeconds = template.getLoopMillis() * MILLIS_TO_SECONDS_FACTOR;
        material.setFloat("DiffuseMapInterval2", loopSeconds);
        material.setInt("DiffuseMapTilesX2", cols);
        material.setInt("DiffuseMapTilesY2", rows);
        material.setTexture("DiffuseMap3", textureDetails);
        material.setTexture("DiffuseMap4", textureBattleStats);
        material.setTexture("DiffuseMap5", textureMulligan);
        material.setTexture("FoilMap", textureFoilMap);

        if (cardModel.getGlow() != null) {
            visualization.setGlow(cardModel.getGlow());
        } else {
            visualization.removeGlow();
        }
    }

    private Texture2D createBackgroundTexture(List<CardColor> colors, boolean minified) {
        PaintableImage imageBackground = new PaintableImage(textureWidth, textureHeight);
        imageBackground.setBackground(Color.BLACK);
        BufferedImage bufferedImageBackground = getCardBackgroundImage(colors, minified);
        imageBackground.paintSameSizeImage(new PaintableImage(bufferedImageBackground));
        imageBackground.flipY();
        return new Texture2D(imageBackground.getImage());
    }

    private Texture2D createMulliganTexture() {
        BufferedImage bufferedImage = cardImages.readImage("images/mulligan.png");
        return new Texture2D(new PaintableImage(bufferedImage).getImage());
    }

    private Texture2D createArtworkTexture(int artworkTiles, CardModel cardModel, int artworkWidth, int artworkHeight, int artworkX, int artworkY, int cols, int rows) {
        PaintableImage imageArtwork = new PaintableImage(cols * textureWidth, rows * textureHeight);
        imageArtwork.setBackground_Alpha(0);
        BufferedImage art = cardImages.readAndScaleImage(cardImages.getCardImageFilePath(cardModel), artworkTiles * artworkWidth, artworkHeight);

        PaintableImage srcImage = new PaintableImage(art);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int sourceX = ((y * cols) + x) * artworkWidth;
                int sourceY = 0;
                int sourceWidth = artworkWidth;
                int sourceHeight = artworkHeight;

                int destX = (x * textureWidth) + artworkX;
                int destY = (y * textureHeight) + artworkY;
                int destWidth = artworkWidth;
                int destHeight = artworkHeight;

                imageArtwork.paintImage(srcImage,
                        sourceX, sourceY, sourceWidth, sourceHeight,
                        destX, destY, destWidth, destHeight);
            }
        }
        imageArtwork.flipY();
        return new Texture2D(imageArtwork.getImage());
    }

    private int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }

    private Texture2D createFoilTexture(int artworkWidth, int artworkHeight, int artworkX, int artworkY, Integer attack, Integer health) {
        PaintableImage imageFoilMap = new PaintableImage(textureWidth, textureHeight);
        imageFoilMap.setBackground_Alpha(0);
        for (int x = 0; x < artworkWidth; x++) {
            for (int y = 0; y < artworkHeight; y++) {
                imageFoilMap.setPixel_Alpha(artworkX + x, artworkY + y, 255);
            }
        }

        BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        drawStats(imageGraphics, attack, health, false);
        imageGraphics.dispose();
        imageFoilMap.removeByAlphaMask(new PaintableImage(image));

        imageFoilMap.flipY();
        return new Texture2D(imageFoilMap.getImage());
    }

    private Texture2D createDetailsTexture(CardModel cardModel) {
        DisplayCardTemplate template = cardModel.getTemplate();
        BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D front = image.createGraphics();

        front.setFont(FONTTITLE);
        front.setColor(Color.BLACK);
        front.drawString(template.getName(), 35, 57);

        int textStartX = 45;
        Integer manaCost = template.getHand().getCast().getManaCost();
        if (manaCost != null) {
            drawCardCostManaAmount(front, manaCost, (textureWidth - textStartX + 20), (46 + 15), FONTSTATS);
        }
        int tmpY = 370;
        List<String> drawnKeywords = new ArrayList<>(cardModel.getKeywords());
        drawnKeywords.sort(Comparator.naturalOrder());
        if (drawnKeywords.size() > 0) {
            String keywordsText = "";
            for (int i = 0; i < drawnKeywords.size(); i++) {
                if (i != 0) {
                    keywordsText += " ";
                }
                String keyword = drawnKeywords.get(i);
                keywordsText += keyword;
            }
            front.setColor(Color.BLACK);
            front.setFont(FONTKEYWORDS);
            int tmpX = textStartX;
            tmpY = drawStringMultiLine(front, keywordsText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 24;
        }
        front.setColor(Color.BLACK);
        String description = template.getDescription();
        if (description != null) {
            front.setFont(FONTDESCRIPTION);
            int tmpX = textStartX;
            tmpY = drawStringMultiLine(front, description, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 24;
        }
        String flavourText = template.getFlavourText();
        if (flavourText != null) {
            int tmpX = textStartX;
            front.setFont(FONTFLAVORTEXT);
            tmpY = drawStringMultiLine(front, flavourText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
        }
        drawStats(front, cardModel.getAttack(), cardModel.getHealth(), cardModel.isDamaged());
        Set<Tribe> tribes = cardModel.getTemplate().getTribes();
        if (!tribes.isEmpty()) {
            String tribesText = "";
            for (Tribe tribe : tribes) {
                if (!tribesText.isEmpty()) {
                    tribesText += ", ";
                }
                tribesText += tribe;
            }
            front.setFont(FONTTRIBES);
            front.setColor(Color.BLACK);
            front.drawString(tribesText, textStartX, 334);
        }
        front.dispose();
        PaintableImage paintable = new PaintableImage(image);
        paintable.flipY();
        return new Texture2D(paintable.getImage());
    }

    //http://stackoverflow.com/questions/4413132/problems-with-newline-in-graphics2d-drawstring
    public int drawStringMultiLine(Graphics2D graphics, String text, int lineWidth, int startX, int followingX, int y, int linesGap) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int x = startX;
        String currentLine = text;
        if (fontMetrics.stringWidth(currentLine) < (lineWidth - (x - followingX))) {
            graphics.drawString(currentLine, x, y);
        } else {
            String[] words = text.split(" ");
            currentLine = words[0];
            for (int i = 1; i < words.length; i++) {
                if (fontMetrics.stringWidth(currentLine + words[i]) < (lineWidth - (x - followingX))) {
                    currentLine += " " + words[i];
                } else {
                    graphics.drawString(currentLine, x, y);
                    y += (fontMetrics.getHeight() + linesGap);
                    x = followingX;
                    currentLine = words[i];
                }
            }
            if (currentLine.trim().length() > 0) {
                graphics.drawString(currentLine, x, y);
            }
        }
        return y;
    }

    private void drawCardCostManaAmount(Graphics2D graphics, int manaCost, int endX, int y, Font font) {
        graphics.setFont(font);
        int startX = (int) (endX - font.getStringBounds(Integer.toString(manaCost), graphics.getFontRenderContext()).getWidth());
        drawOutlinedText(graphics, Integer.toString(manaCost), startX, y, Color.BLACK, Color.WHITE);
    }

    private Texture2D createBattleStatsTexture(Integer attack, Integer health, boolean damaged) {
        BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        drawStats(imageGraphics, attack, health, damaged);
        imageGraphics.dispose();
        PaintableImage paintable = new PaintableImage(image);
        paintable.flipY();
        return new Texture2D(paintable.getImage());
    }

    private void drawStats(Graphics2D graphics, Integer attack, Integer health, boolean damaged) {
        int tmpY = 513;
        BufferedImage statImage = IMAGE_CACHE.computeIfAbsent("images/templates/stat.png", cardImages::readImage);
        if (attack != null) {
            graphics.drawImage(statImage, 29, 458, 73, 73, null);
            String attackDamageText = ("" + attack);
            graphics.setFont(FONTSTATS);
            Rectangle2D attackDamageBounds = graphics.getFontMetrics().getStringBounds(attackDamageText, graphics);
            int tmpX = (int) (65 - (attackDamageBounds.getWidth() / 2));
            drawOutlinedText(graphics, attackDamageText, tmpX, tmpY, Color.BLACK, Color.WHITE);
        }
        if (health != null) {
            graphics.drawImage(statImage, 298, 458, 73, 73, null);
            String lifepointsText = ("" + health);
            graphics.setFont(FONTSTATS);
            Rectangle2D lifepointsBounds = graphics.getFontMetrics().getStringBounds(lifepointsText, graphics);
            int tmpX = (int) (335 - (lifepointsBounds.getWidth() / 2));
            drawOutlinedText(graphics, lifepointsText, tmpX, tmpY, Color.BLACK, (damaged ? Color.RED : Color.WHITE));
        }
    }

    private void drawOutlinedText(Graphics2D graphics, String text, int x, int y, Color outlineColor, Color textColor) {
        graphics.setColor(outlineColor);
        int strength = 2;
        graphics.drawString(text, x - strength, y - strength);
        graphics.drawString(text, x + 0, y - strength);
        graphics.drawString(text, x + strength, y - strength);
        graphics.drawString(text, x - strength, y + 0);
        graphics.drawString(text, x + 0, y + 0);
        graphics.drawString(text, x + strength, y + 0);
        graphics.drawString(text, x - strength, y + strength);
        graphics.drawString(text, x + 0, y + strength);
        graphics.drawString(text, x + strength, y + strength);
        graphics.setColor(textColor);
        graphics.drawString(text, x, y);
    }

    public static <T extends Enum<T>> long enumCollectionToLong(Collection<T> set) {
        long r = 0;
        for (T value : set) {
            r |= 1L << value.ordinal();
        }
        return r;
    }

    public BufferedImage getCardBackgroundImage(List<CardColor> colors, boolean minified) {
        String type = minified ? "rect" : "full";
        int partWidth = Math.round(((float) textureWidth) / colors.size());
        BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        int x = 0;
        int lineX;
        for (CardColor color : colors) {
            for (int i = (int) (-0.5f * partWidth); i < (1.5f * partWidth); i++) {
                lineX = (x + i);
                if (lineX > 0) {
                    if (lineX >= textureWidth) {
                        break;
                    }
                    Image templateImage = IMAGE_CACHE.computeIfAbsent("images/templates/template_" + type + "_" + color.ordinal() + ".png", cardImages::readImage);
                    float alpha;
                    if (((i < (0.5 * partWidth)) && (lineX < (partWidth / 2))) || (i > (0.5 * partWidth))) {
                        alpha = 1;
                    } else {
                        alpha = (1 - (Math.abs(((((float) i) / partWidth) - 0.5f))));
                    }
                    imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    while (!imageGraphics.drawImage(templateImage, lineX, 0, lineX + 1, textureHeight, lineX, 0, lineX + 1, textureHeight, null)) {
                        //http://stackoverflow.com/questions/20442295/drawimage-wont-work-but-drawrect-does
                    }
                }
            }
            x += partWidth;
        }
        imageGraphics.dispose();
        return image;
    }
}
