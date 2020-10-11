package com.guykn.setsolver.set;


import androidx.annotation.NonNull;

import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.util.Locale;

public class SetCard {
    private SetCardPosition pos;
    private SetCardColor color;
    private SetCardCount count;
    private SetCardFill fill;
    private SetCardShape shape;

    public SetCard(SetCardPosition pos, SetCardColor color, SetCardCount count, SetCardFill fill, SetCardShape shape) {
        this.pos = pos;
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

    public SetCardPosition getPos() {
        return pos;
    }
}
