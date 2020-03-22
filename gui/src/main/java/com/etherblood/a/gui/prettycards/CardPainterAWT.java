package com.etherblood.a.gui.prettycards;

import com.etherblood.a.templates.CardColor;
import com.destrostudios.cardgui.samples.visualisation.PaintableImage;
import com.destrostudios.cardgui.samples.visualisation.SimpleCardVisualizer;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

public class CardPainterAWT extends SimpleCardVisualizer<CardModel> {

    private static final Font FONTTITLE = new Font("Tahoma", Font.BOLD, 28);
    private static final Font FONTDESCRIPTION = new Font("Tahoma", Font.PLAIN, 20);
    private static final Font FONTKEYWORDS = new Font("Tahoma", Font.BOLD, 20);
    private static final Font FONTEFFECTS = new Font("Tahoma", Font.PLAIN, 20);
    private static final Font FONTFLAVORTEXT = new Font("Tahoma", Font.ITALIC, 20);
    private static final Font FONTTRIBES = new Font("Tahoma", Font.BOLD, 20);
    private static final Font FONTSTATS = new Font("Tahoma", Font.BOLD, 50);

    private static final int LINEWIDTH = 306;
    private static final int CARDCOSTICONSIZE = 24;
    private static final int CARDCOSTGAPSIZE = 2;
    private static final int EFFECTSICONSIZE = 15;
    private static final int EFFECTSGAPSIZE = 2;

    private static final HashMap<String, BufferedImage> LETTERIMAGES = new HashMap<>();

    private final CardImages cardImages;
    private final HashMap<String, BufferedImage> cardBackgroundImages = new HashMap<>();
    private int tmpX, tmpY;

    public CardPainterAWT(CardImages cardImages) {
        super(400, 560);
        this.cardImages = cardImages;
    }

