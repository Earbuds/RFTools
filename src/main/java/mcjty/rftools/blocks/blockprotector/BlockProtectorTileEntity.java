package mcjty.rftools.blocks.blockprotector;

import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.entity.SyncedValueSet;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.smartwrench.SmartWrenchSelector;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Set;

public class BlockProtectorTileEntity extends GenericEnergyReceiverTileEntity implements SmartWrenchSelector {

    private int id = -1;

    // Relative coordinates (relative to this tile entity)
    private SyncedValueSet<Coordinate> protectedBlocks = new SyncedValueSet<Coordinate>() {
        @Override
        public Coordinate readElementFromNBT(NBTTagCompound tagCompound) {
            return Coordinate.readFromNBT(tagCompound, "c");
        }

        @Override
        public NBTTagCompound writeElementToNBT(Coordinate element) {
            return Coordinate.writeToNBT(element);
        }
    };

    public BlockProtectorTileEntity() {
        super(BlockProtectorConfiguration.MAXENERGY, BlockProtectorConfiguration.RECEIVEPERTICK);
        registerSyncedObject(protectedBlocks);
    }

    @Override
    protected void checkStateServer() {
        if (protectedBlocks.isEmpty()) {
            return;
        }
        consumeEnergy(protectedBlocks.size() * BlockProtectorConfiguration.rfPerProtectedBlock);
    }

    public boolean attemptHarvestProtection() {
        int rf = getEnergyStored(ForgeDirection.DOWN);
        if (BlockProtectorConfiguration.rfForHarvestAttempt > rf) {
            return false;
        }
        consumeEnergy(BlockProtectorConfiguration.rfForHarvestAttempt);
        return true;
    }

    // Distance is relative with 0 being closes to the explosion and 1 being furthest away.
    public int attemptExplosionProtection(float distance, float radius) {
        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rfneeded = (int) (BlockProtectorConfiguration.rfForExplosionProtection * (1.0 - distance) * radius / 8.0f) + 1;

        if (rfneeded > rf) {
            return -1;
        }
        if (rfneeded <= 0) {
            rfneeded = 1;
        }
        consumeEnergy(rfneeded);
        return rfneeded;
    }

    public Set<Coordinate> getProtectedBlocks() {
        return protectedBlocks;
    }

    public Coordinate absoluteToRelative(Coordinate c) {
        return absoluteToRelative(c.getX(), c.getY(), c.getZ());
    }

    public Coordinate absoluteToRelative(int x, int y, int z) {
        return new Coordinate(x - xCoord, y - yCoord, z - zCoord);
    }

    // Test if this relative coordinate is protected.
    public boolean isProtected(Coordinate c) {
        return protectedBlocks.contains(c);
    }

    // Used by the explosion event handler.
    public void removeProtection(Coordinate relative) {
        protectedBlocks.remove(relative);
        markDirty();
        notifyBlockUpdate();
    }

    // Toggle a coordinate to be protected or not. The coordinate given here is absolute.
    public void toggleCoordinate(GlobalCoordinate c) {
        if (c.getDimension() != worldObj.provider.dimensionId) {
            // Wrong dimension. Don't do anything.
            return;
        }
        Coordinate relative = absoluteToRelative(c.getCoordinate());
        if (protectedBlocks.contains(relative)) {
            protectedBlocks.remove(relative);
        } else {
            protectedBlocks.add(relative);
        }
        markDirty();
        notifyBlockUpdate();
    }

    @Override
    public void selectBlock(int x, int y, int z) {
        // This is always called server side.
        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(x, y, z), worldObj.provider.dimensionId);
        toggleCoordinate(gc);
    }

    public int getOrCalculateID() {
        if (id == -1) {
            BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);
            GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);
            id = protectors.getNewId(gc);

            protectors.save(worldObj);
            setId(id);
        }
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    /**
     * This method is called after putting down a protector that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);

        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);

        if (id == -1) {
            id = protectors.getNewId(gc);
            markDirty();
        } else {
            protectors.assignId(gc, id);
        }

        protectors.save(worldObj);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        protectedBlocks.readFromNBT(tagCompound, "coordinates");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("protectorId")) {
            id = tagCompound.getInteger("protectorId");
        } else {
            id = -1;
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        protectedBlocks.writeToNBT(tagCompound, "coordinates");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("protectorId", id);
    }

}
