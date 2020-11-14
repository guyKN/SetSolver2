package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

public class SetCardCount extends SetCardFeature<SetCardCount.SetCardCountEnum> {
    public enum SetCardCountEnum implements SetCardFeatureEnum {
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

    final public SetCardCountEnum countEnum;


    private SetCardCount(SetCardCountEnum countEnum, double confidence) {
        super(confidence);
        this.countEnum = countEnum;
    }

    public SetCardCount(ClassificationResult result) {
        super(result.probability);
        this.countEnum = getEnumFromId(result.id);
    }

    @Override
    public String getDescription() {
        return String.format(Locale.US,
                "%.0f%% confidence that there are %s",
                confidence * 100, countEnum.toString());
    }

    @Override
    public String getName() {
        return countEnum.getName();
    }

    protected SetCardCountEnum[] getFeatureEnumValues() {
        return SetCardCountEnum.values();
    }

}
