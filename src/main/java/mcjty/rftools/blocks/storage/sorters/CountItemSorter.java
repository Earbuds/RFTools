package mcjty.rftools.blocks.storage.sorters;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class CountItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "count";
    }

    @Override
    public String getTooltip() {
        return "Sort on count";
    }

    @Override
    public int getU() {
        return 13*16;
    }

    @Override
    public int getV() {
        return 16;
    }

    @Override
    public Comparator<Pair<ItemStack, Integer>> getComparator() {
        return new Comparator<Pair<ItemStack, Integer>>() {
            @Override
            public int compare(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
                Integer c1 = o1.getLeft().stackSize;
                Integer c2 = o2.getLeft().stackSize;
                return c2.compareTo(c1);
            }
        };
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        Integer c1 = o1.getLeft().stackSize;
        Integer c2 = o2.getLeft().stackSize;
        return c2.compareTo(c1) == 0;
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return "Count " + object.getKey().stackSize;
    }
}
