package com.guykn.setsolver.set;

import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

/**
 * Like a setCard, but doesn't store a position for the card
 */
public class PositionlessSetCard {
    //Todo: check if there is a better implementation (Maybe Use a decorator pattern: https://en.wikipedia.org/wiki/Decorator_pattern)
    private SetCardColor color;
    private SetCardCount count;
    private SetCardFill fill;
    private SetCardShape shape;

    public SetCardColor getColor() {
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

    public PositionlessSetCard(SetCardColor color, SetCardCount count, SetCardFill fill, SetCardShape shape){
        this.color = color;
        this.count = count;
        this.fill = fill;
        this.shape = shape;
    }
}
