package net.runelite.mixins;

import net.runelite.api.events.PostItemComposition;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.MethodHook;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSItemComposition;
import net.runelite.rs.api.RSNode;

@Mixin(RSItemComposition.class)
public abstract class RSItemCompositionMixin implements RSItemComposition
{

    @Shadow("client")
    private static RSClient client;

    @Inject
    @MethodHook(value = "post", end = true)
    public void post(int garbage)
    {
        final PostItemComposition event = new PostItemComposition();
        event.setItemComposition(this);
        client.getEventBus().post(event);
    }
}
