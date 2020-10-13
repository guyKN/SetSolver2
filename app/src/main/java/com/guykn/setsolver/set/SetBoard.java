package com.guykn.setsolver.set;

import com.guykn.setsolver.drawing.DrawableList;

public class SetBoard extends DrawableList<SetCard> {
    public void addCard(SetCard card){
        addDrawable(card);
    }
}
