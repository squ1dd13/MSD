package com.squ1dd13.msd.decompiler.high;

public class SyntaxHighlight {
    private static class RGBColor {
        int r, g, b;

        RGBColor(int red, int green, int blue) {
            r = red;
            g = green;
            b = blue;
        }

        String colorString() {
            return "\033[38;2;" + r + ";" + g + ";" + b + "m";
        }
    };

    static String white = new RGBColor(255, 255, 255).colorString();
    static String green = new RGBColor(100, 255, 100).colorString();
    static String blue = new RGBColor(100, 100, 255).colorString();
    static String red = new RGBColor(255, 100, 100).colorString();
    static String codeColor = new RGBColor(200, 255, 255).colorString();
    static String gray = new RGBColor(100, 100, 100).colorString();
    static String varColor =  new RGBColor(255, 200, 200).colorString();
    static String pink = new RGBColor(255, 150, 200).colorString();
    static String orange = new RGBColor(255, 150, 0).colorString();
    static String blueGreen = new RGBColor(0, 220, 200).colorString();
    static String callColor = new RGBColor(255, 255, 100).colorString();
    static String stringColor = new RGBColor(50, 200, 255).colorString();

    public static String highlightLine(String line) {
        line = line.replaceAll("(?<=(?:if\\()|(?:while\\()|(?:and)|(?:or))(\\s*\\?[^(]+)", callColor + "$1" + codeColor);
        line = line.replaceAll("if\\(([^)]+)\\)", pink + "if" + codeColor + "($1)");
        line = line.replaceAll("while\\(([^)]+)\\)", pink + "while" + codeColor + "($1)");
        line = line.replaceAll("([^:]+):", blueGreen + "$1" + codeColor + ":");
        line = line.replaceAll("(&?local[^_]+_\\d+)", orange + "$1" + codeColor);
        line = line.replaceAll("(&?global[^_]+_\\d+)", red + "$1" + codeColor);
        line = line.replaceAll("(?<=(?:\\()|(?:, ))(-?[\\d.]+f?)", green + "$1" + codeColor);
        line = line.replaceAll("([^(]+)\\(", varColor + "$1" + codeColor + "(");
        line = line.replaceAll("'(\\.|[^'])*'", stringColor + "$0" + codeColor);

        return line;
    }
}
