package com.guykn.setsolver.set;

import com.guykn.setsolver.drawing.DrawableList;

//todo: implement logic to find sets
public class SetBoardPosition extends DrawableList<SetCard> {
    public void addCard(SetCard card){
        addDrawable(card);
    }
}
