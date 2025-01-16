package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.demo.nn.ActivationFunctions.Type;

public class ActiveLayerParameters extends LayerParameters{
    private Type activation;

    public ActiveLayerParameters(Type type) {
        super("ACT");
        this.activation = type;
    }

    public Type getActivation() {
        return activation;
    }
}
