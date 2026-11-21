// Made with Blockbench 5.2.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class jhniju<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "jhniju"), "main");
	private final ModelPart Body;
	private final ModelPart Tail;
	private final ModelPart LegBL;
	private final ModelPart LegBR;
	private final ModelPart LegFL;
	private final ModelPart LegFR;
	private final ModelPart Neck;
	private final ModelPart Head;
	private final ModelPart Muzzle;
	private final ModelPart EarL;
	private final ModelPart EarR;
	private final ModelPart MuleEarL;
	private final ModelPart MuleEarR;
	private final ModelPart ReinsL;
	private final ModelPart ReinsR;
	private final ModelPart Bridle;
	private final ModelPart BitL;
	private final ModelPart BitR;
	private final ModelPart Mane;
	private final ModelPart BagL;
	private final ModelPart BagR;
	private final ModelPart Saddle;

	public jhniju(ModelPart root) {
		this.Body = root.getChild("Body");
		this.Tail = this.Body.getChild("Tail");
		this.LegBL = this.Body.getChild("LegBL");
		this.LegBR = this.Body.getChild("LegBR");
		this.LegFL = this.Body.getChild("LegFL");
		this.LegFR = this.Body.getChild("LegFR");
		this.Neck = this.Body.getChild("Neck");
		this.Head = this.Neck.getChild("Head");
		this.Muzzle = this.Head.getChild("Muzzle");
		this.EarL = this.Head.getChild("EarL");
		this.EarR = this.Head.getChild("EarR");
		this.MuleEarL = this.Head.getChild("MuleEarL");
		this.MuleEarR = this.Head.getChild("MuleEarR");
		this.ReinsL = this.Head.getChild("ReinsL");
		this.ReinsR = this.Head.getChild("ReinsR");
		this.Bridle = this.Head.getChild("Bridle");
		this.BitL = this.Bridle.getChild("BitL");
		this.BitR = this.Bridle.getChild("BitR");
		this.Mane = this.Neck.getChild("Mane");
		this.BagL = this.Body.getChild("BagL");
		this.BagR = this.Body.getChild("BagR");
		this.Saddle = this.Body.getChild("Saddle");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -20.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 9.0F));

		PartDefinition Tail = Body.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 2.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition LegBL = Body.addOrReplaceChild("LegBL", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 2.0F, 0.0F));

		PartDefinition LegBR = Body.addOrReplaceChild("LegBR", CubeListBuilder.create().texOffs(48, 21).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 2.0F, 0.0F));

		PartDefinition LegFL = Body.addOrReplaceChild("LegFL", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 2.0F, -18.0F));

		PartDefinition LegFR = Body.addOrReplaceChild("LegFR", CubeListBuilder.create().texOffs(48, 21).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 2.0F, -18.0F));

		PartDefinition Neck = Body.addOrReplaceChild("Neck", CubeListBuilder.create().texOffs(0, 35).addBox(-2.0F, -11.0F, -3.0F, 4.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -17.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition Head = Neck.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -5.0F, 0.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, -3.0F));

		PartDefinition Muzzle = Head.addOrReplaceChild("Muzzle", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition EarL = Head.addOrReplaceChild("EarL", CubeListBuilder.create().texOffs(19, 16).mirror().addBox(-0.5F, -18.0F, 2.99F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition EarR = Head.addOrReplaceChild("EarR", CubeListBuilder.create().texOffs(19, 16).addBox(-1.5F, -18.0F, 2.99F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, 0.0F, 0.0F, -0.0873F));

		PartDefinition MuleEarL = Head.addOrReplaceChild("MuleEarL", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-3.0F, -22.0F, 2.99F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition MuleEarR = Head.addOrReplaceChild("MuleEarR", CubeListBuilder.create().texOffs(0, 12).addBox(1.0F, -22.0F, 2.99F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition ReinsL = Head.addOrReplaceChild("ReinsL", CubeListBuilder.create().texOffs(32, 2).addBox(1.1F, -1.5F, 0.5F, 0.0F, 3.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -2.5F, -2.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition ReinsR = Head.addOrReplaceChild("ReinsR", CubeListBuilder.create().texOffs(32, 2).addBox(-0.1F, -1.5F, 0.5F, 0.0F, 3.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.5F, -2.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition Bridle = Head.addOrReplaceChild("Bridle", CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -16.0F, -5.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.25F))
		.texOffs(0, 0).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 11.0F, 3.0F));

		PartDefinition BitL = Bridle.addOrReplaceChild("BitL", CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -14.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BitR = Bridle.addOrReplaceChild("BitR", CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -14.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mane = Neck.addOrReplaceChild("Mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -16.0F, 4.05F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BagL = Body.addOrReplaceChild("BagL", CubeListBuilder.create().texOffs(26, 21).mirror().addBox(1.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -1).addBox(7.0F, -1.0F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(1.0F, -1.0F, 1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 1).addBox(3.0F, 1.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(4.0F, 2.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -4).addBox(1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -8.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition BagR = Body.addOrReplaceChild("BagR", CubeListBuilder.create().texOffs(26, 21).addBox(-9.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -1.0F, 1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-1, -1).addBox(-9.0F, -1.0F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 1).addBox(-7.0F, 1.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-6.0F, 2.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -4).addBox(-3.0F, -1.0F, -5.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(-2, -1).addBox(-1.0F, -2.0F, 1.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -8.0F, 2.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Saddle = Body.addOrReplaceChild("Saddle", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, 1.0F, -5.5F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -9.0F, -7.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}