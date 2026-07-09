package com.parshwa.create.radionautics.compat.cosmonautics;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.radio.RadioWebUBridge;
import dev.devce.websnodelib.api.NodeRegistry;
import dev.devce.websnodelib.api.WNode;
import dev.devce.websnodelib.api.elements.WLabel;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

public final class CosmonauticsWebUCompat {
    public static final ResourceLocation RADIO_RECEIVER =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_receiver");
    public static final ResourceLocation RADIO_TEXT_CHAR =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_text_char");
    public static final ResourceLocation RADIO_TEXT_VIEWER =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_text_viewer");

    private static boolean registered;

    private CosmonauticsWebUCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        NodeRegistry.register(RADIO_RECEIVER, "Radionautics", CosmonauticsWebUCompat::createRadioReceiver);
        NodeRegistry.register(RADIO_TEXT_CHAR, "Radionautics", CosmonauticsWebUCompat::createRadioTextChar);
        NodeRegistry.register(RADIO_TEXT_VIEWER, "Radionautics", CosmonauticsWebUCompat::createRadioTextViewer);
    }

    private static WNode createRadioReceiver(int x, int y) {
        WNode node = new WNode(RADIO_RECEIVER, "Radio Receiver", x, y);
        node.addInput("Frequency", 0xFFB95B);
        node.addOutput("Number", 0x55FF99);
        node.addOutput("Text Hash", 0x55D6FF);
        node.addOutput("Length", 0xD7B8FF);
        node.addOutput("Message Id", 0xFFFFFF);
        node.addOutput("Received Tick", 0xFFE66D);
        node.setEvaluator(CosmonauticsWebUCompat::evaluateRadioReceiver);
        return node;
    }

    private static WNode createRadioTextChar(int x, int y) {
        WNode node = new WNode(RADIO_TEXT_CHAR, "Radio Text Char", x, y);
        node.addInput("Frequency", 0xFFB95B);
        node.addInput("Index", 0xFFFFFF);
        node.addOutput("Code", 0x55D6FF);
        node.addOutput("Digit", 0x55FF99);
        node.addOutput("Is Digit", 0xFFE66D);
        node.addOutput("Length", 0xD7B8FF);
        node.addOutput("Received Tick", 0xFFE66D);
        node.setEvaluator(CosmonauticsWebUCompat::evaluateRadioTextChar);
        return node;
    }

    private static WNode createRadioTextViewer(int x, int y) {
        WNode node = new WNode(RADIO_TEXT_VIEWER, "Radio Text Viewer", x, y);
        node.addInput("Frequency", 0xFFB95B);
        node.addOutput("Message Id", 0xFFFFFF);
        node.addOutput("Received Tick", 0xFFE66D);
        WLabel label = new WLabel("No radio data");
        node.addElement(label);
        node.getCustomData().putString("LabelElement", "0");
        node.setEvaluator(n -> evaluateRadioTextViewer(n, label));
        return node;
    }

    private static void evaluateRadioReceiver(WNode node) {
        RadioWebUBridge.LastMessage message = getMessage(node, 0);
        setOutput(node, 0, message == null ? 0.0D : message.parsedNumber());
        setOutput(node, 1, message == null ? 0.0D : message.hash());
        setOutput(node, 2, message == null ? 0.0D : message.length());
        setOutput(node, 3, message == null ? 0.0D : message.messageId());
        setOutput(node, 4, message == null ? -1.0D : message.receivedDayTime());
    }

    private static void evaluateRadioTextChar(WNode node) {
        RadioWebUBridge.LastMessage message = getMessage(node, 0);
        int index = (int) Math.floor(input(node, 1));
        int code = message == null ? 0 : RadioWebUBridge.charCode(message.payload(), index);
        int digit = code >= '0' && code <= '9' ? code - '0' : -1;
        setOutput(node, 0, code);
        setOutput(node, 1, digit);
        setOutput(node, 2, digit >= 0 ? 1 : 0);
        setOutput(node, 3, message == null ? 0.0D : message.length());
        setOutput(node, 4, message == null ? -1.0D : message.receivedDayTime());
    }

    private static void evaluateRadioTextViewer(WNode node, WLabel label) {
        RadioWebUBridge.LastMessage message = getMessage(node, 0);
        if (message == null) {
            label.setText("No radio data");
            setOutput(node, 0, 0.0D);
            setOutput(node, 1, -1.0D);
            return;
        }
        label.setText(trimForNode(message.payload()));
        setOutput(node, 0, message.messageId());
        setOutput(node, 1, message.receivedDayTime());
    }

    private static RadioWebUBridge.LastMessage getMessage(WNode node, int pin) {
        return RadioWebUBridge.lastMessage(formatFrequency(input(node, pin)));
    }

    private static String formatFrequency(double frequency) {
        if (Math.rint(frequency) == frequency) {
            return Long.toString((long) frequency);
        }
        return String.format(Locale.ROOT, "%.6f", frequency).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static double input(WNode node, int index) {
        if (index < 0 || index >= node.getInputs().size()) {
            return 0.0D;
        }
        return node.getInputs().get(index).getValue();
    }

    private static void setOutput(WNode node, int index, double value) {
        if (index >= 0 && index < node.getOutputs().size()) {
            node.getOutputs().get(index).setValue(value);
        }
    }

    private static String trimForNode(String text) {
        if (text.length() <= 48) {
            return text;
        }
        return text.substring(0, 45) + "...";
    }
}