    @Override
    public PaintableImage paintCard(CardModel cardModel) {
        PaintableImage paintableImage = new PaintableImage(400, 560);
        BufferedImage bufferedImage = new BufferedImage(paintableImage.getWidth(), paintableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        drawCard(graphics, cardModel, bufferedImage.getWidth(), bufferedImage.getHeight());
        paintableImage.loadImage(bufferedImage, true);
        return paintableImage;
    }

    private void drawCard(Graphics2D graphics, CardModel cardModel, int width, int height) {
        DisplayCardTemplate template = cardModel.getTemplate();
        graphics = (Graphics2D) graphics.create();
//        if (cardModel.isFaceUp()) {
//            List<String> drawnKeywords = new LinkedList<>();
//            drawnKeywords.addAll(cardModel.getKeywords());
//            String castDescription = cardModel.getCastDescription();
//            if (castDescription != null) {
//                drawnKeywords.add("Cast");
//            }
        graphics.setColor(Color.WHITE);
        graphics.fillRect(35, 68, 329, 242);
        String imageFilePath = cardImages.getCardImageFilePath(cardModel);
        graphics.drawImage(cardImages.getCachedImage(imageFilePath, 329, 242), 35, 68, null);
        List<CardColor> colors = template.getColors();
        graphics.drawImage(getCardBackgroundImage(colors, width, height), 0, 0, null);
        graphics.setFont(FONTTITLE);
        graphics.setColor(Color.BLACK);
        String title = template.getName();
        int textStartX = 45;
        if (title != null) {
            graphics.drawString(title, 35, 57);
        }
//        graphics.drawImage(cardImages.getCachedImage("images/templates/stat.png"), 314, 7, 73, 73, null);
        graphics.setFont(FONTSTATS);
        drawCardCostManaAmount(graphics, template.getAttackPhaseCast() != null ? template.getAttackPhaseCast().getManaCost() : template.getBlockPhaseCast().getManaCost(), (width - textStartX + 20), (46 + 15), FONTSTATS);
        tmpY = 370;
//            if (drawnKeywords.size() > 0) {
//                String keywordsText = "";
//                for (int i = 0; i < drawnKeywords.size(); i++) {
//                    if (i != 0) {
//                        keywordsText += " ";
//                    }
//                    String keyword = drawnKeywords.get(i);
//                    keywordsText += keyword + (keyword.equals("Cast") ? ":" : ".");
//                }
//                graphics.setFont(FONTKEYWORDS);
//                tmpX = textStartX;
//                drawStringMultiLine(graphics, keywordsText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
//                if (castDescription != null) {
//                    tmpX += 3;
//                    drawSpellDescription(graphics, castDescription, LINEWIDTH, tmpX, textStartX, tmpY);
//                }
//                tmpY += 18;
//            }
        graphics.setColor(Color.BLACK);
        String description = template.getDescription();
        if (description != null) {
            graphics.setFont(FONTDESCRIPTION);
            tmpX = textStartX;
            drawStringMultiLine(graphics, description, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 18;
        }
//            List<Spell> spells = cardModel.getSpells();
//            if (spells != null) {
//                for (Spell spell : spells) {
//                    tmpX = textStartX;
//                    if (spell.getCost() != null) {
//                        drawCost(graphics, spell.getCost(), lineWidth, tmpX, textStartX, tmpY);
//                        tmpX += 3;
//                    }
//                    drawSpellDescription(graphics, spell.getDescription(), lineWidth, tmpX, textStartX, tmpY);
//                    tmpY += 18;
//                }
//            }
        String flavourText = template.getFlavourText();
        if (flavourText != null) {
            tmpX = textStartX;
            graphics.setFont(FONTFLAVORTEXT);
            drawStringMultiLine(graphics, flavourText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 18;
        }
        tmpY = 514;
//        int attackDamage = cardModel.getAttack();
//        graphics.drawImage(cardImages.getCachedImage("images/templates/stat.png"), 29, 458, 73, 73, null);
//        String attackDamageText = ("" + attackDamage);
//        graphics.setFont(FONTSTATS);
//        Rectangle2D attackDamageBounds = graphics.getFontMetrics().getStringBounds(attackDamageText, graphics);
//        tmpX = (int) (65 - (attackDamageBounds.getWidth() / 2));
//        drawOutlinedText(graphics, attackDamageText, tmpX, tmpY, Color.BLACK, Color.WHITE);
//        int lifepoints = cardModel.getHealth();
//        graphics.drawImage(cardImages.getCachedImage("images/templates/stat.png"), 298, 458, 73, 73, null);
//        String lifepointsText = ("" + lifepoints);
//        graphics.setFont(FONTSTATS);
//        Rectangle2D lifepointsBounds = graphics.getFontMetrics().getStringBounds(lifepointsText, graphics);
//        tmpX = (int) (335 - (lifepointsBounds.getWidth() / 2));
//        drawOutlinedText(graphics, lifepointsText, tmpX, tmpY, Color.BLACK, (cardModel.isDamaged() ? Color.RED : Color.WHITE));
//            List<String> tribes = cardModel.getTribes();
//            if (tribes.size() > 0) {
//                String tribesText = "";
//                for (int i = 0; i < tribes.size(); i++) {
//                    if (i != 0) {
//                        tribesText += ", ";
//                    }
//                    tribesText += tribes.get(i);
//                }
        graphics.setFont(FONTTRIBES);
        graphics.setColor(Color.BLACK);
        graphics.drawString(template.getAttackPhaseCast() != null ? "Sorcery" : "Instant", textStartX, 334);
//            }
//        } else {
//            graphics.drawImage(CardImages.getCachedImage("images/cardback.png"), 0, 0, width, height, null);
//        }
        graphics.dispose();
    }

    public PaintableImage paintMinion(MinionModel cardModel) {
        PaintableImage paintableImage = new PaintableImage(400, 560);
        BufferedImage bufferedImage = new BufferedImage(paintableImage.getWidth(), paintableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        drawMinion(graphics, cardModel, bufferedImage.getWidth(), bufferedImage.getHeight());
        paintableImage.loadImage(bufferedImage, true);
        return paintableImage;
    }

    private void drawMinion(Graphics2D graphics, MinionModel cardModel, int width, int height) {
        DisplayMinionTemplate template = cardModel.getTemplate();
        graphics = (Graphics2D) graphics.create();
//        if (cardModel.isFaceUp()) {
//            List<String> drawnKeywords = new LinkedList<>();
//            drawnKeywords.addAll(cardModel.getKeywords());
//            String castDescription = cardModel.getCastDescription();
//            if (castDescription != null) {
//                drawnKeywords.add("Cast");
//            }
        graphics.setColor(Color.WHITE);
        graphics.fillRect(35, 68, 329, 242);
        String imageFilePath = cardImages.getCardImageFilePath(cardModel);
        graphics.drawImage(cardImages.getCachedImage(imageFilePath, 329, 242), 35, 68, null);
        List<CardColor> colors = template.getColors();
        graphics.drawImage(getCardBackgroundImage(colors, width, height), 0, 0, null);
        graphics.setFont(FONTTITLE);
        graphics.setColor(Color.BLACK);
        String title = template.getName();
        int textStartX = 45;
        if (title != null) {
            graphics.drawString(title, 35, 57);
//            graphics.drawString(title, 45, 54);
        }
//        drawCardCostManaAmount(graphics, template.getAttackPhaseCast() != null ? template.getAttackPhaseCast().getManaCost() : template.getBlockPhaseCast().getManaCost(), (width - textStartX - CARDCOSTICONSIZE), (46 - (CARDCOSTICONSIZE / 2)));
        tmpY = 370;
//            if (drawnKeywords.size() > 0) {
//                String keywordsText = "";
//                for (int i = 0; i < drawnKeywords.size(); i++) {
//                    if (i != 0) {
//                        keywordsText += " ";
//                    }
//                    String keyword = drawnKeywords.get(i);
//                    keywordsText += keyword + (keyword.equals("Cast") ? ":" : ".");
//                }
//                graphics.setFont(FONTKEYWORDS);
//                tmpX = textStartX;
//                drawStringMultiLine(graphics, keywordsText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
//                if (castDescription != null) {
//                    tmpX += 3;
//                    drawSpellDescription(graphics, castDescription, LINEWIDTH, tmpX, textStartX, tmpY);
//                }
//                tmpY += 18;
//            }
        graphics.setColor(Color.BLACK);
        String description = template.getDescription();
        if (description != null) {
            graphics.setFont(FONTDESCRIPTION);
            tmpX = textStartX;
            drawStringMultiLine(graphics, description, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 18;
        }
//            List<Spell> spells = cardModel.getSpells();
//            if (spells != null) {
//                for (Spell spell : spells) {
//                    tmpX = textStartX;
//                    if (spell.getCost() != null) {
//                        drawCost(graphics, spell.getCost(), lineWidth, tmpX, textStartX, tmpY);
//                        tmpX += 3;
//                    }
//                    drawSpellDescription(graphics, spell.getDescription(), lineWidth, tmpX, textStartX, tmpY);
//                    tmpY += 18;
//                }
//            }
        String flavourText = template.getFlavourText();
        if (flavourText != null) {
            tmpX = textStartX;
            graphics.setFont(FONTFLAVORTEXT);
            drawStringMultiLine(graphics, flavourText, LINEWIDTH, tmpX, textStartX, tmpY, -2);
            tmpY += 18;
        }
        tmpY = 513;
        int attackDamage = cardModel.getAttack();
        graphics.drawImage(cardImages.getCachedImage("images/templates/stat.png"), 29, 458, 73, 73, null);
        String attackDamageText = ("" + attackDamage);
        graphics.setFont(FONTSTATS);
        Rectangle2D attackDamageBounds = graphics.getFontMetrics().getStringBounds(attackDamageText, graphics);
        tmpX = (int) (65 - (attackDamageBounds.getWidth() / 2));
        drawOutlinedText(graphics, attackDamageText, tmpX, tmpY, Color.BLACK, Color.WHITE);
        int lifepoints = cardModel.getHealth();
        graphics.drawImage(cardImages.getCachedImage("images/templates/stat.png"), 298, 458, 73, 73, null);
        String lifepointsText = ("" + lifepoints);
        graphics.setFont(FONTSTATS);
        Rectangle2D lifepointsBounds = graphics.getFontMetrics().getStringBounds(lifepointsText, graphics);
        tmpX = (int) (335 - (lifepointsBounds.getWidth() / 2));
        drawOutlinedText(graphics, lifepointsText, tmpX, tmpY, Color.BLACK, (cardModel.isDamaged() ? Color.RED : Color.WHITE));
//            List<String> tribes = cardModel.getTribes();
//            if (tribes.size() > 0) {
//                String tribesText = "";
//                for (int i = 0; i < tribes.size(); i++) {
//                    if (i != 0) {
//                        tribesText += ", ";
//                    }
//                    tribesText += tribes.get(i);
//                }
//                graphics.setFont(fontTribes);
//                graphics.setColor(Color.BLACK);
//                graphics.drawString(tribesText, textStartX, 334);
//            }
//        } else {
//            graphics.drawImage(CardImages.getCachedImage("images/cardback.png"), 0, 0, width, height, null);
//        }
        graphics.dispose();
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

    public BufferedImage getCardBackgroundImage(List<CardColor> colors, int width, int height) {
        String key = "";
        for (int i = 0; i < colors.size(); i++) {
            if (i != 0) {
                key += ",";
            }
            key += colors.get(i);
        }
        BufferedImage image = cardBackgroundImages.get(key);
        if (image == null) {
            int partWidth = Math.round(((float) width) / colors.size());
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = image.createGraphics();
            int x = 0;
            int lineX;
            for (CardColor color : colors) {
                for (int i = (int) (-0.5f * partWidth); i < (1.5f * partWidth); i++) {
                    lineX = (x + i);
                    if (lineX > 0) {
                        if (lineX >= width) {
                            break;
                        }
                        Image templateImage = cardImages.getCachedImage("images/templates/template_" + color.ordinal() + ".png");
                        float alpha;
                        if (((i < (0.5 * partWidth)) && (lineX < (partWidth / 2))) || (i > (0.5 * partWidth))) {
                            alpha = 1;
                        } else {
                            alpha = (1 - (Math.abs(((((float) i) / partWidth) - 0.5f))));
                        }
                        imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        while (!imageGraphics.drawImage(templateImage, lineX, 0, lineX + 1, height, lineX, 0, lineX + 1, height, null)) {
                            //http://stackoverflow.com/questions/20442295/drawimage-wont-work-but-drawrect-does
                        }
                    }
                }
                x += partWidth;
            }
            imageGraphics.dispose();
            cardBackgroundImages.put(key, image);
        }
        return image;
    }

    private void drawSpellDescription(Graphics2D graphics, String description, int lineWidth, int startX, int followingX, int y) {
        graphics.setFont(FONTEFFECTS);
        drawStringMultiLine(graphics, description, lineWidth, startX, followingX, y, -2);
    }

    private void drawCost(Graphics2D graphics, int cost, int tiredness, int lineWidth, int startX, int followingX, int y) {
        for (int i = 0; i < tiredness; i++) {
            drawTapIcon(graphics, startX, y);
        }
        drawSpellCostManaAmount(graphics, cost, lineWidth, tmpX, followingX, y);
    }

    private void drawTapIcon(Graphics2D graphics, int x, int y) {
        graphics.drawImage(cardImages.getCachedImage("images/tap.png", EFFECTSICONSIZE, EFFECTSICONSIZE), x, y - 12, EFFECTSICONSIZE, EFFECTSICONSIZE, null);
        x += (EFFECTSICONSIZE + EFFECTSGAPSIZE);
        tmpX = x;
    }

    private void drawSpellCostManaAmount(Graphics2D graphics, int manaCost, int lineWidth, int startX, int followingX, int y) {
        drawOutlinedText(graphics, Integer.toString(manaCost), startX, y, Color.BLACK, Color.WHITE);
//        tmpX = startX;
//        tmpY = y;
//        for (int i = 0; i < manaCost; i++) {
//            if (tmpX > (followingX + lineWidth)) {
//                tmpX = followingX;
//                tmpY += 18;
//            }
//            graphics.drawImage(cardImages.getCachedImage("images/mana/" + CardColor.GREEN.ordinal() + ".png", EFFECTSICONSIZE, EFFECTSICONSIZE), tmpX, tmpY - 12, EFFECTSICONSIZE, EFFECTSICONSIZE, null);
//            tmpX += (EFFECTSICONSIZE + EFFECTSGAPSIZE);
//        }
    }

    private void drawCardCostManaAmount(Graphics2D graphics, int manaCost, int endX, int y, Font font) {
        int startX = (int) (endX - font.getStringBounds(Integer.toString(manaCost), graphics.getFontRenderContext()).getWidth());
        drawOutlinedText(graphics, Integer.toString(manaCost), startX, y, Color.BLACK, Color.WHITE);
//        tmpX = endX;
//        tmpY = y;
//        for (int i = 0; i < manaCost; i++) {
//            graphics.drawImage(cardImages.getCachedImage("images/mana/" + CardColor.GREEN.ordinal() + ".png", CARDCOSTICONSIZE, CARDCOSTICONSIZE), tmpX, tmpY, CARDCOSTICONSIZE, CARDCOSTICONSIZE, null);
//            tmpX -= (CARDCOSTGAPSIZE + CARDCOSTICONSIZE);
//        }
    }

    //http://stackoverflow.com/questions/4413132/problems-with-newline-in-graphics2d-drawstring
    public void drawStringMultiLine(Graphics2D graphics, String text, int lineWidth, int startX, int followingX, int y, int linesGap) {
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
        x += fontMetrics.stringWidth(currentLine);
        tmpX = x;
        tmpY = y;
    }

    private static BufferedImage getLetterImage(Graphics graphics, String letter) {
        BufferedImage image = LETTERIMAGES.get(letter);
        if (image == null) {
            image = createStringImage(graphics, letter);
            LETTERIMAGES.put(letter, image);
        }
        return image;
    }

    //http://stackoverflow.com/questions/10388118/how-to-make-rotated-text-look-good-with-java2d
    public static BufferedImage createStringImage(Graphics graphics, String text) {
        int width = (graphics.getFontMetrics().stringWidth(text) + 5);
        int height = graphics.getFontMetrics().getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        imageGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        imageGraphics.setColor(Color.BLACK);
        imageGraphics.setFont(graphics.getFont());
        imageGraphics.drawString(text, 0, (height - graphics.getFontMetrics().getDescent()));
        imageGraphics.dispose();
        return image;
    }
}
