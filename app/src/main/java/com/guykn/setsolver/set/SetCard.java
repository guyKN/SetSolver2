package com.guykn.setsolver.set;


import androidx.annotation.NonNull;

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

    protected SetCard(GenericRotatedRectangle rotatedRect,
            SetCardColor color, SetCardCount count, SetCardFill fill, SetCardShape shape) {
        super(rotatedRect);
        this.color = color;
        this.count = count;
        this.fill = fill;
        this.shape = shape;
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
