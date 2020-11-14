package com.guykn.setsolver.set;

import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.util.Locale;

/**
 * Like a setCard, but doesn't store a position for the card
 */
public class PositionlessSetCard {
    //Todo: check if there is a better implementation (Maybe Use a decorator pattern: https://en.wikipedia.org/wiki/Decorator_pattern)
    final public SetCardColor color;
    final public SetCardCount count;
    final public SetCardFill fill;
    final public SetCardShape shape;


    public PositionlessSetCard(SetCardColor color, SetCardCount count,
                               SetCardFill fill, SetCardShape shape){
        this.color = color;
        this.count = count;
        this.fill = fill;
        this.shape = shape;
    }

    public String getDescription(){
        return String.format(Locale.US,
                "Color: %s (%.0f%% confidence)\n" +
                        "Count %s (%.0f%% confidence)\n" +
                        "Fill: %s (%.0f%% confidence)\n" +
                        "Shape: %s (%.0f%% confidence)\n" +
                        "Total Confidence: %.0f%%",
                color.getName(), color.getConfidence(),
                count.getName(), count.getConfidence(),
                fill.getName(), fill.getConfidence(),
                shape.getName(), shape.getConfidence(),
                getTotalConfidence()*100);
    }

    public String getShortDescription(){
        return String.format(Locale.US,
                "%s (%.0f%%)\n" +
                        "%s (%.0f%%)\n" +
                        "%s (%.0f%%)\n" +
                        "%s (%.0f%%)\n",
                color.getName(), color.getConfidence()*100,
                count.getName(), count.getConfidence()*100,
                fill.getName(), fill.getConfidence()*100,
                shape.getName(), shape.getConfidence()*100);
    }

    public String getVeryShortDescription(){
        return String.format(Locale.US,
                "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n",
                color.getName(),
                count.getName(),
                fill.getName(),
                shape.getName());
    }

    public double getTotalConfidence(){
        return color.getConfidence() * count.getConfidence() * fill.getConfidence() * shape.getConfidence();
    }

    public boolean isSameAs(PositionlessSetCard card){
        return this.color.colorEnum == card.color.colorEnum &&
                this.count.countEnum == card.count.countEnum &&
                this.fill.fillEnum == card.fill.fillEnum &&
                this.shape.shapeEnum == card.shape.shapeEnum;
    }

}
