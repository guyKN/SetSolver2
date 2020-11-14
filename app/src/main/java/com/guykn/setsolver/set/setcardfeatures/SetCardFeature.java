package com.guykn.setsolver.set.setcardfeatures;

public abstract class SetCardFeature<FeatureEnum extends SetCardFeatureEnum> {
    //todo: implement check for equality with other featureEnums
    //todo: make this start staticly, to speed up runtime

    public SetCardFeature(double confidence){
        this.confidence = confidence;
    }

    protected FeatureEnum getEnumFromId(int id){
        for(FeatureEnum featureEnum : getFeatureEnumValues()){
            if (featureEnum.getId() == id) {
                return featureEnum;
            }
        }
        throw new IllegalArgumentException("Id does not match : " + id);
    }



    public final double confidence;
    public double getConfidence(){
        return confidence;
    }

    protected abstract FeatureEnum[] getFeatureEnumValues();
    public abstract String getName();
    public abstract String getDescription();

}
