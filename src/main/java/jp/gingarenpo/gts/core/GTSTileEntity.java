package jp.gingarenpo.gts.core;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * 共通して使用するようなTileEntityのメソッドを定義したもの。これ自体はAbstractなので使用できないので
 * 継承する必要あり。
 */
public abstract class GTSTileEntity extends TileEntity {
	
	/**
	 * 強制的に遠くまで描画できるようにする
	 * @return
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox();
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return Math.pow(128, 2);
	}
}
