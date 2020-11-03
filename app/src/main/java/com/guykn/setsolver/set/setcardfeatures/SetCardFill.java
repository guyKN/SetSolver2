package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

public class SetCardFill extends SetCardFeature<SetCardFill.SetCardFillEnum> {
    enum SetCardFillEnum implements SetCardFeatureEnum {
        EMPTY(0, "Empty"), PARTIAL(1, "Partial"), FULL(2, "Full");

        private final String name;
        public String getName() {
            return name;
        }
        private final int id;
        public int getId() {
            return id;
        }

        private SetCardFillEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

    }
    private final SetCardFillEnum fill;

    private SetCardFill(SetCardFillEnum fill, double certainty) {
        this.fill = fill;
        this.confidence = certainty;
    }

    public SetCardFill(ClassificationResult result){
        this.fill = getEnumFromId(result.id);
        this.confidence = result.probability;
    }

    @Override
    public String getDescription(){
        return String.format(Locale.US,
                "%.0f%% confidence that the cards' fill is %s",
                confidence *100, getName());
    }

    @Override
    public String getName() {
        return fill.getName();
    }

    protected SetCardFillEnum[] getFeatureEnumValues(){
        return SetCardFillEnum.values();
    }

}
