package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

public class SetCardCount extends SetCardFeature<SetCardCount.SetCardCountEnum> {
    enum SetCardCountEnum implements SetCardFeatureEnum {
        ONE_SYMBOL(0, "1 Symbol"), TWO_SYMBOLS(1, "2 Symbols"), THREE_SYMBOLS(2, "3 Symbols");
        private final int id;
        private final String name;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        private SetCardCountEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    final private double certainty;
    final private SetCardCountEnum count;


    private SetCardCount(SetCardCountEnum count, double certainty) {
        this.certainty = certainty;
        this.count = count;
    }

    public SetCardCount(ClassificationResult result) {
        this.count = getEnumFromId(result.id);
        this.certainty = result.probability;
    }

    @Override
    public String getDescription() {
        return String.format(Locale.US,
                "%.0f%% confidence that there are %s",
                certainty * 100, count.toString());
    }

    @Override
    public String getName() {
        return count.getName();
    }

    protected SetCardCountEnum[] getFeatureEnumValues() {
        return SetCardCountEnum.values();
    }

}
