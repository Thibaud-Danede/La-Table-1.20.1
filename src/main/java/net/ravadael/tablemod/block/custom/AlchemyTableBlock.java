package net.ravadael.tablemod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

public class AlchemyTableBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Component CONTAINER_TITLE = Component.translatable("container.tablemod.alchemy");

    // ---------------------
    // HITBOX DE BASE (NORTH)
    // 16 × 14 × 15 px
    // ---------------------
    private static final VoxelShape SHAPE_NORTH = Shapes.box(
            0.0,            // minX
            0.0,            // minY
            1.0 / 16.0,     // minZ (décalé pour longueur 14)
            1.0,            // maxX
            15.0 / 16.0,    // maxY
            15.0 / 16.0     // maxZ
    );

    private static final VoxelShape SHAPE_EAST  = rotateShape(SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(SHAPE_EAST);
    private static final VoxelShape SHAPE_WEST  = rotateShape(SHAPE_SOUTH);

    public AlchemyTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST  -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_NORTH;
        };
    }

    // -------------------------------------
    // Rotation correcte d'une VoxelShape
    // -------------------------------------
    private static VoxelShape rotateShape(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double newMinX = 1 - maxZ;
            double newMinZ = minX;
            double newMaxX = 1 - minZ;
            double newMaxZ = maxX;

            buffer[1] = Shapes.or(buffer[1], Shapes.box(
                    newMinX, minY, newMinZ,
                    newMaxX, maxY, newMaxZ
            ));
        });

        return buffer[1];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inv, player) -> new AlchemyTableMenu(id, inv, level, pos),
                CONTAINER_TITLE
        );
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
}
