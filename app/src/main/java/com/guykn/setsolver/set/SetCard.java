package com.guykn.setsolver.set;


import androidx.annotation.NonNull;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.util.Locale;
public class SetCard extends GenericRotatedRectangle {
    private SetCardColor color;
    private SetCardCount count;
    private SetCardFill fill;
    private SetCardShape shape;


    //Todo: check if there is a better implementation (Maybe Use a decorator pattern: https://en.wikipedia.org/wiki/Decorator_pattern)
    public SetCard(GenericRotatedRectangle rotatedRect,
            PositionlessSetCard card) {
        super(rotatedRect);
        this.color = card.getColor();
        this.count = card.getCount();
        this.fill = card.getFill();
        this.shape = card.getShape();
    }

    public double getTotalCertainty(){
        return color.getCertainty() * count.getCertainty() * fill.getCertainty() * shape.getCertainty();
    }

    @Override
    @NonNull
    public String toString(){
        return String.format(Locale.US,
                "%s\n%s\n%s\n%s\nOverall Certainty: %.0f%%",
                color.toString(), count.toString(), fill.toString(), shape.toString(), getTotalCertainty()*100);
    }

    public SetCardColor getColor(){
        return color;
    }
    public SetCardCount getCount() {
        return count;
    }
    public SetCardFill getFill() {
        return fill;
    }
    public SetCardShape getShape() {
        return shape;
    }
}
