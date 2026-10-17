package eu.decentsoftware.holograms.api.utils.scheduler;

import eu.decentsoftware.holograms.platform.api.player.PlatformPlayer;
import eu.decentsoftware.holograms.platform.api.player.PlatformPlayerService;
import eu.decentsoftware.holograms.platform.api.scheduler.PlatformScheduler;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class STest {

    @Test
    void playerWorkUsesTheEntityAwareSchedulerRoute() {
        PlatformScheduler scheduler = mock(PlatformScheduler.class);
        PlatformPlayerService players = mock(PlatformPlayerService.class);
        Player bukkitPlayer = mock(Player.class);
        PlatformPlayer platformPlayer = mock(PlatformPlayer.class);
        when(players.getPlayer(bukkitPlayer)).thenReturn(platformPlayer);
        S.initialize(scheduler, players);

        Runnable task = () -> { };
        S.forPlayer(bukkitPlayer, task);

        verify(scheduler).runForPlayer(platformPlayer, task);
    }
}
