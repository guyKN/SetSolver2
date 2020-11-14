package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

public class SetCardFill extends SetCardFeature<SetCardFill.SetCardFillEnum> {
    public enum SetCardFillEnum implements SetCardFeatureEnum {
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

    public final SetCardFillEnum fillEnum;

    private SetCardFill(SetCardFillEnum fillEnum, double confidence) {
        super(confidence);
        this.fillEnum = fillEnum;
    }

    public SetCardFill(ClassificationResult result){
        super(result.probability);
        this.fillEnum = getEnumFromId(result.id);
    }

    @Override
    public String getDescription(){
        return String.format(Locale.US,
                "%.0f%% confidence that the cards' fill is %s",
                confidence *100, getName());
    }

    @Override
    public String getName() {
        return fillEnum.getName();
    }

    protected SetCardFillEnum[] getFeatureEnumValues(){
        return SetCardFillEnum.values();
    }

}
