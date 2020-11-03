package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.R;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

public class SetCardColor extends SetCardFeature<SetCardColor.SetCardColorEnum>{

    enum SetCardColorEnum implements SetCardFeatureEnum {
        RED(0, "Red", R.color.redCard),
        PURPLE(1, "Purple", R.color.purpleCard),
        GREEN(2, "Green", R.color.greenCard);

        private final String name;

        public int getColorCode() {
            return colorCode;
        }

        private final int colorCode;

        public String getName() {
            return name;
        }

        private final int id;

        public int getId() {
            return id;
        }

        private SetCardColorEnum(int id, String name, int colorCode) {
            this.id = id;
            this.name = name;
            this.colorCode = colorCode;
        }


    }

    private final SetCardColorEnum color;


    private SetCardColor(SetCardColorEnum color, double certainty) {
        this.confidence = certainty;
        this.color = color;
    }

    public SetCardColor(ClassificationResult result){
        this.color = getEnumFromId(result.id);
        this.confidence = result.probability;
    }

    @Override
    public String getDescription(){
        return String.format(Locale.US,
                "%.0f%% confidence that the card is %s",
                confidence *100, getName());
    }

    @Override
    public String getName() {
        return color.getName();
    }

    protected SetCardColorEnum[] getFeatureEnumValues(){
        return SetCardColorEnum.values();
    }


}
