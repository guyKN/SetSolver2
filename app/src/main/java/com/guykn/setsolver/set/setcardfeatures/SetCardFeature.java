package com.guykn.setsolver.set.setcardfeatures;

public abstract class SetCardFeature<FeatureEnum extends SetCardFeatureEnum> {
    //todo: implement check for equality with other featureEnums
    protected FeatureEnum getEnumFromId(int id){
        for(FeatureEnum featureEnum : getFeatureEnumValues()){
            if (featureEnum.getId() == id) {
                return featureEnum;
            }
        }
        throw new IllegalArgumentException("Id must be between 0 and 2. Given: " + id);
    }

    protected double certainty;
    public double getCertainty(){
        return certainty;
    }

    public abstract String toString();
    protected abstract FeatureEnum[] getFeatureEnumValues();

}
