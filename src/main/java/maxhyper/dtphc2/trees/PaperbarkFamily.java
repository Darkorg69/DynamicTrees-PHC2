package maxhyper.dtphc2.trees;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BasicBranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PaperbarkFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(PaperbarkFamily::new);

    ItemStack paperStack;
    float barkRegrowChance = 0.01f;

    public PaperbarkFamily(ResourceLocation name) {
        super(name);
        paperStack = new ItemStack(Items.PAPER);
    }

    public ItemStack getPaperStack(Level world) {
        ItemStack stack = paperStack.copy();
        stack.setCount(1 + world.random.nextInt(3));
        return stack;
    }

    @Override
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        final BasicBranchBlock branch = new BasicBranchBlock(name, this.getProperties().randomTicks()){
            @Override
            public void stripBranch(BlockState state, Level world, BlockPos pos, Player player, ItemStack heldItem) {
                Vec3 center = new Vec3(pos.getX()+0.5f, pos.getY()+0.5f, pos.getZ()+0.5f);
                Vec3 offsetDir = player.position().subtract(center).normalize().multiply(0.5,0.5,0.5);
                center = center.add(offsetDir);
                world.addFreshEntity(new ItemEntity(world, center.x, center.y, center.z, getPaperStack(world)));
                super.stripBranch(state,world,pos, getRadius(state));
            }

            @SuppressWarnings("deprecation")
            @Override
            public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
                if (rand.nextFloat() < barkRegrowChance && isStrippedBranch()){
                    int radius = TreeHelper.getRadius(world, pos);
                    int radiusDown = TreeHelper.isBranch(world.getBlockState(pos.below())) ? TreeHelper.getRadius(world, pos.below()) : getMaxRadius();
                    this.getFamily().getBranch().ifPresent(branch -> branch.setRadius(world, pos,
                                    Math.min(radiusDown, radius + (DTConfigs.ENABLE_STRIP_RADIUS_REDUCTION.get() ? 1 : 0)),
                                    null
                            )
                    );
                }
                super.tick(state, world, pos, rand);
            }
        };
        if (this.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }

}
