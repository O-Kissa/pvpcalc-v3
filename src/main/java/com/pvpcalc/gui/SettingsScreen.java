        }

        message = Lang.t(
                "msg.set", keyName(kb), kb.getBoundKeyLocalizedText().getString()
        );
    }

    /** Looks at the keyboard while a key button is waiting for the new key. Escape clears the binding. */
    private void pollKey() {
        long h = GLFW.glfwGetCurrentContext();
        if (h == 0L || listening == null) return;

        for (int code : KEY_CODES) {
            if (GLFW.glfwGetKey(h, code) != GLFW.GLFW_PRESS) {
                held.remove(code);
                continue;
            }

            if (held.contains(code)) continue;

            KeyBinding kb = listening;
            listening = null;

            if (code == GLFW.GLFW_KEY_ESCAPE) {
                assign(kb, InputUtil.UNKNOWN_KEY);
            } else {
                assign(kb, InputUtil.Type.KEYSYM.createFromCode(code));
            }

            buildRows();
            return;
        }
    }

    /** While waiting for a key, Escape must not close the screen. */
    public boolean shouldCloseOnEsc() {
        return listening == null;
    }

    public boolean mouseScrolled(double mx, double my, double h, double v) {
        guard(() -> {
            page = Math.max(0, page + (v > 0 ? -1 : 1));
            buildRows();
        });
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (listening != null) {
            guard(this::pollKey);
        }

        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(
                this.textRenderer, Text.literal(Lang.t("gui.title")), 10, 11, 0xFFFFFF55
        );

        int y = rowsTop;
        for (Settings.Def d : pageRows) {
            int color = Settings.isDefault(d.id) ? 0xFFFFFFFF : 0xFFFFD855;
            ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal(nameOf(d)), 16, y + 6, color
            );
            y += 22;
        }

        int ky = rowsTop;
        for (KeyBinding kb : pageKeys) {
            ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal(keyName(kb)), 16, ky + 6, 0xFFFFFFFF
            );

            String c = conflictOf(kb);
            if (c != null) {
                ctx.drawTextWithShadow(
                        this.textRenderer,
                        Text.literal(Lang.t("gui.hotkey_conflict", c)),
                        this.width / 2 + 178,
                        ky + 6,
                        0xFFFF5555
                );
            }
            ky += 22;
        }

        if (!pageKeys.isEmpty()) {
            ctx.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal(Lang.t("gui.hotkeys_info")),
                    16,
                    this.height - 52,
                    0xFFAAAAAA
            );
        }

        if (!message.isEmpty()) {
            ctx.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal(message),
                    10,
                    this.height - 40,
                    0xFF55FF55
            );
        }

        ctx.drawTextWithShadow(
                this.textRenderer,
                Text.literal(Lang.t("gui.page", page + 1)),
                this.width - 60,
                this.height - 40,
                0xFFAAAAAA
        );

        if (System.currentTimeMillis() > confirmUntil && confirmUntil != 0) {
            confirmUntil = 0;
            resetTabButton.setMessage(Text.literal(Lang.t("gui.reset_tab")));
        }
    }

    @Override
    public void removed() {
        ConfigIo.saveIfDirty();
    }

    @Override
    public void close() {
        if (this.client != null) {
