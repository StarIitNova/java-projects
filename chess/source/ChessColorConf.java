package source;

class ChessColorConf {

    public static final ColorProfile DARK_DARK = new ColorProfile(
        new RGB(0),
        new RGB(110, 145, 80)
    );
    public static final ColorProfile DARK_LIGHT = new ColorProfile(
        new RGB(255),
        new RGB(110, 145, 80)
    );
    public static final ColorProfile LIGHT_DARK = new ColorProfile(
        new RGB(0),
        new RGB(220, 220, 185)
    );
    public static final ColorProfile LIGHT_LIGHT = new ColorProfile(
        new RGB(255),
        new RGB(220, 220, 185)
    );

    public static final ColorProfile LIGHT_RED = new ColorProfile(
        new RGB(200, 0, 0),
        new RGB(220, 220, 185)
    );
    public static final ColorProfile DARK_RED = new ColorProfile(
        new RGB(200, 0, 0),
        new RGB(110, 145, 80)
    );

    public static final ColorProfile MAIN_BG = new ColorProfile(
        new RGB(215, 215, 225),
        new RGB(18, 21, 25)
    );

    public static final ColorProfile TIMER_BG = new ColorProfile(
        new RGB(215, 215, 225),
        new RGB(36, 41, 49)
    );
}
