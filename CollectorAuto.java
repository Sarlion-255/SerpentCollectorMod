package it.serpentcollector;

import it.serpentcollector.route.RouteManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

public class CollectorAuto {

    public enum Phase {
        IDLE,
        TRANSFERRING,
        NEXT_STATION,
        FINISHED
    }

    private Phase currentPhase = Phase.IDLE;
    private final RouteManager routeManager = new RouteManager();

    public void startRoute(BlockPos startPos, BlockPos endPos) {
        routeManager.setupRoute(startPos, endPos);
        currentPhase = Phase.TRANSFERRING;
    }

    public void tick(MinecraftClient client) {
        if (currentPhase == Phase.IDLE || currentPhase == Phase.FINISHED || client.world == null) {
            return;
        }

        RouteManager.DepositStation currentStation = routeManager.getCurrentStation();
        if (currentStation == null) {
            currentPhase = Phase.FINISHED;
            return;
        }

        BlockPos depositPos = currentStation.getDepositPos();
        boolean allCollectorsEmpty = true;

        for (BlockPos collectorPos : currentStation.getCollectorPositions()) {
            if (client.world.getBlockEntity(collectorPos) instanceof Inventory collectorInv) {
                if (!collectorInv.isEmpty()) {
                    allCollectorsEmpty = false;
                    
                    if (client.world.getBlockEntity(depositPos) instanceof Inventory depositInv) {
                        transferItems(collectorInv, depositInv);
                    }
                }
            }
        }

        if (allCollectorsEmpty) {
            if (!routeManager.nextStation()) {
                currentPhase = Phase.FINISHED;
            }
        }
    }

    private void transferItems(Inventory source, Inventory destination) {
        for (int i = 0; i < source.size(); i++) {
            var stack = source.getStack(i);
            if (!stack.isEmpty()) {
                for (int j = 0; j < destination.size(); j++) {
                    var destStack = destination.getStack(j);
                    if (destStack.isEmpty()) {
                        destination.setStack(j, stack.copy());
                        source.setStack(i, net.minecraft.item.ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
    }
}
