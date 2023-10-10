package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonObject;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.condition.ResourceConditionContext;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.oregrowth.OreGrowthConfig;

/**
 * Created 10/10/2023 by SuperMartijn642
 */
public class OreGrowthDefaultRecipeCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public boolean test(ResourceConditionContext context){
        return OreGrowthConfig.enableRecipes.get();
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<OreGrowthDefaultRecipeCondition> {

        @Override
        public void serialize(JsonObject json, OreGrowthDefaultRecipeCondition condition){
        }

        @Override
        public OreGrowthDefaultRecipeCondition deserialize(JsonObject json){
            return new OreGrowthDefaultRecipeCondition();
        }
    }
}
