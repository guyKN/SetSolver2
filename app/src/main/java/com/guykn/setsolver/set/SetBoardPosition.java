package com.guykn.setsolver.set;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.guykn.setsolver.drawing.DrawingCallbackList;
import com.guykn.setsolver.set.setcardfeatures.SetCardFeatureEnum;

import java.util.List;

//todo: implement logic to find sets
public class SetBoardPosition extends DrawingCallbackList<SetCard> {

    private final static boolean adjustColor = false;
    private static final Paint redPaint;
    private static final Paint greenPaint;
    private static final Paint purplePaint;
    private static final Paint textPaint;

    static {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeWidth(20f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    static {
        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(10f);
        redPaint.setStyle(Paint.Style.STROKE);
    }

    static {
        greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(10f);
        greenPaint.setStyle(Paint.Style.STROKE);
    }

    static {
        purplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        purplePaint.setColor(Color.MAGENTA);
        purplePaint.setStrokeWidth(10f);
        purplePaint.setStyle(Paint.Style.STROKE);
    }

    static boolean isSet(SetCard card1, SetCard card2, SetCard card3) {
        return !containsDuplicates(card1, card2, card3) &&
                isFeatureSet(card1.getColorEnum(), card2.getColorEnum(), card3.getColorEnum()) &&
                isFeatureSet(card1.getCountEnum(), card2.getCountEnum(), card3.getCountEnum()) &&
                isFeatureSet(card1.getShapeEnum(), card2.getShapeEnum(), card3.getShapeEnum()) &&
                isFeatureSet(card1.getFillEnum(), card2.getFillEnum(), card3.getFillEnum());
    }

    static boolean containsDuplicates(SetCard card1, SetCard card2, SetCard card3) {
        return card1.isSameAs(card2) ||
                card2.isSameAs(card3) ||
                card3.isSameAs(card1);

    }

    static boolean isFeatureSet(SetCardFeatureEnum feature1,
                                SetCardFeatureEnum feature2,
                                SetCardFeatureEnum feature3) {
        int id1 = feature1.getId();
        int id2 = feature2.getId();
        int id3 = feature3.getId();

        return (((id2 == id3) && (id1 == id3))) ||
                ((id1 != id2) && (id2 != id3) && (id3 != id1));


    }

    public void addCard(SetCard card) {
        addDrawable(card);
    }

    private List<SetCard> getCards() {
        return getDrawables();
    }

    public void findSets() {
        int setId = 0;
        List<SetCard> cards = getCards();


        for(int i1 = 0;i1<cards.size();i1++) {
            SetCard card1 = cards.get(i1);
            for (int i2 = i1 + 1; i2 < cards.size(); i2++) {
                SetCard card2 = cards.get(i2);
                for (int i3 = i2 + 1; i3 < cards.size(); i3++) {
                    SetCard card3 = cards.get(i3);
                    Log.d(TAG, card1.getVeryShotDescription());
                    Log.d(TAG, card2.getVeryShotDescription());
                    Log.d(TAG, card3.getVeryShotDescription());
                    if (isSet(card1, card2, card3)) {
                        Log.d(TAG, "Found set!");
                        Log.d(TAG, card1.getVeryShotDescription());
                        Log.d(TAG, card2.getVeryShotDescription());
                        Log.d(TAG, card3.getVeryShotDescription());

                        card1.addToSet(setId);
                        card2.addToSet(setId);
                        card3.addToSet(setId);
                        setId++;
                    }
                }
            }
        }
    }

    /*
    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        for (SetCard card : getDrawables()) {
            Paint currentPaint = paint;
            if(adjustColor) {
                switch (card.getColor().getName()) {
                    case "Green":
                        currentPaint = greenPaint;
                        break;
                    case "Red":
                        currentPaint = redPaint;
                        break;
                    case "Purple":
                        currentPaint = purplePaint;
                        break;
                    default:
                        throw new IllegalStateException("the given color is neither red, green, nor purple");
                }
            }

            card.drawOnCanvas(canvas, currentPaint);
        }
    }

     */
}
