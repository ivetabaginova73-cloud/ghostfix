package com.ghostfix.gui;

import com.ghostfix.GhostFixConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Ghostfix settings screen. Open it in game with Right Shift (changeable in Controls). */
public class GhostFixScreen extends Screen {
	private static final int INNER_W = 236;
	private static final int PAD = 12;
	private static final int ROW = 24;
	private static final int HEADER = 15;
	private static final int TOTAL_HEIGHT = 16 + 20 + (HEADER + ROW) + (HEADER + ROW * 2) * 3 + 30 + 8;

	private final Screen parent;

	private int panelL, panelT, panelR, panelB;
	private ConfigSlider cooldownSlider;

	public GhostFixScreen(Screen parent) {
		super(Text.literal("Ghostfix"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		GhostFixConfig cfg = GhostFixConfig.get();

		int x = (this.width - INNER_W) / 2;
		int top = Math.max(6, (this.height - TOTAL_HEIGHT) / 2);
		panelL = x - PAD;
		panelR = x + INNER_W + PAD;
		panelT = top;

		int y = top + 10;

		Text titleText = Text.literal("Ghostfix").formatted(Formatting.BOLD, Formatting.AQUA);
		int titleW = this.textRenderer.getWidth(titleText);
		TextWidget title = new TextWidget(titleW, 12, titleText, this.textRenderer);
		title.setPosition(x + (INNER_W - titleW) / 2, y);
		this.addDrawableChild(title);
		y += 20;

		// ---- Presets ----
		y = header(x, y, "Quick presets");
		int bw = (INNER_W - 8) / 3;
		this.addDrawableChild(preset(x, y, bw, "Light", 1, "Remembers a click for 1 tick. Best on low ping."));
		this.addDrawableChild(preset(x + bw + 4, y, bw, "Balanced", 2, "Remembers a click for 2 ticks. Good default."));
		this.addDrawableChild(preset(x + (bw + 4) * 2, y, bw, "Strong", 4, "Remembers a click for 4 ticks. For high ping."));
		y += ROW;

		// ---- General ----
		y = header(x, y, "General");
		this.addDrawableChild(toggle(x, y, "Ghostfix", () -> cfg.enabled, v -> cfg.enabled = v,
				"Master switch for the whole mod."));
		y += ROW;
		this.addDrawableChild(slider(x, y, "Click memory", 1, 5, cfg.bufferTicks,
				v -> cfg.bufferTicks = v, v -> v + (v == 1 ? " tick" : " ticks"),
				"How long a missed click is remembered. 1 tick = 0.05 s."));
		y += ROW;

		// ---- Targeting ----
		y = header(x, y, "Targeting");
		this.addDrawableChild(toggle(x, y, "Only players", () -> cfg.onlyPlayers, v -> cfg.onlyPlayers = v,
				"Only help when the target is another player."));
		y += ROW;
		this.addDrawableChild(toggle(x, y, "Only mid-air targets", () -> cfg.onlyMidAir, v -> cfg.onlyMidAir = v,
				"Only help when the target is not on the ground."));
		y += ROW;

		// ---- Cooldown ----
		y = header(x, y, "Attack cooldown");
		this.addDrawableChild(toggle(x, y, "Wait for cooldown", () -> cfg.waitForCooldown, v -> {
			cfg.waitForCooldown = v;
			if (cooldownSlider != null) {
				cooldownSlider.active = v;
			}
		}, "Prevents weak hits: a remembered click waits until your weapon has recharged."));
		y += ROW;
		cooldownSlider = slider(x, y, "Cooldown needed", 0, 100, cfg.cooldownPercent,
				v -> cfg.cooldownPercent = v, v -> v + "%",
				"How recharged your weapon must be before a remembered click fires.");
		cooldownSlider.active = cfg.waitForCooldown;
		this.addDrawableChild(cooldownSlider);
		y += ROW + 6;

		// ---- Footer ----
		int half = (INNER_W - 4) / 2;
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset to defaults"), b -> {
			GhostFixConfig.reset();
			this.clearAndInit();
		}).dimensions(x, y, half, 20).build());
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
				.dimensions(x + half + 4, y, half, 20).build());

		panelB = y + 20 + 10;
	}

	/** Dark panel behind all widgets. */
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(panelL - 1, panelT - 1, panelR + 1, panelB + 1, 0xFF000000);   // border
		context.fill(panelL, panelT, panelR, panelB, 0xE6141418);                    // body
		context.fill(panelL, panelT, panelR, panelT + 2, 0xFF3DD6E0);                // accent line
	}

	@Override
	public void close() {
		GhostFixConfig.save();
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}

	// ---------- helpers ----------

	private int header(int x, int y, String text) {
		TextWidget w = new TextWidget(INNER_W, 10, Text.literal(text).formatted(Formatting.GOLD), this.textRenderer);
		w.setPosition(x, y + 2);
		this.addDrawableChild(w);
		return y + HEADER;
	}

	private ButtonWidget preset(int x, int y, int w, String name, int ticks, String tip) {
		ButtonWidget b = ButtonWidget.builder(Text.literal(name), button -> {
			GhostFixConfig.get().bufferTicks = ticks;
			GhostFixConfig.save();
			this.clearAndInit();
		}).dimensions(x, y, w, 20).build();
		b.setTooltip(Tooltip.of(Text.literal(tip)));
		return b;
	}

	private interface BoolGetter { boolean get(); }
	private interface BoolSetter { void set(boolean v); }
	private interface IntSetter { void set(int v); }
	private interface IntFormatter { String format(int v); }

	private static Text toggleText(String name, boolean value) {
		return Text.literal(name + ": ")
				.append(Text.literal(value ? "ON" : "OFF").formatted(value ? Formatting.GREEN : Formatting.RED));
	}

	private static ButtonWidget toggle(int x, int y, String name, BoolGetter getter, BoolSetter setter, String tip) {
		ButtonWidget b = ButtonWidget.builder(toggleText(name, getter.get()), button -> {
			boolean next = !getter.get();
			setter.set(next);
			button.setMessage(toggleText(name, next));
			GhostFixConfig.save();
		}).dimensions(x, y, INNER_W, 20).build();
		b.setTooltip(Tooltip.of(Text.literal(tip)));
		return b;
	}

	private static ConfigSlider slider(int x, int y, String name, int min, int max, int current,
	                                   IntSetter setter, IntFormatter formatter, String tip) {
		ConfigSlider s = new ConfigSlider(x, y, name, min, max, current, setter, formatter);
		s.setTooltip(Tooltip.of(Text.literal(tip)));
		return s;
	}

	/** Whole-number slider. */
	private static class ConfigSlider extends SliderWidget {
		private final String name;
		private final int min;
		private final int max;
		private final IntSetter setter;
		private final IntFormatter formatter;

		ConfigSlider(int x, int y, String name, int min, int max, int current,
		             IntSetter setter, IntFormatter formatter) {
			super(x, y, INNER_W, 20, Text.empty(), (current - min) / (double) (max - min));
			this.name = name;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.formatter = formatter;
			this.updateMessage();
		}

		private int currentValue() {
			return min + (int) Math.round(this.value * (max - min));
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.literal(name + ": " + formatter.format(currentValue())));
		}

		@Override
		protected void applyValue() {
			setter.set(currentValue());
			GhostFixConfig.save();
		}
	}
}
