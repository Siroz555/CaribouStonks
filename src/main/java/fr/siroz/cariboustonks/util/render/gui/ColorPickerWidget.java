package fr.siroz.cariboustonks.util.render.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.text.Text;

import java.util.List;

@Deprecated
public class ColorPickerWidget extends ContainerWidget {

    public ColorPickerWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());

    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return 0;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 0;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
