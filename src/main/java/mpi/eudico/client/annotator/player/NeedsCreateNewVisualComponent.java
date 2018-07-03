package mpi.eudico.client.annotator.player;

import java.awt.Component;

public interface NeedsCreateNewVisualComponent {
    /**
     * A Media Player that implements this interface needs special treatment when
     * creating and destroying a detached player. This is used in class
     * {@link mpi.eudico.client.annotator.layout.PlayerLayoutModel}.
     */
    public Component createNewVisualComponent();
}
