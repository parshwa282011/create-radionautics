package com.parshwa.create.radionautics.client;

import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import com.parshwa.create.radionautics.menu.BrassRadioLinkMenu;
import com.parshwa.create.radionautics.network.ConfigureBrassRadioLinkPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class BrassRadioLinkScreen extends AbstractContainerScreen<BrassRadioLinkMenu> {
    private EditBox frequency;
    private EditBox encryptionKey;
    private EditBox linkIdentifier;
    private static final String[] CRYPTO_TYPES = {"none", "xor", "aes", "base64"};
    private String encryptionTypeValue = "none";
    private boolean receiver;
    private boolean encrypted;
    private Button modeButton;
    private Button encryptionButton;
    private Button cryptoButton;

    public BrassRadioLinkScreen(BrassRadioLinkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 238;
        imageHeight = 188;
    }

    @Override
    protected void init() {
        super.init();
        BrassRadioLinkMenu.Snapshot snapshot = menu.snapshot();
        receiver = snapshot.receiver();
        encrypted = snapshot.encrypted();
        encryptionTypeValue = normalizeCrypto(snapshot.encryptionType());

        int x = leftPos + 22;
        int y = topPos + 34;
        frequency = addTextBox(x, y, 194, snapshot.frequency(), "Frequency");
        modeButton = addRenderableWidget(Button.builder(modeText(), button -> {
            receiver = !receiver;
            button.setMessage(modeText());
        }).bounds(x, y + 26, 94, 20).build());
        encryptionButton = addRenderableWidget(Button.builder(encryptionText(), button -> {
            encrypted = !encrypted;
            button.setMessage(encryptionText());
        }).bounds(x + 100, y + 26, 94, 20).build());
        cryptoButton = addRenderableWidget(Button.builder(cryptoText(), button -> {
            encryptionTypeValue = nextCrypto(encryptionTypeValue);
            button.setMessage(cryptoText());
        }).bounds(x, y + 62, 90, 20).build());
        encryptionKey = addTextBox(x + 100, y + 62, 94, snapshot.encryptionKey(), "Key");
        linkIdentifier = addTextBox(x, y + 98, 194, snapshot.linkIdentifier(), "Identifier");

        addRenderableWidget(Button.builder(Component.translatable("gui.create_radio.brass_radio_link.save"), button -> save())
                .bounds(x + 114, y + 126, 80, 20)
                .build());
    }

    private EditBox addTextBox(int x, int y, int width, String value, String hint) {
        EditBox box = new EditBox(font, x, y, width, 20, Component.literal(hint));
        box.setMaxLength(128);
        box.setValue(value == null ? "" : value);
        box.setHint(Component.literal(hint));
        addRenderableWidget(box);
        return box;
    }

    private Component modeText() {
        return Component.translatable(receiver ? "gui.create_radio.brass_radio_link.receiver" : "gui.create_radio.brass_radio_link.sender");
    }

    private Component encryptionText() {
        return Component.translatable(encrypted ? "gui.create_radio.brass_radio_link.encrypted" : "gui.create_radio.brass_radio_link.clear");
    }

    private Component cryptoText() {
        return Component.literal("Crypto: " + encryptionTypeValue);
    }

    private static String normalizeCrypto(String value) {
        if (value != null) {
            for (String type : CRYPTO_TYPES) {
                if (type.equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
        }
        return "none";
    }

    private static String nextCrypto(String value) {
        String normalized = normalizeCrypto(value);
        for (int i = 0; i < CRYPTO_TYPES.length; i++) {
            if (CRYPTO_TYPES[i].equals(normalized)) {
                return CRYPTO_TYPES[(i + 1) % CRYPTO_TYPES.length];
            }
        }
        return CRYPTO_TYPES[0];
    }

    private void save() {
        PacketDistributor.sendToServer(new ConfigureBrassRadioLinkPayload(
                menu.pos(),
                frequency.getValue(),
                receiver,
                encrypted,
                encryptionTypeValue,
                encryptionKey.getValue(),
                linkIdentifier.getValue()));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x3B2A12, false);
        graphics.drawString(font, Component.translatable("gui.create_radio.brass_radio_link.frequency"), 22, 22, 0x5C421D, false);
        graphics.drawString(font, Component.translatable("gui.create_radio.brass_radio_link.crypto"), 22, 84, 0x5C421D, false);
        graphics.drawString(font, Component.translatable("gui.create_radio.brass_radio_link.identifier"), 22, 120, 0x5C421D, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFB78A3C);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFFE0B86C);
        graphics.fill(x + 12, y + 18, x + imageWidth - 12, y + imageHeight - 12, 0xFFF1D68C);
        graphics.fill(x + 16, y + 24, x + imageWidth - 16, y + imageHeight - 16, 0xFFFAE8AD);
    }
}
