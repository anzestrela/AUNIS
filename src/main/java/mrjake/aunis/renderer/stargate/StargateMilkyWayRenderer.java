package mrjake.aunis.renderer.stargate;

import mrjake.aunis.OBJLoader.ModelEnum;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.OBJModel;
import mrjake.aunis.util.math.MathFunction;
import mrjake.aunis.util.math.MathFunctionImpl;
import mrjake.aunis.util.math.MathRange;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class StargateMilkyWayRenderer extends StargateClassicRenderer<StargateMilkyWayRendererState> {
	
	private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
	private static final float GATE_DIAMETER = 10.1815f;
	
	@Override
	protected void applyTransformations(StargateMilkyWayRendererState rendererState) {
		GlStateManager.translate(0.50, GATE_DIAMETER/2 + rendererState.stargateSize.renderTranslationY, 0.50);			
		GlStateManager.scale(rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale);
	}
	
	@Override
	protected void renderGate(StargateMilkyWayRendererState rendererState, double partialTicks) {
		renderRing(rendererState, partialTicks);
		GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
		renderChevrons(rendererState, partialTicks);
		
		rendererDispatcher.renderEngine.bindTexture(ModelEnum.MILKYWAY_GATE_MODEL.textureResource);
		ModelLoader.getModel(ModelEnum.MILKYWAY_GATE_MODEL).render();
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Ring
	
	private void renderRing(StargateMilkyWayRendererState rendererState, double partialTicks) {
		GlStateManager.pushMatrix();
		float angularRotation = rendererState.spinHelper.currentSymbol.getAngle();
		
		if (rendererState.spinHelper.isSpinning)
			angularRotation += rendererState.spinHelper.apply(getWorld().getTotalWorldTime() + partialTicks);
		
		if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 0)
			angularRotation *= -1;

		
		if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 270) {
			GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
			GlStateManager.rotate(angularRotation, 1, 0, 0);
			GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
		}
		
		else {
			GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
			GlStateManager.rotate(angularRotation, 0, 0, 1);
			GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
		}
		
		GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
		
		rendererDispatcher.renderEngine.bindTexture(ModelEnum.MILKYWAY_RING_MODEL.textureResource);
		ModelLoader.getModel(ModelEnum.MILKYWAY_RING_MODEL).render();
		
		GlStateManager.popMatrix();
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	private static MathRange chevronOpenRange = new MathRange(0, 1.57f);
	private static MathFunction chevronOpenFunction = new MathFunctionImpl(x -> x*x*x*x/80f);
	
	private static MathRange chevronCloseRange = new MathRange(0, 1.428f);
	private static MathFunction chevronCloseFunction = new MathFunctionImpl(x0 -> MathHelper.cos(x0*1.1f) / 12f);
	
	private float calculateTopChevronOffset(StargateMilkyWayRendererState rendererState, double partialTicks) {
		float tick = (float) (getWorld().getTotalWorldTime() - rendererState.chevronActionStart + partialTicks);
		float x = tick / 6.0f;
		
		if (rendererState.chevronOpening) {
			if (chevronOpenRange.test(x))
				return chevronOpenFunction.apply(x);
			else {
				rendererState.chevronOpen = true;
				rendererState.chevronOpening = false;
			}
		}
		
		else if (rendererState.chevronClosing) {
			if (chevronCloseRange.test(x))
				return chevronCloseFunction.apply(x);
			else {
				rendererState.chevronOpen = false;
				rendererState.chevronClosing = false;
			}
		}
		
		return rendererState.chevronOpen ? 0.08333f : 0;
	}
	
	@Override
	protected void renderChevron(StargateMilkyWayRendererState rendererState, double partialTicks, ChevronEnum chevron) {		
		OBJModel ChevronLight = ModelLoader.getModel(ModelEnum.MILKYWAY_CHEVRON_LIGHT);
		OBJModel ChevronMoving = ModelLoader.getModel(ModelEnum.MILKYWAY_CHEVRON_MOVING);

		GlStateManager.pushMatrix();
			
		GlStateManager.rotate(chevron.rotation, 0, 0, 1);
					
		rendererDispatcher.renderEngine.bindTexture(rendererState.chevronTextureList.get(chevron));
					
		if (chevron.isFinal()) {
			float chevronOffset = calculateTopChevronOffset(rendererState, partialTicks);
			
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(0, chevronOffset, 0);
			ChevronLight.render();
			
			GlStateManager.translate(0, -2*chevronOffset, 0);
			ChevronMoving.render();
			
			GlStateManager.popMatrix();
		}
		
		else {
			ChevronMoving.render();
			
//			GlStateManager.disableLighting();
			ChevronLight.render();	
			GlStateManager.enableLighting();
		}			
		
		rendererDispatcher.renderEngine.bindTexture(ModelEnum.MILKYWAY_CHEVRON_FRAME.textureResource);
		ModelLoader.getModel(ModelEnum.MILKYWAY_CHEVRON_FRAME).render();
		ModelLoader.getModel(ModelEnum.MILKYWAY_CHEVRON_BACK).render();
		
		GlStateManager.popMatrix();
	}
}
