/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */

package com.myopicmobile.textwarrior.common;


public class ColorSchemeDark extends ColorScheme {

    private static final int BLACK = 0xFF000000;
    private static final int BACKGROUBD_ = 0xFF303030;
    private static final int G_BLUE = 0xFF2F97EF;
    private static final int DARK_RED = 0xFF8B0000;
    private static final int DARK_ORA = 0xFFFF8000;
    private static final int GREY = 0xFF808080;
    private static final int LIGHT_GREY = 0xFFAAAAAA;
    private static final int MAROON = 0xFF800000;
    private static final int INDIGO = 0xFF40B0FF;
    private static final int OLIVE_GREEN = 0xFF3F7F5F;
    private static final int STRING_GREEN = 0xFF6E865A;
    private static final int RED = 0x44FF0000;
    private static final int WHITE = 0xFFFFFFE0;
    private static final int LIGHT_BLUE = 0xFF6080FF;
    private static final int LIGHT_BLUE2 = 0xFF40B0FF;
    private static final int GREEN = 0xFF88AA88;

    public ColorSchemeDark() {
        // High-contrast, black-on-white color scheme
        setColor(Colorable.FOREGROUND, G_BLUE);//2函数
        setColor(Colorable.BACKGROUND, BACKGROUBD_);
        setColor(Colorable.SELECTION_FOREGROUND, WHITE);
        setColor(Colorable.SELECTION_BACKGROUND, 0xFF97C024);
        setColor(Colorable.CARET_FOREGROUND, WHITE);
        setColor(Colorable.CARET_BACKGROUND, LIGHT_BLUE2);
        setColor(Colorable.CARET_DISABLED, GREY);
        setColor(Colorable.LINE_HIGHLIGHT, 0x20888888);

        setColor(Colorable.NON_PRINTING_GLYPH, LIGHT_GREY);
        setColor(Colorable.COMMENT, OLIVE_GREEN);
        setColor(Colorable.KEYWORD, DARK_ORA);
        setColor(Colorable.NAME, INDIGO);
        setColor(Colorable.LITERAL, LIGHT_BLUE);
        setColor(Colorable.STRING, STRING_GREEN);
        setColor(Colorable.SECONDARY, GREY);
    }

    @Override
    public boolean isDark() {
        return true;
    }
}
