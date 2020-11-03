package com.guykn.setsolver.set;


import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

public class SetCard extends GenericRotatedRectangle {

    final private PositionlessSetCard card;

    //Todo: check if there is a better implementation (Maybe Use a decorator pattern: https://en.wikipedia.org/wiki/Decorator_pattern)
    public SetCard(GenericRotatedRectangle rotatedRect,
                   PositionlessSetCard card) {
        super(rotatedRect);
        this.card = card;
    }


    public SetCardColor getColor() {
        return card.getColor();
    }

    public SetCardCount getCount() {
        return card.getCount();
    }

    public SetCardFill getFill() {
        return card.getFill();
    }

    public SetCardShape getShape() {
        return card.getShape();
    }
}
